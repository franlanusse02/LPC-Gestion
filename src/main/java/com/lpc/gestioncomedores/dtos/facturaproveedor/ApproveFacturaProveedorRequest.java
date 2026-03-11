package com.lpc.gestioncomedores.dtos.facturaproveedor;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ApproveFacturaProveedorRequest(
        @NotNull LocalDate fechaEmision,
        LocalDate fechaPagoProvisoria,
        Long bancoPagadorId,
        String observaciones


        ) {
}
