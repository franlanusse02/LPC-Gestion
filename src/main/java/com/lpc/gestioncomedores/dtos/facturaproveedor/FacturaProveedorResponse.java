package com.lpc.gestioncomedores.dtos.facturaproveedor;

import com.lpc.gestioncomedores.models.enums.EstadoFacturaProveedor;
import com.lpc.gestioncomedores.models.enums.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaProveedorResponse(
        Long id,
        Long comedorId,
        String comedorNombre,
        Long proveedorId,
        String proveedorNombre,
        String proveedorTaxId,
        String numeroFactura,
        LocalDate fechaFactura, //Fecha ingresada
        LocalDate fechaEmision, //Completado por admin: estado -> APROBADO
        BigDecimal monto,
        MedioPago medioPago,
        EstadoFacturaProveedor estado,
        Long movimientoPagoProveedorId, //NUll hasta pago: estado -> PAGADO
        LocalDate fechaPagoProvisoria,
        LocalDate fechaPago, //Null hasta pago: estado -> PAGADO
        String observaciones
) {
}
