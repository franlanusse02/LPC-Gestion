package com.lpc.gestioncomedores.dtos.cajas.responses;

import com.lpc.gestioncomedores.models.enums.EstadoCierreCajaLinea;
import com.lpc.gestioncomedores.models.enums.ModoPagoEventoCaja;
import com.lpc.gestioncomedores.models.enums.TipoVentaCaja;

import java.math.BigDecimal;
import java.time.Instant;

public record CierreCajaLineasResponse(
        Long id,
        TipoVentaCaja tipoVenta,
        BigDecimal monto,
        BigDecimal precioMenuUnitarioSnapshot,
        Boolean cobradoEvento,
        ModoPagoEventoCaja modoPagoEvento,
        String numeroOperacion,
        String numeroOrdenEvento,
        Integer cantidadPaxEvento,
        String lugarPisoEvento,
        EstadoCierreCajaLinea estado,
        String claveUnicaCierreLinea,
        Instant anuladoEn,
        Long anuladoPorId,
        String motivoAnulacion
) {
}
