package com.lpc.gestioncomedores.dtos.movimiento;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateMovimientoRequest(
        @NotNull(message = "Monto no puede ser null") @DecimalMin("0.01")
        BigDecimal monto,

        @NotNull(message = "Medio de pago no puede ser null.")
        MedioPago medioPago,

        @NotNull(message = "Cierre de Caja Id no puede ser null.") @Positive(message = "Cierre de Caja Id debe ser positivo")
        Long cierreCajaId,

        String comentarios
) {};