package com.lpc.gestioncomedores.dtos.cierreCaja;

import com.lpc.gestioncomedores.dtos.auth.UsuarioResponse;
import com.lpc.gestioncomedores.dtos.comedor.ComedorCompressedResponse;
import com.lpc.gestioncomedores.dtos.movimiento.MovimientoResponse;
import com.lpc.gestioncomedores.dtos.ptoVenta.PuntoDeVentaResponse;
import com.lpc.gestioncomedores.models.CierreCaja;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DetailedCierreCajaResponse(
        Long id,
        PuntoDeVentaResponse puntoDeVenta,
        ComedorCompressedResponse comedor,
        LocalDate fechaOperacion,
        UsuarioResponse creadoPor,
        BigDecimal montoTotal,
        Long totalPlatosVendidos,
        Instant createdAt,
        String comentarios,
        Long anulacionId,
        List<MovimientoResponse> movimientos
) {

    // Constructor to convert from CierreCaja entity to CierreCajaResponse
    public DetailedCierreCajaResponse(CierreCaja cierreCaja) {
        this(
                cierreCaja.getId(),
                PuntoDeVentaResponse.from(cierreCaja.getPuntoDeVenta()),
                ComedorCompressedResponse.from(cierreCaja.getPuntoDeVenta().getComedor()),
                cierreCaja.getFechaOperacion(),
                UsuarioResponse.from(cierreCaja.getCreadoPor()),
                cierreCaja.calcularMontoTotal(),
                cierreCaja.getTotalPlatosVendidos(),
                cierreCaja.getCreatedAt(),
                cierreCaja.getComentarios(),
                cierreCaja.getAnulacion() != null ? cierreCaja.getAnulacion().getId() : null,
                cierreCaja.getMovimientos().stream().map(MovimientoResponse::new).toList()
        );
    }
}
