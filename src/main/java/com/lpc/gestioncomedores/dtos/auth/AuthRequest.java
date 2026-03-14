package com.lpc.gestioncomedores.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// ── Request: login ──────────────────────────────────────────────────────────

public class AuthRequest {

    @NotNull(message = "El CUIL es obligatorio")
    private Long cuil;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public Long getCuil() {
        return cuil;
    }

    public String getPassword() {
        return password;
    }
}
