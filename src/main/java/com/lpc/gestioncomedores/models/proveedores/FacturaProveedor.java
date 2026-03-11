package com.lpc.gestioncomedores.models.proveedores;

import com.lpc.gestioncomedores.models.admin.Banco;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.enums.EstadoFacturaProveedor;
import com.lpc.gestioncomedores.models.enums.MedioPago;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoPagoProveedor;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.*;

@Entity
@Getter
@AllArgsConstructor
@Table(name = "facturas_proveedores")
public class FacturaProveedor {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Relaciona la factura a un proveedor (ManyToOne -> FC - Proveedor)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "numero_factura", nullable = false)
    private String numeroFactura;

    // -----------------------
    // FECHAS ----------------

    //Estado: PENDIENTE
    @Column(name = "fecha_factura", nullable = false)
    private LocalDate fechaFactura;

    //Estado: PENDIENTE -> APROBADO
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    //Estado: APROBADO -> PAGADO
    @Column(name = "fecha_pago_provisoria")
    private LocalDate fechaPagoProvisoria;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    // FECHAS ----------------
    // -----------------------


    @OneToOne(mappedBy = "factura")
    private MovimientoPagoProveedor pago;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoFacturaProveedor estado = EstadoFacturaProveedor.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_pagador_id")
    private Banco bancoPagador;

    @Column(name = "medio_de_pago", nullable = false)
    @Enumerated(EnumType.STRING)
    private MedioPago medioPago;

    @Column(nullable = false)
    private BigDecimal monto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    private String observaciones;



    // ----------------------
    // METHODS --------------
    // ----------------------

    protected FacturaProveedor() {
        //JPA
    }

    private FacturaProveedor(
            Proveedor proveedor,
            Comedor comedor,
            String numeroFactura,
            LocalDate fechaFactura,
            BigDecimal monto,
            MedioPago medioPago,
            String observaciones
    ) {
        this.proveedor = proveedor;
        this.comedor = comedor;
        this.numeroFactura = numeroFactura;
        this.fechaFactura = fechaFactura;
        this.monto = monto;
        this.medioPago = medioPago;
        this.observaciones = observaciones;
    }

    public static FacturaProveedor create(
            Proveedor proveedor,
            Comedor comedor,
            String numeroFactura,
            LocalDate fechaFactura,
            BigDecimal monto,
            MedioPago medioPago,
            String observaciones
    ) {
        return new FacturaProveedor(proveedor, comedor, numeroFactura, fechaFactura, monto, medioPago, observaciones);
    }

    public void applyReviewPatch(
            Proveedor proveedor,
            Comedor comedor,
            String numeroFactura,
            LocalDate fechaFactura,
            BigDecimal monto,
            MedioPago medioPago,
            String observaciones
    ) {
        if (proveedor != null) this.proveedor = proveedor;
        if (comedor != null) this.comedor = comedor;
        if (numeroFactura != null) this.numeroFactura = numeroFactura;
        if (fechaFactura != null) this.fechaFactura = fechaFactura;
        if (monto != null) this.monto = monto;
        if (medioPago != null) this.medioPago = medioPago;
        if (observaciones != null) this.observaciones = observaciones;
    }

    public void approvePayment (
            LocalDate fechaPagoProvisoria,
            LocalDate fechaEmision,
            Banco bancoPagador,
            String observaciones
    ) {
        if (fechaPagoProvisoria != null) this.fechaPagoProvisoria = fechaPagoProvisoria;
        this.bancoPagador = bancoPagador;
        this.fechaEmision = fechaEmision;
        if (observaciones != null) this.observaciones = observaciones;
        this.estado = EstadoFacturaProveedor.EMITIDO;
    }

    public void markAsPaid(
            LocalDate fechaPago,
            String observaciones
    ){
        LocalDate fechaFinal = (fechaPago != null) ? fechaPago : this.fechaPagoProvisoria;
        if (fechaFinal == null) {
            throw new IllegalStateException("No se puede marcar como pago una factura sin fecha de pago.");
        }

        this.fechaPago = fechaFinal;
        this.estado = EstadoFacturaProveedor.PAGADO;
        if (observaciones != null) this.observaciones = observaciones;
    }


    public void cancelPayment(
            String observaciones
    ) {
        this.estado = EstadoFacturaProveedor.CANCELADO;
        if (observaciones != null) this.observaciones = observaciones;
    }

}
