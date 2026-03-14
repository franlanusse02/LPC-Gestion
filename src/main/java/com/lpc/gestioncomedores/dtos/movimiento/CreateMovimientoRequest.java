package com.lpc.gestioncomedores.dtos.movimiento;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import java.math.BigDecimal;

public record CreateMovimientoRequest(
        BigDecimal monto,
        MedioPago medioPago,
        Long cierreCajaId,
        String comentarios
) {};