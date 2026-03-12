package com.lpc.gestioncomedores.dtos.cajas.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnularCierreCajaRequest(
        @NotNull @NotBlank String motivo
) {
}
