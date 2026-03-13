package com.lpc.gestioncomedores.dtos.cajas.requests;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AgregarLineaCierreCajaRequest(
        @NotNull MedioPago medioPago,
        @NotNull @Positive BigDecimal monto
        ) {
}
