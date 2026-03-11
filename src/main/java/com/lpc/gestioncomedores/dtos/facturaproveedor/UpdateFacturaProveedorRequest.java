package com.lpc.gestioncomedores.dtos.facturaproveedor;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateFacturaProveedorRequest(
        Long comedorId,
        LocalDate fechaFactura,
        @Pattern(regexp = ".*\\S.*", message = "numeroFactura no puede ser un campo vacio (Blank, no Null")
        String numeroFactura,
        Long proveedorId,
        @DecimalMin(value = "0.01", inclusive = true)
        BigDecimal monto,
        MedioPago medioPago,
        String observaciones
) {
}
