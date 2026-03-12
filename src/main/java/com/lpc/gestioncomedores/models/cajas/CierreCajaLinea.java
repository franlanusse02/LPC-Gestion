package com.lpc.gestioncomedores.models.cajas;


import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCajaLinea;
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
        name = "cierre_caja_lineas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cierre_caja_linea",
                columnNames = {
                        "cierre_caja_id",
                        "clave_unica_cierre_linea"
                }
        )
)
public class CierreCajaLinea {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cierre_caja_id", nullable = false)
    private CierreCaja cierreCaja;

    @Column(name = "tipo_venta", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoVentaCaja tipoVenta;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "precio_unitario_menu_snapshot", precision = 19, scale = 2)
    private BigDecimal precioMenuUnitarioSnapshot; //SOLO MENU DEL DIA

    private Boolean cobradoEvento; //SOLO EVENTOS

    @Enumerated(EnumType.STRING)
    private ModoPagoEventoCaja modoPagoEvento; //SOLO EVENTOS

    private String numeroOperacion;

    @Column(name = "numero_orden_evento")
    private String numeroOrdenEvento; //SOLO EVENTOS

    private Integer cantidadPaxEvento; //SOLO EVENTOS

    private String lugarPisoEvento; //SOLO EVENTOS

    @Enumerated(EnumType.STRING) @Column(name = "estado_cierre_linea", nullable = false)
    private EstadoCierreCajaLinea estado = EstadoCierreCajaLinea.ACTIVA;

    @Column(name = "anulado_en")
    private Instant anuladoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id", foreignKey = @ForeignKey(name = "fk_linea_anulada_por"))
    private Usuario anuladoPor;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    @Column(name = "clave_unica_cierre_linea", nullable = false)
    private String claveUnicaCierreLinea;


    // METHODS

    protected CierreCajaLinea () {
        //JPA
    }
    //TODO: CHECK IF REMOVE
    private CierreCajaLinea (
            TipoVentaCaja tipoVenta,
            BigDecimal monto,
            BigDecimal precioMenuUnitarioSnapshot,
            Boolean cobradoEvento,
            ModoPagoEventoCaja modoPagoEvento,
            String numeroOperacion,
            String numeroOrdenEvento,
            Integer cantidadPaxEvento,
            String lugarPisoEvento
    ) {
        this.tipoVenta = tipoVenta;
        this.monto = monto;
        this.precioMenuUnitarioSnapshot = precioMenuUnitarioSnapshot;
        this.cobradoEvento = cobradoEvento;
        this.modoPagoEvento = modoPagoEvento;
        this.numeroOperacion = numeroOperacion;
        this.numeroOrdenEvento = numeroOrdenEvento;
        this.cantidadPaxEvento = cantidadPaxEvento;
        this.lugarPisoEvento = lugarPisoEvento;
    }

    public static CierreCajaLinea create(
            TipoVentaCaja tipoVenta,
            BigDecimal monto,
            BigDecimal precioMenuUnitarioSnapshot,
            Boolean cobradoEvento,
            ModoPagoEventoCaja modoPagoEvento,
            String numeroOperacion,
            String numeroOrdenEvento,
            Integer cantidadPaxEvento,
            String lugarPisoEvento
    ) {
        CierreCajaLinea linea = new CierreCajaLinea();
        linea.tipoVenta = tipoVenta;
        linea.monto = monto;
        linea.precioMenuUnitarioSnapshot = precioMenuUnitarioSnapshot;
        linea.cobradoEvento = cobradoEvento;
        linea.modoPagoEvento = modoPagoEvento;
        linea.numeroOperacion = numeroOperacion;
        linea.numeroOrdenEvento = numeroOrdenEvento;
        linea.cantidadPaxEvento = cantidadPaxEvento;
        linea.lugarPisoEvento = lugarPisoEvento;
        linea.estado = EstadoCierreCajaLinea.ACTIVA;

        linea.normalizarCampos();
        linea.validarSegunTipoVenta();
        linea.validarCamposComunes();
        linea.claveUnicaCierreLinea = linea.construirClaveUnica();


        return linea;
    }

