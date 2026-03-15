package com.lpc.gestioncomedores.dtos.cierreCaja;

import com.lpc.gestioncomedores.dtos.auth.UsuarioResponse;
import com.lpc.gestioncomedores.models.utils.Anulacion;
import com.lpc.gestioncomedores.models.utils.AnulacionCierre;

import java.time.Instant;

public record AnulacionCierreResponse(
        Long id,
        UsuarioResponse anuladoPor,
        Instant fechaAnulacion,
        String motivoAnulacion
) {
    public AnulacionCierreResponse(AnulacionCierre anulacion) {
        this(
                anulacion.getId(),
                UsuarioResponse.from(anulacion.getAnuladoPor()),
                anulacion.getFechaAnulacion(),
                anulacion.getMotivo()
        );
    }
}
