package com.lpc.gestioncomedores.dtos.comedor;

import com.lpc.gestioncomedores.dtos.ptoVenta.PuntoDeVentaResponse;
import com.lpc.gestioncomedores.models.Comedor;

import java.util.List;

public record ComedorResponse(
        Long id,
        String name,
        List<PuntoDeVentaResponse> puntosDeVenta) {
    public static ComedorResponse from(Comedor c) {
        return new ComedorResponse(
                c.getId(),
                c.getName(),
                c.getPuntosDeVenta().stream()
                        .map(PuntoDeVentaResponse::from)
                        .toList());
    }
}
