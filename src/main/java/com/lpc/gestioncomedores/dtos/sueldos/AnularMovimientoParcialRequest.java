package com.lpc.gestioncomedores.dtos.sueldos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnularMovimientoParcialRequest(
        @NotNull Long movimientoId,
        @NotNull @NotBlank String motivo
) {
}
