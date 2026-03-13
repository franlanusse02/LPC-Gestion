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
            Instant creadoEn,
            String observaciones
    ) {
        this.comedor = comedor;
        this.puntoDeVenta = puntoDeVenta;
        this.fechaOperacion = fechaOperacion;
        this.creadoPor = creadoPor;
        this.creadoEn = creadoEn;
        this.estado = EstadoCierreCaja.BORRADOR;
        this.observaciones = observaciones;

        this.lineas = new ArrayList<>();
        this.movimientos = new ArrayList<>();

        this.anuladoPor = null;
        this.anuladoEn = null;
        this.motivoAnulacion = null;
    }
}
