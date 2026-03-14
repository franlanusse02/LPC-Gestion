package com.lpc.gestioncomedores.dtos.auth;

import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(

        @NotNull(message = "El CUIL es obligatorio") Long cuil,

        @NotNull(message = "El rol es obligatorio") UsuarioRol rol,

        @NotBlank(message = "La contraseña es obligatoria") String password) {
}
