package com.lpc.gestioncomedores.dtos.cajas.responses;

import com.lpc.gestioncomedores.models.enums.EstadoCierreCajaLinea;
import com.lpc.gestioncomedores.models.enums.MedioPago;

import java.math.BigDecimal;
import java.time.Instant;

public record CierreCajaLineaResponse(
        Long id,
        MedioPago medioPago,
        BigDecimal monto,
        EstadoCierreCajaLinea estado,
        Instant anuladoEn,
        Long anuladoPorId,
        String motivoAnulacion
) {
}