    public void anular(Usuario usuario, String motivo, Instant fechaHora) {
        if (!this.estaActiva()) {
            throw new BadRequestException("CierreCajaLinea tiene que estar activa para anular.");
        } else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null.");
        }else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio o ser null");
        } else if (fechaHora == null) {
            throw new BadRequestException("fechaHora no puede ser de tipo null.");
        }

        archivarClaveUnica(fechaHora);

        this.anuladoEn = fechaHora;
        this.anuladoPor = usuario;
        this.motivoAnulacion = motivo.trim();
        this.estado = EstadoCierreCajaLinea.ANULADA;
    }

    void asignarCierreCaja(CierreCaja cierreCaja) {
        if (cierreCaja == null) {
            throw new BadRequestException("cierreCaja no puede ser null.");
        } else if (this.cierreCaja == cierreCaja) {
            return; // already assigned to same aggregate
        } else if (this.cierreCaja != null) {
            throw new IllegalStateException("La linea ya pertenece a un cierre de caja");
        }

        this.cierreCaja = cierreCaja;
    }

    public Boolean estaActiva() {
        return this.estado == EstadoCierreCajaLinea.ACTIVA;
    }

    //PRIVATE HELPER METHODS

    private void validarCamposComunes() {
        if (this.tipoVenta == null) {
            throw new BadRequestException("tipoVenta no puede ser null");
        }
        validarMontoPositivo();
    }

    private void validarSegunTipoVenta() {
        if (esEvento()) {
            validarCamposEvento();
        } else {
            validarCamposNoEvento();
        }

        if (esMenuDelDia()) {
            validarCamposMenuDelDia();
        }
    }

    private Boolean esEvento() {
        return this.tipoVenta == TipoVentaCaja.EVENTOS;
    }

    private Boolean esMenuDelDia() {
        return this.tipoVenta == TipoVentaCaja.MENU_DEL_DIA;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void normalizarCampos() {
        this.numeroOperacion = trimToNull(this.numeroOperacion);
        this.numeroOrdenEvento = trimToNull(this.numeroOrdenEvento);
        this.lugarPisoEvento = trimToNull(this.lugarPisoEvento);
        this.motivoAnulacion = trimToNull(this.motivoAnulacion);
    }

    private void validarMontoPositivo() {
        if (this.monto == null || this.monto.signum() <= 0) {
            throw new BadRequestException("Monto tiene que ser mayor a 0");
        }
    }

    private void validarCamposEvento() {
        if (!esEvento()) return;

        if (this.cobradoEvento == null) throw new BadRequestException("cobradoEvento no puede ser null en EVENTOS");
        if (this.modoPagoEvento == null) throw new BadRequestException("modoPagoEvento no puede ser null en EVENTOS");
        if (this.numeroOrdenEvento == null || this.numeroOrdenEvento.isBlank()) {
            throw new BadRequestException("numeroOrdenEvento es obligatorio en EVENTOS");
        }
        if (this.cantidadPaxEvento == null || this.cantidadPaxEvento <= 0) {
            throw new BadRequestException("cantidadPaxEvento debe ser mayor a 0 en EVENTOS");
        }
    }

    private void validarCamposNoEvento() {
        if (esEvento()) return;

        if (this.cobradoEvento != null || this.modoPagoEvento != null || this.numeroOrdenEvento != null
                || this.cantidadPaxEvento != null || this.lugarPisoEvento != null) {
            throw new BadRequestException("campos de evento no aplican para tipoVenta " + this.tipoVenta);
        }
    }

    private void validarCamposMenuDelDia() {
        if (!esMenuDelDia()) return;

        if (this.precioMenuUnitarioSnapshot == null || this.precioMenuUnitarioSnapshot.signum() <= 0) {
            throw new BadRequestException("precioMenuUnitarioSnapshot debe ser mayor a 0 en MENU_DEL_DIA");
        }
    }

    private String construirClaveUnica() {
        if (esEvento()) {
            return this.tipoVenta.name() + "|" + this.numeroOrdenEvento;
        }
        return this.tipoVenta.name();
    }

    private void archivarClaveUnica(Instant fechaHora) {
        this.claveUnicaCierreLinea = this.claveUnicaCierreLinea + "|ANULADA|" + fechaHora.toEpochMilli();
    }


}
