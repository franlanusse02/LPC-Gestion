package com.lpc.gestioncomedores.dtos.auth;

public record LoginResponse(
        String token,
        Long cuil,
        String name,
        String rol) {
}
