package com.lpc.gestioncomedores.models.cajas;


import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCajaLinea;
import com.lpc.gestioncomedores.models.enums.MedioPago;
import com.lpc.gestioncomedores.models.personas.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
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


    public static CierreCajaLinea crear(
            BigDecimal monto,
            MedioPago medioPago
    ){
        if (medioPago == null) {
            throw new BadRequestException("Medio de pago no puede ser null");
        }
        validarMontoPositivo(monto);

        return new CierreCajaLinea(monto, medioPago);
    }

    public void anularLinea(Usuario usuario, String motivo){
        if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        } else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio.");
        } else if (this.estado == EstadoCierreCajaLinea.ANULADA) {
            throw new IllegalStateException("No se puede anular una linea ya anulada.");
        }

        this.estado = EstadoCierreCajaLinea.ANULADA;
        this.anuladoPor = usuario;
        this.motivoAnulacion = trimToNull(motivo);
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

    public boolean estaActivaLinea(){
        return this.estado == EstadoCierreCajaLinea.ACTIVA;
    }

    public void actualizarMonto(BigDecimal monto) {
        validarMontoPositivo(monto);
        this.monto = monto;
    }


    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void validarMontoPositivo(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new BadRequestException("Monto debe ser mayor a 0.");
        }
    }




}
