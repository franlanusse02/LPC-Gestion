package com.lpc.gestioncomedores.dtos.comedor;

import com.lpc.gestioncomedores.dtos.ptoVenta.PuntoDeVentaDetailedResponse;

import java.util.List;

public record ComedorDetailedResponse(
        Long id,
        String nombre,
        List<PuntoDeVentaDetailedResponse> puntosDeVenta
) {
}
