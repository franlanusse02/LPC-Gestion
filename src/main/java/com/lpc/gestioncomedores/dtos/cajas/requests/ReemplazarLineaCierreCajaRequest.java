package com.lpc.gestioncomedores.dtos.cajas.requests;

import com.lpc.gestioncomedores.models.enums.ModoPagoEventoCaja;
import com.lpc.gestioncomedores.models.enums.TipoVentaCaja;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReemplazarLineaCierreCajaRequest(
        @NotNull @Positive Long lineaIdOriginal,
        @NotNull TipoVentaCaja tipoVenta,
        @NotNull @DecimalMin("0.01") BigDecimal monto,
        @DecimalMin("0.01") BigDecimal precioMenuUnitarioSnapshot,
        Boolean cobradoEvento,
        ModoPagoEventoCaja modoPagoEvento,
        String numeroOperacion,
        String numeroOrdenEvento,
        @Positive Integer cantidadPaxEvento,
        String lugarPisoEvento,
        @NotNull @NotBlank String motivo
) {
}
