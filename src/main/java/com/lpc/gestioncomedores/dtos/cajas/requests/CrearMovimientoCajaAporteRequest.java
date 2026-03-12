package com.lpc.gestioncomedores.dtos.cajas.requests;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CrearMovimientoCajaAporteRequest(
        @NotNull @Positive Long comedorId,
        @NotNull @Positive Long puntoVentaId,
        @NotNull @DecimalMin("0.01")BigDecimal monto,
        @NotNull MedioPago medioPago,
        String comentarios
        ) {
}
