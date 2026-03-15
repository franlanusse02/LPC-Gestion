package com.lpc.gestioncomedores.dtos.cierreCaja;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PatchCierreCajaRequest(
        @NotNull
        LocalDate fechaOperacion,
        @NotNull @Positive
        Long comedorId,
        @NotNull @Positive
        Long puntoDeVentaId,
        @NotNull @Positive
        Long totalPlatosVendidos,
        String comentarios
) {
}
