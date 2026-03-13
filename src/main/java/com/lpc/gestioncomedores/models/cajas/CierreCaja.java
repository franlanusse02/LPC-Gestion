package com.lpc.gestioncomedores.models.cajas;


import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.comedores.PuntoDeVenta;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCaja;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCajaLinea;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoCaja;
import com.lpc.gestioncomedores.models.personas.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Table(
        name = "cierres_caja",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cierre_caja",
                columnNames = {
                        "comedor_id",
                        "punto_venta_id",
                        "fecha_operacion",
                        "estado"
                }
        )
)
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoDeVenta puntoDeVenta;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario creadoPor;

    @Column(name = "total_platos_vendidos")
    private Integer totalPlatosVendidos;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCierreCaja estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    private String observaciones;

    private Instant anuladoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_anulacion_id")
    private Usuario anuladoPor;

    private String motivoAnulacion;

    @OneToMany(mappedBy = "cierreCaja", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientoCaja> movimientos = new ArrayList<>();

    @OneToMany(mappedBy = "cierreCaja", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CierreCajaLinea> lineas = new ArrayList<>();


    // METHODS

    protected CierreCaja() {
        //JPA
    }

    private CierreCaja(
            Comedor comedor,
            PuntoDeVenta puntoDeVenta,
            LocalDate fechaOperacion,
            Usuario creadoPor,
            String observaciones,
            Integer totalPlatosVendidos
    ) {
        this.comedor = comedor;
        this.puntoDeVenta = puntoDeVenta;
        this.fechaOperacion = fechaOperacion;
        this.creadoPor = creadoPor;
        this.creadoEn = Instant.now();
        this.estado = EstadoCierreCaja.BORRADOR;
        this.observaciones = trimToNull(observaciones);
        this.totalPlatosVendidos = totalPlatosVendidos;

        this.lineas = new ArrayList<>();
        this.movimientos = new ArrayList<>();

        this.anuladoPor = null;
        this.anuladoEn = null;
        this.motivoAnulacion = null;
    }



    public static CierreCaja crear(
            Comedor comedor,
            PuntoDeVenta puntoDeVenta,
            LocalDate fechaOperacion,
            Usuario creadoPor,
            String observaciones,
            Integer totalPlatosVendidos
    ){
        if (comedor == null) {
            throw new BadRequestException("Comedor no puede ser null");
        } else if (puntoDeVenta == null) {
            throw new BadRequestException("Punto de venta no puede ser null");
        } else if (fechaOperacion == null) {
            throw new BadRequestException("Fecha de operacion no puede ser null");
        } else if (creadoPor == null) {
            throw new BadRequestException("Usuario no puede ser null");
        } else if (totalPlatosVendidos != null && totalPlatosVendidos < 0) {
            throw new BadRequestException("Total de platos vendidos no puede ser menor a 0");
        } if (!puntoDeVenta.getComedor().getId().equals(comedor.getId())) {
            throw new BadRequestException("Punto de venta no pertenece a ese comedor");
        }

        return new CierreCaja(comedor, puntoDeVenta, fechaOperacion, creadoPor, observaciones, totalPlatosVendidos);


    }

    public void actualizarTotalPlatosVendidos(Integer totalPlatosVendidos) {
        if (this.estado != EstadoCierreCaja.BORRADOR) {
            throw new IllegalStateException("No se pueden modificar cierres que no esten en estado BORRADOR");
        } else if (totalPlatosVendidos != null && totalPlatosVendidos < 0) {
            throw new BadRequestException("Total de platos vendidos no puede ser menor a 0");
        }

        this.totalPlatosVendidos = totalPlatosVendidos;
    }

    public void actualizarObservaciones(String observaciones) {
        if (this.estado != EstadoCierreCaja.BORRADOR) {
            throw new IllegalStateException("No se pueden modificar cierres que no esten en estado BORRADOR");
        }
        this.observaciones = trimToNull(observaciones);
    }

    public void anular(
            Usuario usuario,
            String motivo
    ) {
        if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null");
        } else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio.");
        } else if (this.estado == EstadoCierreCaja.ANULADO) {
            throw new IllegalStateException("Este cierre ya esta anulado");
        }
        String motivoNormalizado = trimToNull(motivo);

        this.movimientos.stream()
                .filter(m -> !m.estaAnulado())
                .forEach(m -> m.anular(usuario, motivoNormalizado));

        this.estado = EstadoCierreCaja.ANULADO;
        this.anuladoEn = Instant.now();
        this.anuladoPor = usuario;
        this.motivoAnulacion = motivoNormalizado;
    }


    public void agregarLinea(CierreCajaLinea linea) {
        if (this.estado == EstadoCierreCaja.ANULADO) {
            throw new IllegalStateException("No se puede agregar lineas a un cierre anulado");
        } else if (this.estado == EstadoCierreCaja.CERRADO) {
            throw new IllegalStateException("No se puede agregar lineas a un cierre cerrado.");
        } else if (linea == null) {
            throw new BadRequestException("CierreCajaLinea no puede ser null.");
        } if (!linea.estaActivaLinea()) {
            throw new IllegalStateException("No se puede agregar una linea no activa");
        }

        if (this.lineas.stream()
                .anyMatch(l -> linea.getMedioPago() == l.getMedioPago() && l.estaActivaLinea()
            )
        ){
            throw new IllegalStateException("Cierre ya posee una linea activa con ese medio de pago.");
        }
        linea.asignarCierreCaja(this);
        this.lineas.add(linea);
    }

    public void reemplazarLinea(
            CierreCajaLinea lineaNueva,
            Long lineaViejaId,
            Usuario usuario,
            String motivo
    ) {
        if (lineaNueva == null) {
            throw new BadRequestException("Nueva linea no puede ser null");
        } else if (lineaViejaId == null) {
            throw new BadRequestException("Linea vieja no puede ser null");
        } else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        } else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio.");
        } else if (this.estado == EstadoCierreCaja.ANULADO || this.estado == EstadoCierreCaja.CERRADO) {
            throw new IllegalStateException("Solo se pueden modificar cierres en estado BORRADOR");
        } else if (!lineaNueva.estaActivaLinea()) {
            throw new IllegalStateException("No se puede reemplazar por una linea no activa.");
        }

        CierreCajaLinea lineaVieja = this.lineas.stream()
                .filter(l -> lineaViejaId.equals(l.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No se encontro la linea vieja indicada"));

        if (!lineaVieja.estaActivaLinea()) {
            throw new IllegalStateException("Solo se puede reemplazar una linea activa.");
        }

        boolean existeOtraLineaActivaConMismoMedioPago = this.lineas.stream()
                .filter(CierreCajaLinea::estaActivaLinea)
                .filter(l -> !lineaViejaId.equals(l.getId()))
                .anyMatch(l -> l.getMedioPago() == lineaNueva.getMedioPago());

        if (existeOtraLineaActivaConMismoMedioPago) {
            throw new IllegalStateException("Cierre ya posee otra linea activa con ese medio de pago.");
        }

        lineaVieja.anularLinea(usuario, motivo);
        lineaNueva.asignarCierreCaja(this);
        this.lineas.add(lineaNueva);
    }


    public BigDecimal calcularMontoTotal() {
        return this.lineas.stream()
                .filter(l -> l.getEstado() == EstadoCierreCajaLinea.ACTIVA)
                .map(l -> l.getMonto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void finalizarCierre() {
        if (this.estado != EstadoCierreCaja.BORRADOR) {
            throw new IllegalStateException("Solo se pueden finalizar cierres en estado borrador.");
        }
        List<CierreCajaLinea> lineasActivas = this.lineas.stream()
                .filter(CierreCajaLinea::estaActivaLinea)
                .toList();

        if (lineasActivas.isEmpty()) {
            throw new IllegalStateException("Cierre no tiene lineas activas.");
        }

        for (CierreCajaLinea linea : lineasActivas) {
            MovimientoCaja mov = MovimientoCaja.createDesdeCierre(
                    this,
                    this.comedor,
                    this.puntoDeVenta,
                    this.creadoPor,
                    linea.getMonto(),
                    linea.getMedioPago(),
                    Instant.now(),
                    this.observaciones
            );
            this.movimientos.add(mov);
        }

        this.estado = EstadoCierreCaja.CERRADO;
    }




    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
