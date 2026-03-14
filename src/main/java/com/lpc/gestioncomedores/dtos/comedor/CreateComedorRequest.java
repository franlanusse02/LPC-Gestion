package com.lpc.gestioncomedores.dtos.comedor;

import jakarta.validation.constraints.NotBlank;

public record CreateComedorRequest(
        @NotBlank(message = "El nombre es obligatorio") String name) {
}
