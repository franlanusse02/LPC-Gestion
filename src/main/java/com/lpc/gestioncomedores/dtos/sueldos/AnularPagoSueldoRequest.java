package com.lpc.gestioncomedores.dtos.sueldos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnularPagoSueldoRequest(
        @NotNull @NotBlank String motivo
) {
}
