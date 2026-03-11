package com.lpc.gestioncomedores.dtos.sueldos;

import com.lpc.gestioncomedores.models.enums.ContratoEmpleado;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePagoSueldoRequest(
        @NotNull Long empleadoId,
        @NotNull Long comedorId,
        @NotNull LocalDate periodoInicio,
        @NotNull LocalDate periodoFin,
        @NotNull ContratoEmpleado contrato,
        @NotNull @NotBlank String funcionEmpleado,
        @NotNull LocalDate fechaPago,
        @DecimalMin(value = "0.01", inclusive = true) @NotNull
        BigDecimal montoTotal,
        String observaciones
) {
}
