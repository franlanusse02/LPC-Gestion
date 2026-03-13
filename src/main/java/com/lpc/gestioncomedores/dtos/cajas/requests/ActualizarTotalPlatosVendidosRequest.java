package com.lpc.gestioncomedores.dtos.cajas.requests;

import jakarta.validation.constraints.PositiveOrZero;

public record ActualizarTotalPlatosVendidosRequest(
        @PositiveOrZero Integer totalPlatosVendidos
) {
}
