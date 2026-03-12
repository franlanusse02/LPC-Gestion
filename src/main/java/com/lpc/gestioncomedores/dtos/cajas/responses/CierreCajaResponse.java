package com.lpc.gestioncomedores.dtos.cajas.responses;

import com.lpc.gestioncomedores.models.enums.EstadoCierreCaja;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CierreCajaResponse(
        Long id,
        Long comedorId,
        Long puntoVentaId,
        LocalDate fechaOperacion,
        Instant creadoEn,
        Long creadoPorId,
        EstadoCierreCaja estado,
        String observaciones,
        Instant anuladoEn,
        Long anuladoPorId,
        String motivoAnulacion,
        BigDecimal totalCierre,
        List<CierreCajaLineasResponse> lineas,
        List<MovimientoCajaResponse> movimientos

) {
}
