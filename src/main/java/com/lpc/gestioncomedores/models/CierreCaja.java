package com.lpc.gestioncomedores.models;

import com.lpc.gestioncomedores.exceptions.BadRequestException;

import com.lpc.gestioncomedores.models.utils.AnulacionCierre;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cierres_caja")
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoDeVenta puntoDeVenta;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario creadoPor;

    @Column(name = "total_platos_vendidos")
    private Long totalPlatosVendidos;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    private String comentarios = "";

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private AnulacionCierre anulacion;

    @OneToMany(mappedBy = "cierreCaja", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Movimiento> movimientos = new ArrayList<>();

    // Main Constructor
    public CierreCaja(PuntoDeVenta puntoDeVenta, LocalDate fechaOperacion, Usuario creadoPor, Long totalPlatosVendidos, String comentarios) {
        this.puntoDeVenta = puntoDeVenta;
        this.fechaOperacion = fechaOperacion;
        this.creadoPor = creadoPor;
        this.totalPlatosVendidos = totalPlatosVendidos;
        this.comentarios = comentarios;
    }

    // METHODS
    public void actualizarTotalPlatosVendidos(Long totalPlatosVendidos) {
        if (totalPlatosVendidos != null && totalPlatosVendidos < 0) {
            throw new BadRequestException("Total de platos vendidos no puede ser menor a 0");
        }

        this.totalPlatosVendidos = totalPlatosVendidos;
    }

    public void agregarMovimiento(Movimiento movimiento) {
        if (this.movimientos.stream()
                .anyMatch(m -> movimiento.getMedioPago() == m.getMedioPago())) {
            throw new IllegalStateException("Cierre ya posee una linea activa con ese medio de pago.");
        }

        movimiento.setCierreCaja(this);
        this.movimientos.add(movimiento);
    }

    public BigDecimal calcularMontoTotal() {
        return this.movimientos.stream()
                .filter(m -> m.getAnulacion() == null)
                .map(Movimiento::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void anularCierre(Usuario anuladoPor, String motivoAnulacion) {
        if (motivoAnulacion == null || motivoAnulacion.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio");
        }

        for (Movimiento movimiento : this.movimientos) {
            if (movimiento.getAnulacion() == null) {
                movimiento.anularMovimiento(motivoAnulacion, anuladoPor);
            }
        }
        AnulacionCierre anulacion = new AnulacionCierre();
        anulacion.setFechaAnulacion(Instant.now());
        anulacion.setMotivo(motivoAnulacion);
        anulacion.setAnuladoPor(anuladoPor);

        this.anulacion = anulacion;
    }


}

