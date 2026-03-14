package com.lpc.gestioncomedores.dtos.cierreCaja;

import java.time.LocalDate;

public record CreateCierreCajaRequest(
        Long puntoVentaId,
        LocalDate fechaOperacion,
        Long totalPlatosVendidos,
        String comentarios) {
}
