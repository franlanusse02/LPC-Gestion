package com.lpc.gestioncomedores.dtos.auth;

public record AuthResponse(
        String token,
        Long cuil,
        String rol) {
}
