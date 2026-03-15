package com.lpc.gestioncomedores.dtos.ptoVenta;

import com.lpc.gestioncomedores.dtos.cierreCaja.CierreCajaResponse;

import java.util.List;

public record PuntoDeVentaDetailedResponse(
        Long id,
        String nombre,
        Long comedorId,
        List<CierreCajaResponse> cierres
) {
}
