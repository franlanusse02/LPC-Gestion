package com.lpc.gestioncomedores.dtos.ptoVenta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePuntoDeVentaRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,

        @NotNull(message = "El id del comedor es obligatorio") Long comedorId) {
}
