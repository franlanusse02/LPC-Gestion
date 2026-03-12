package com.lpc.gestioncomedores.dtos.cajas.responses;

import com.lpc.gestioncomedores.models.enums.CategoriaCaja;
import com.lpc.gestioncomedores.models.enums.EstadoMovimientoCaja;
import com.lpc.gestioncomedores.models.enums.MedioPago;
import com.lpc.gestioncomedores.models.enums.Sentido;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimientoCajaResponse(
        Long id,
        Long puntoVentaId,
        CategoriaCaja categoria,
        Long usuarioId,
        Long cierreCajaId,
        BigDecimal monto,
        MedioPago medioPago,
        Sentido sentido,
        Instant fechaHora,
        String comentarios,
        EstadoMovimientoCaja estadoMovimientoCaja,
        Instant anuladoEn,
        Long anuladoPorId,
        String motivoAnulacion
) {
}
