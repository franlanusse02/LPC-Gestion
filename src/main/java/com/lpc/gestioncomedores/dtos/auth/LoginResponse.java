package com.lpc.gestioncomedores.dtos.auth;

public record LoginResponse(
        String token,
        Long cuil,
        String nombre,
        String rol) {
}
