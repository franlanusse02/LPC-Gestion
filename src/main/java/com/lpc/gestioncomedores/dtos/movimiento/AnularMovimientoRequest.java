package com.lpc.gestioncomedores.dtos.movimiento;

import jakarta.validation.constraints.NotNull;

public record AnularMovimientoRequest(
        @NotNull String motivo
) {
}
