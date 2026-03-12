package com.lpc.gestioncomedores.models.movimientos.childs;

import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.models.cajas.CierreCaja;
import com.lpc.gestioncomedores.models.comedores.PuntoDeVenta;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.enums.CategoriaCaja;
import com.lpc.gestioncomedores.models.enums.EstadoMovimientoCaja;
import com.lpc.gestioncomedores.models.enums.MedioPago;
import com.lpc.gestioncomedores.models.enums.Sentido;
import com.lpc.gestioncomedores.models.movimientos.Movimiento;
import com.lpc.gestioncomedores.models.personas.Usuario;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@DiscriminatorValue("CAJA")
public class MovimientoCaja extends Movimiento {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_venta_id")
    private PuntoDeVenta puntoDeVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private CategoriaCaja categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_mov_caja_empleado"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cierre_caja_id")
    private CierreCaja cierreCaja;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_mov_caja")
    private EstadoMovimientoCaja estadoMovimientoCaja = EstadoMovimientoCaja.REGISTRADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mov_caja_anulado_por_id")
    private Usuario anuladoPor;

    @Column(name = "mov_caja_anulado_en")
    private Instant anuladoEn;

    @Column(name = "mov_caja_motivo_anulacion")
    private String motivoAnulacion;


    // METHODS

    public static MovimientoCaja createAporte(
            Comedor comedor,
            PuntoDeVenta puntoDeVenta,
            Usuario usuario,
            BigDecimal monto,
            MedioPago medioPago,
            Instant fechaHora,
            String comentarios
    ) {
        if (comedor == null) {
            throw new BadRequestException("Comedor no puede ser null.");
        } else if (puntoDeVenta == null) {
            throw new BadRequestException("PuntoDeVenta no puede ser null.");
        } else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        } else if (monto == null || monto.signum() <= 0) {
            throw new BadRequestException("Monto debe ser mayor a 0.");
        } else if (medioPago == null) {
            throw new BadRequestException("MedioPago no puede ser null.");
        } else if (fechaHora == null) {
            throw new BadRequestException("fechaHora no puede ser null.");
        }

        MovimientoCaja mov = new MovimientoCaja();
        mov.setComedor(comedor);
        mov.puntoDeVenta = puntoDeVenta;
        mov.usuario = usuario;
        mov.categoria = CategoriaCaja.APORTE;
        mov.setMonto(monto);
        mov.setMedioPago(medioPago);
        mov.setFechaHora(fechaHora);
        mov.setSentido(Sentido.INGRESO);
        mov.setComentarios(comentarios != null ? comentarios.trim() : null);
        mov.cierreCaja = null;
        mov.estadoMovimientoCaja = EstadoMovimientoCaja.REGISTRADO;

        return mov;
    }

    public static MovimientoCaja createDesdeCierre(
            CierreCaja cierreCaja,
            Comedor comedor,
            PuntoDeVenta puntoDeVenta,
            Usuario usuario,
            BigDecimal monto,
            MedioPago medioPago,
            Instant fechaHora,
            String comentarios
    ) {
        if (cierreCaja == null) {
            throw new BadRequestException("cierreCaja no puede ser null.");
        } else if (comedor == null) {
            throw new BadRequestException("Comedor no puede ser null.");
        } else if (puntoDeVenta == null) {
            throw new BadRequestException("PuntoDeVenta no puede ser null.");
        } else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        } else if (monto == null || monto.signum() <= 0) {
            throw new BadRequestException("Monto debe ser mayor a 0.");
        } else if (medioPago == null) {
            throw new BadRequestException("MedioPago no puede ser null.");
        } else if (fechaHora == null) {
            throw new BadRequestException("fechaHora no puede ser null.");
        }

        MovimientoCaja mov = new MovimientoCaja();
        mov.setComedor(comedor);
        mov.puntoDeVenta = puntoDeVenta;
        mov.usuario = usuario;
        mov.categoria = CategoriaCaja.CIERRE;
        mov.cierreCaja = cierreCaja;
        mov.setMonto(monto);
        mov.setMedioPago(medioPago);
        mov.setFechaHora(fechaHora);
        mov.setSentido(Sentido.INGRESO);
        mov.setComentarios(comentarios != null ? comentarios.trim() : null);
        mov.estadoMovimientoCaja = EstadoMovimientoCaja.REGISTRADO;

        return mov;
    }


    public void anular(Usuario usuario, String motivo) {
        if (this.estadoMovimientoCaja == EstadoMovimientoCaja.ANULADO) {
            throw new IllegalStateException("No se puede anular un movimiento previamente anulado.");
        } else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        } else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio.");
        }

        this.estadoMovimientoCaja = EstadoMovimientoCaja.ANULADO;
        this.anuladoPor = usuario;
        this.anuladoEn = Instant.now();
        this.motivoAnulacion = motivo.trim();
    }

    public boolean estaAnulado() {
        return this.estadoMovimientoCaja == EstadoMovimientoCaja.ANULADO;
    }


}
