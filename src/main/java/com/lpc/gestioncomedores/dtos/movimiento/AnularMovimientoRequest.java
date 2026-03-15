package com.lpc.gestioncomedores.dtos.movimiento;

import jakarta.validation.constraints.NotBlank;

public record AnularMovimientoRequest(
        @NotBlank(message = "Motivo no puede estar vacio.") String motivo
) {
}
