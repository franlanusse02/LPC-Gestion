package com.lpc.gestioncomedores.models.cajas;


import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.comedores.PuntoDeVenta;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCaja;
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
@AllArgsConstructor
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoDeVenta puntoDeVenta;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario creadoPor;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Enumerated(EnumType.STRING) @Column(name = "estado", nullable = false)
    private EstadoCierreCaja estado;

    private String observaciones;

    private Instant anuladoEn;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_anulacion_id")
    private Usuario anuladoPor;

    private String motivoAnulacion;

    @OneToMany(mappedBy = "cierreCaja", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientoCaja> movimientos = new ArrayList<>();

    @OneToMany(mappedBy = "cierreCaja", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CierreCajaLinea> lineas = new ArrayList<>();


    // METHODS

    protected CierreCaja () {
        //JPA
    }

    private CierreCaja (
            Comedor comedor,
            PuntoDeVenta puntoDeVenta,
            LocalDate fechaOperacion,
            Usuario creadoPor,
            Instant creadoEn,
            String observaciones
    ) {
        this.comedor = comedor;
        this.puntoDeVenta = puntoDeVenta;
        this.fechaOperacion = fechaOperacion;
        this.creadoPor = creadoPor;
        this.creadoEn = creadoEn;
        this.estado = EstadoCierreCaja.BORRADOR;
        this.observaciones = trimToNull(observaciones);

        this.lineas = new ArrayList<>();
        this.movimientos = new ArrayList<>();

        this.anuladoPor = null;
        this.anuladoEn = null;
        this.motivoAnulacion = null;
    }

    public static CierreCaja create(
            Comedor comedor,
            PuntoDeVenta puntoDeVenta,
            LocalDate fechaOperacion,
            Usuario creadoPor,
            Instant creadoEn,
            String observaciones
    ) {
        validarDatosCreacion(comedor, puntoDeVenta, fechaOperacion, creadoPor, creadoEn);

        return new CierreCaja(
                comedor,
                puntoDeVenta,
                fechaOperacion,
                creadoPor,
                creadoEn,
                observaciones
        );

    }

    public void anular(Usuario usuario, String motivo) {
        validarDatosAnulacion(usuario, motivo);

        if (this.estado == EstadoCierreCaja.ANULADO) {
            throw new IllegalStateException("Este cierre ya fue anulado.");
        }

        String motivoNormalizado = motivo.trim();

        this.movimientos.stream()
                .filter(m -> !m.estaAnulado())
                .forEach(m -> m.anular(usuario, motivoNormalizado));

        this.estado = EstadoCierreCaja.ANULADO;
        this.anuladoPor = usuario;
        this.anuladoEn = Instant.now();
        this.motivoAnulacion = motivoNormalizado;
    }


    public void cerrar() {
        validarEditable();
        validarConsistenciaLineas();
        this.estado = EstadoCierreCaja.CERRADO;
    }

    public void agregarLinea(CierreCajaLinea linea) {
        validarEditable();
        validarLineaNueva(linea);
        validarClaveUnicaDisponible(linea.getClaveUnicaCierreLinea());

        linea.asignarCierreCaja(this);
        this.lineas.add(linea);
    }

    public void anularLinea(Long lineaId, Usuario usuario, String motivo) {
        validarDatosAnulacion(usuario, motivo);
        validarEditable();
        CierreCajaLinea linea = buscarLineaActivaPorId(lineaId);
        linea.anular(usuario, motivo.trim(), Instant.now());
    }

    public BigDecimal calcularTotalCierre() {
        return this.lineas.stream()
                .filter(CierreCajaLinea::estaActiva)
                .map(CierreCajaLinea::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void actualizarObservaciones(String observaciones) {
        observaciones = trimToNull(observaciones);
        if (observaciones == null || observaciones.isBlank()) {
            throw new BadRequestException("Observaciones no pueden ser vacias o null.");
        }

        this.observaciones = observaciones;
    }



    // PRIVATE HELPER METHODS

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void validarDatosCreacion(
            Comedor comedor,
            PuntoDeVenta puntoDeVenta,
            LocalDate fechaOperacion,
            Usuario creadoPor,
            Instant creadoEn
    ) {
        if (comedor == null) {
            throw new BadRequestException("Comedor no puede ser null.");
        } else if (puntoDeVenta == null) {
            throw new BadRequestException("PuntoDeVenta no puede ser null.");
        } else if (fechaOperacion == null) {
            throw new BadRequestException("fechaOperacion no puede ser null.");
        } else if (creadoPor == null) {
            throw new BadRequestException("Usuario creador no puede ser null.");
        } else if (creadoEn == null) {
            throw new BadRequestException("creadoEn no puede ser null.");
        }
    }

    private void validarEditable() {
        if (this.estado != EstadoCierreCaja.BORRADOR) {
            throw new IllegalStateException("Solo se puede editar un cierre en estado BORRADOR.");
        }
    }

    private void validarDatosAnulacion(Usuario usuario, String motivo) {
        if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        } else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio.");
        }
    }

    private List<CierreCajaLinea> obtenerLineasActivas() {
        return this.lineas.stream()
                .filter(CierreCajaLinea::estaActiva)
                .toList();
    }

    private CierreCajaLinea buscarLineaActivaPorId(Long lineaId) {
        if (lineaId == null) {
            throw new BadRequestException("lineaId no puede ser null.");
        }

        return this.lineas.stream()
                .filter(l -> lineaId.equals(l.getId()))
                .filter(CierreCajaLinea::estaActiva)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Linea activa no encontrada en este cierre."));
    }

    private void validarLineaNueva(CierreCajaLinea linea) {
        if (linea == null) {
            throw new BadRequestException("La linea no puede ser null.");
        } else if (!linea.estaActiva()) {
            throw new BadRequestException("Solo se pueden agregar lineas activas.");
        }
    }

    private void validarClaveUnicaDisponible(String claveUnica) {
        if (claveUnica == null || claveUnica.isBlank()) {
            throw new BadRequestException("claveUnicaCierreLinea no puede estar vacia.");
        }

        boolean existe = this.lineas.stream()
                .filter(CierreCajaLinea::estaActiva)
                .anyMatch(l -> claveUnica.equals(l.getClaveUnicaCierreLinea()));

        if (existe) {
            throw new BadRequestException("Ya existe una linea activa con la misma clave unica.");
        }
    }


    private void validarConsistenciaLineas() {
        List<CierreCajaLinea> activas = obtenerLineasActivas();
        if (activas.isEmpty()) {
            throw new BadRequestException("No se puede cerrar caja sin lineas activas.");
        }

        Set<String> claves = new HashSet<>();
        for (CierreCajaLinea linea : activas) {
            if (linea.getMonto() == null || linea.getMonto().signum() <= 0) {
                throw new BadRequestException("Todas las lineas activas deben tener monto mayor a 0.");
            }
            if (!claves.add(linea.getClaveUnicaCierreLinea())) {
                throw new BadRequestException("Hay lineas activas duplicadas por clave unica.");
            }
        }
    }

    private BigDecimal sumarMontosActivos() {
        return this.lineas.stream()
                .filter(CierreCajaLinea::estaActiva)
                .map(CierreCajaLinea::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}
