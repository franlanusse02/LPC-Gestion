package com.lpc.gestioncomedores.dtos.cajas.requests;

import com.lpc.gestioncomedores.models.enums.ModoPagoEventoCaja;
import com.lpc.gestioncomedores.models.enums.TipoVentaCaja;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AgregarLineaCierreCajaRequest(
        @NotNull TipoVentaCaja tipoVenta,
        @NotNull @DecimalMin("0.01") BigDecimal monto,
        @DecimalMin("0.01") BigDecimal precioMenuUnitarioSnapshot,
        Boolean cobradoEvento,
        ModoPagoEventoCaja modoPagoEvento,
        String numeroOperacion,
        String numeroOrdenEvento,
        @Positive Integer cantidadPaxEvento,
        String lugarPisoEvento
        ) {
}
