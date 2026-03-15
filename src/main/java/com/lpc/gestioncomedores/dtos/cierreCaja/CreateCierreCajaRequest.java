package com.lpc.gestioncomedores.dtos.cierreCaja;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record CreateCierreCajaRequest(
        @NotNull(message = "Id Punto de Venta no puede ser null.") @Positive(message = "Id Punto de Venta debe ser positivo")
        Long puntoVentaId,
        @NotNull(message = "Fecha de Operacion no puede ser null")
        LocalDate fechaOperacion,
        @NotNull(message = "Total de platos vendidos no puede ser null.") @PositiveOrZero(message = "Totl de platos vendidos no puede ser menor a cero.")
        Long totalPlatosVendidos,

        String comentarios) {
}
