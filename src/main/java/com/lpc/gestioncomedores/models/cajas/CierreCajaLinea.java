package com.lpc.gestioncomedores.models.cajas;


import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCajaLinea;
import com.lpc.gestioncomedores.models.enums.MedioPago;
import com.lpc.gestioncomedores.models.enums.ModoPagoEventoCaja;
import com.lpc.gestioncomedores.models.enums.TipoVentaCaja;
import com.lpc.gestioncomedores.models.personas.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@AllArgsConstructor
@Getter
@Table(
        name = "cierre_caja_lineas"
)
public class CierreCajaLinea {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cierre_caja_id", nullable = false)
    private CierreCaja cierreCaja;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false)
    private MedioPago medioPago;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING) @Column(name = "estado_cierre_linea", nullable = false)
    private EstadoCierreCajaLinea estado = EstadoCierreCajaLinea.ACTIVA;

    @Column(name = "anulado_en")
    private Instant anuladoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id", foreignKey = @ForeignKey(name = "fk_linea_anulada_por"))
    private Usuario anuladoPor;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;



    // METHODS

    protected CierreCajaLinea () {
        //JPA
    }
    //TODO: CHECK IF REMOVE
    private CierreCajaLinea (
            BigDecimal monto,
            MedioPago medioPago
    ) {
        this.monto = monto;
        this.medioPago = medioPago;

        this.estado = EstadoCierreCajaLinea.ACTIVA;

        this.anuladoEn = null;
        this.anuladoPor = null;
        this.motivoAnulacion = null;
    }


    public CierreCajaLinea crear(
            BigDecimal monto,
            MedioPago medioPago
    ){
        if (monto == null) {
            throw new BadRequestException("Monto no puede ser null.");
        }else if (medioPago == null) {
            throw new BadRequestException("Medio de pago no puede ser null");
        }

        return new CierreCajaLinea(monto, medioPago);
    }

    public void anular(Usuario usuario, String motivo){
        if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        } else if (motivo == null) {
            throw new BadRequestException("Motivo no puede ser null");
        }

        this.estado = EstadoCierreCajaLinea.ANULADA;
        this.anuladoPor = usuario;
        this.motivoAnulacion = motivo;
        this.anuladoEn = Instant.now();
    }

    public void asignarCierreCaja(CierreCaja cierreCaja) {
        if (cierreCaja == null) {
            throw new BadRequestException("Cierre de caja no puede ser null");
        } else if (this.cierreCaja != null) {
            throw new IllegalStateException("Esta linea ya pertenece a un cierre de caja.");
        }

        this.cierreCaja = cierreCaja;


    }

    public boolean estaActiva(){
        return this.estado == EstadoCierreCajaLinea.ACTIVA;
    }

    public void actualizarMonto(BigDecimal monto) {
        if (monto == null) {
            throw new BadRequestException("Monto no puede ser null.");
        }

        this.monto = monto;
    }


    public String trimToNull(String string) {

    }





}
