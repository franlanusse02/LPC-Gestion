package com.lpc.gestioncomedores.dtos.movimiento;

import com.lpc.gestioncomedores.dtos.auth.UsuarioResponse;
import com.lpc.gestioncomedores.models.utils.Anulacion;

import java.time.Instant;

public record AnulacionMovimientoResponse(
        Long id,
        UsuarioResponse anuladoPor,
        Instant fechaAnulacion,
        String motivoAnulacion
) {
    public AnulacionMovimientoResponse(Anulacion anulacion) {
        this(
                anulacion.getId(),
                UsuarioResponse.from(anulacion.getAnuladoPor()),
                anulacion.getFechaAnulacion(),
                anulacion.getMotivo()
        );
    }
}
