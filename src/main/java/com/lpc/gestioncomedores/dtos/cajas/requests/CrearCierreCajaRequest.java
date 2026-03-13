package com.lpc.gestioncomedores.dtos.cajas.requests;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CrearCierreCajaRequest(
        @NotNull Long comedorId,
        @NotNull Long puntoVentaId,
        @NotNull LocalDate fechaOperacion,
        String observaciones,
        @PositiveOrZero Integer totalPlatosVendidos
        ) {
}
