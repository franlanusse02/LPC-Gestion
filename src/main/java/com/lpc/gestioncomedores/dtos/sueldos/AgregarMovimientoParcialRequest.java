package com.lpc.gestioncomedores.dtos.sueldos;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AgregarMovimientoParcialRequest(
        @NotNull MedioPago medioPago,
        @DecimalMin(value = "0.01") @NotNull BigDecimal montoParcial,
        @NotNull @NotBlank String numeroOperacion
        ) {
}
