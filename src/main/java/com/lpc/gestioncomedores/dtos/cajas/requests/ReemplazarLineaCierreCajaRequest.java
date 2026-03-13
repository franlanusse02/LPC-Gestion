package com.lpc.gestioncomedores.dtos.cajas.requests;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReemplazarLineaCierreCajaRequest (
        @NotNull Long lineaViejaId,
        @NotNull @NotBlank String motivo,
        @NotNull @Positive BigDecimal monto,
        @NotNull MedioPago medioPago
        ){
}
