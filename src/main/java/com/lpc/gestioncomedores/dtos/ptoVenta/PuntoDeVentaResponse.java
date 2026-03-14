package com.lpc.gestioncomedores.dtos.ptoVenta;

import com.lpc.gestioncomedores.models.PuntoDeVenta;

public record PuntoDeVentaResponse(
        Long id,
        String nombre,
        Long comedorId) {
    public static PuntoDeVentaResponse from(PuntoDeVenta p) {
        return new PuntoDeVentaResponse(
                p.getId(),
                p.getNombre(),
                p.getComedor().getId());
    }
}
