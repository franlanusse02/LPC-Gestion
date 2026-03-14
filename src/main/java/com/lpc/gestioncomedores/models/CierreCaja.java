package com.lpc.gestioncomedores.models;

import com.lpc.gestioncomedores.exceptions.BadRequestException;
import com.lpc.gestioncomedores.exceptions.NotFoundException;
import com.lpc.gestioncomedores.models.Comedor;
import com.lpc.gestioncomedores.models.PuntoDeVenta;
import com.lpc.gestioncomedores.models.Usuario;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCaja;
import com.lpc.gestioncomedores.models.utils.Anulacion;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCierreCaja estado = EstadoCierreCaja.PENDIENTE;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    private String comentarios = "";

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Anulacion anulacion;

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
        if (this.estado != EstadoCierreCaja.PENDIENTE) {
            throw new IllegalStateException("No se pueden modificar cierres que no esten en estado PENDIENTE");
        } else if (totalPlatosVendidos != null && totalPlatosVendidos < 0) {
            throw new BadRequestException("Total de platos vendidos no puede ser menor a 0");
        }

        this.totalPlatosVendidos = totalPlatosVendidos;
    }

    public void actualizarComentarios(String observaciones) {
        if (this.estado != EstadoCierreCaja.PENDIENTE) {
            throw new IllegalStateException("No se pueden modificar cierres que no esten en estado PENDIENTE");
        }
        this.comentarios = observaciones;
    }

    public void agregarMovimiento(Movimiento movimiento) {
        if (this.estado == EstadoCierreCaja.ANULADO || this.estado == EstadoCierreCaja.PROCESADO) {
            throw new IllegalStateException("Solo se pueden modificar cierres en estado PENDIENTE");
        }

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
                .map(m -> m.getMonto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void procesarCierre() {
        if (this.estado != EstadoCierreCaja.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden finalizar cierres en estado pendiente.");
        }
        this.estado = EstadoCierreCaja.PROCESADO;
    }
}
