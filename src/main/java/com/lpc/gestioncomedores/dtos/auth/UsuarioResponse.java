package com.lpc.gestioncomedores.dtos.auth;

import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import com.lpc.gestioncomedores.models.Usuario;

public record UsuarioResponse(
        Long cuil,
        String name,
        UsuarioRol rol) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getCuil(), u.getName(), u.getRol());
    }
}
