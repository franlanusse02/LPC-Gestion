package com.lpc.gestioncomedores.dtos.sueldos;

import com.lpc.gestioncomedores.models.enums.ContratoEmpleado;
import com.lpc.gestioncomedores.models.enums.EstadoPagoSueldo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PagoSueldoResponse(
        Long id,
        Long empleadoId,
        Long comedorId,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        ContratoEmpleado contrato,
        String funcionEmpleado,
        LocalDate fechaPago,
        BigDecimal montoTotal,
        EstadoPagoSueldo estado,
        String observaciones,
        Instant creadoEn,
        List<MovimientoPagoSueldoResponse> movimientos
) {
}
