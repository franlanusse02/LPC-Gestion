package com.lpc.gestioncomedores.dtos.comedor;

import com.lpc.gestioncomedores.models.Comedor;

public record ComedorCompressedResponse(
        Long id,
        String nombre
) {
    public static ComedorCompressedResponse from(Comedor c) {
        return new ComedorCompressedResponse(
                c.getId(),
                c.getName()
        );
    }
}
