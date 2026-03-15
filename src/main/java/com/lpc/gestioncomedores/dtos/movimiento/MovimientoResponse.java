package com.lpc.gestioncomedores.dtos.movimiento;

import com.lpc.gestioncomedores.models.Movimiento;
import com.lpc.gestioncomedores.models.enums.MedioPago;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimientoResponse(
        Long id,
        BigDecimal monto,
        MedioPago medioPago,
        Instant fechaHora,
        Long cierreCajaId,
        Long anulacionId,
        String comentarios
) {

    public MovimientoResponse(Movimiento movimiento) {
        this(
                movimiento.getId(),
                movimiento.getMonto(),
                movimiento.getMedioPago(),
                movimiento.getFechaHora(),
                movimiento.getCierreCaja() != null ? movimiento.getCierreCaja().getId() : null,
                movimiento.getAnulacion() != null ? movimiento.getAnulacion().getId() : null,
                movimiento.getComentarios()
        );
    }
}