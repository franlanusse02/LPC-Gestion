package com.lpc.gestioncomedores.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

// ── Request: login ──────────────────────────────────────────────────────────

@Getter
public class LoginRequest {

    @NotNull(message = "El CUIL es obligatorio") @Positive
    private Long cuil;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

}
