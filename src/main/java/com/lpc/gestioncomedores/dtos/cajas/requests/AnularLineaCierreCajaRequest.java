package com.lpc.gestioncomedores.dtos.cajas.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnularLineaCierreCajaRequest(
        @NotNull @NotBlank String motivo,
        @NotNull Long lineaId
        ) {
}
