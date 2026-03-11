package com.lpc.gestioncomedores.dtos.facturaproveedor;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFacturaProveedorRequest(

        @NotNull(message = "Factura debe tener un comedor asociado.")
        Long comedorId,

        @NotNull (message = "Factura debe tener una fecha de factura.")
        LocalDate fechaFactura,

        @NotBlank(message = "Factura debe tener un numero de factura.")
        String numeroFactura,

        @NotNull(message = "Factura debe tener un proveedor asociado.")
        Long proveedorId,

        @NotNull(message = "Factura debe tener un monto.")
        @DecimalMin(value = "0.01", inclusive = true)
        BigDecimal monto,

        @NotNull(message = "Factura debe tener un medio de pago.")
        MedioPago medioPago,

        String observaciones
) {}
