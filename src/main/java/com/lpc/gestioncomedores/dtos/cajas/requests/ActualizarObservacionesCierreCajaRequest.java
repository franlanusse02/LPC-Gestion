package com.lpc.gestioncomedores.dtos.cajas.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActualizarObservacionesCierreCajaRequest(
        @NotNull @NotBlank String observaciones
) {
}
