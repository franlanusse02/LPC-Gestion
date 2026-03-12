package com.lpc.gestioncomedores.dtos.cajas.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AnularLineaCierreCajaRequest(
        @NotNull @Positive Long lineaId,
        @NotNull @NotBlank String motivo
) {
}
