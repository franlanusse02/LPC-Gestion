package com.lpc.gestioncomedores.dtos.cierreCaja;

import com.lpc.gestioncomedores.models.CierreCaja;
import com.lpc.gestioncomedores.models.Movimiento;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCaja;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record CierreCajaResponse(
        Long id,
        Long puntoDeVentaId,
        LocalDate fechaOperacion,
        Long creadoPorId,
        Long totalPlatosVendidos,
        EstadoCierreCaja estado,
        Instant createdAt,
        String comentarios,
        Long anulacionId,
        List<Long> movimientosIds
) {

    // Constructor to convert from CierreCaja entity to CierreCajaResponse
    public CierreCajaResponse(CierreCaja cierreCaja) {
        this(
                cierreCaja.getId(),
                cierreCaja.getPuntoDeVenta() != null ? cierreCaja.getPuntoDeVenta().getId() : null,
                cierreCaja.getFechaOperacion(),
                cierreCaja.getCreadoPor() != null ? cierreCaja.getCreadoPor().getCuil() : null,
                cierreCaja.getTotalPlatosVendidos(),
                cierreCaja.getEstado(),
                cierreCaja.getCreatedAt(),
                cierreCaja.getComentarios(),
                cierreCaja.getAnulacion() != null ? cierreCaja.getAnulacion().getId() : null,
                cierreCaja.getMovimientos() != null ?
                        cierreCaja.getMovimientos().stream()
                                .map(Movimiento::getId)
                                .collect(Collectors.toList())
                        : List.of()
        );
    }
}