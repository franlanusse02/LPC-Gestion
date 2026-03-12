package com.lpc.gestioncomedores.dtos.sueldos;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReemplazarMovimientoParcialRequest(
        @NotNull Long movimientoIdOriginal,
        @NotNull MedioPago nuevoMedioPago,
        @DecimalMin(value = "0.01") @NotNull BigDecimal nuevoMontoParcial,
        @NotNull @NotBlank String nuevoNumeroOperacion,
        @NotNull @NotBlank String motivo
        ) {}
