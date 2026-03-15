package com.lpc.gestioncomedores.dtos.ptoVenta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePuntoDeVentaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotNull(message = "El id del comedor es obligatorio") @Positive(message = "Id de comedor debe ser positivo.")
        Long comedorId) {
}
