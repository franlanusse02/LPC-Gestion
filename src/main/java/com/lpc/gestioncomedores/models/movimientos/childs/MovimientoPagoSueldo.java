package com.lpc.gestioncomedores.models.movimientos.childs;


import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.models.enums.EstadoMovimientoPagoSueldo;
import com.lpc.gestioncomedores.models.movimientos.Movimiento;
import com.lpc.gestioncomedores.models.personas.Usuario;
import com.lpc.gestioncomedores.models.sueldos.PagoSueldo;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
@DiscriminatorValue("PAGO_SUELDO")
public class MovimientoPagoSueldo extends Movimiento {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_sueldo_id",
            foreignKey = @ForeignKey(name = "fk_mov_sueldo_pago"))
    private PagoSueldo pagoSueldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMovimientoPagoSueldo estadoMovimientoPagoSueldo = EstadoMovimientoPagoSueldo.REGISTRADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id")
    private Usuario anuladoPor;

    private Instant anuladoEn;

    private String motivoAnulacion;

    @Column(name = "numero_operacion")
    private String numeroOperacion;




    public void setPagoSueldo(PagoSueldo pagoSueldo) {
        if (pagoSueldo == null) {
            throw new IllegalArgumentException("pagoSueldo no puede ser null");
        }
        this.pagoSueldo = pagoSueldo;
    }

    public void anular(Usuario usuario, String motivo, Instant fechaHora) {

        if (this.estadoMovimientoPagoSueldo == EstadoMovimientoPagoSueldo.ANULADO) {
            throw new IllegalStateException("No se puede anular un movimiento previamente anulado.");
        }else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        }else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio.");
        }else if (fechaHora == null) {
            throw new BadRequestException("Fecha de anulacion no puede ser null.");
        }

        this.estadoMovimientoPagoSueldo = EstadoMovimientoPagoSueldo.ANULADO;
        this.anuladoPor = usuario;
        this.anuladoEn = fechaHora;
        this.motivoAnulacion = motivo.trim();

    }

    public boolean estaAnulado() {
        return this.estadoMovimientoPagoSueldo == EstadoMovimientoPagoSueldo.ANULADO;
    }

    public void setNumeroOperacion(String numeroOperacion) {
        this.numeroOperacion = numeroOperacion;
    }



}
