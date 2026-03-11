package com.lpc.gestioncomedores.dtos.facturaproveedor;


import java.time.LocalDate;

public record PayFacturaProveedorRequest(
        LocalDate fechaPago,
        String observaciones
        ) {
}
