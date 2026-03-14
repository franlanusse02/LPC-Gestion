package com.lpc.gestioncomedores.dtos.auth;

import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegisterRequest(

        @NotNull(message = "El CUIL es obligatorio") @Positive Long cuil,

        @NotNull(message = "El nombre es obligatorio") String nombre,

        @NotNull(message = "El rol es obligatorio") UsuarioRol rol,

        @NotBlank(message = "La contraseña es obligatoria") String password) {
}
