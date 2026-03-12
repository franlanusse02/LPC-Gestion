package com.lpc.gestioncomedores.dtos.sueldos;

import com.lpc.gestioncomedores.models.enums.EstadoMovimientoPagoSueldo;
import com.lpc.gestioncomedores.models.enums.MedioPago;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimientoPagoSueldoResponse(
        Long id,
        MedioPago medioPago,
        BigDecimal monto,
        Instant fechaHora,
        String numeroOperacion,
        EstadoMovimientoPagoSueldo estadoMovimiento
) {
}
