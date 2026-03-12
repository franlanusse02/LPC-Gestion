package com.lpc.gestioncomedores;

import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.enums.EstadoFacturaProveedor;
import com.lpc.gestioncomedores.models.enums.MedioPago;
import com.lpc.gestioncomedores.models.proveedores.FacturaProveedor;
import com.lpc.gestioncomedores.models.proveedores.Proveedor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FacturaProveedorTest {

    @Test
    void create_startsInPendiente() {
        FacturaProveedor factura = nuevaFactura();
        assertEquals(EstadoFacturaProveedor.PENDIENTE, factura.getEstado());
    }

    @Test
    void approve_setsEstadoEmitido() {
        FacturaProveedor factura = nuevaFactura();

        LocalDate fechaEmisionExpected = LocalDate.of(2026, 3, 12);
        LocalDate fechaPagoProvisoriaExpected = LocalDate.of(2026, 3, 20);
        factura.approvePayment(
                fechaPagoProvisoriaExpected,
                fechaEmisionExpected,
                null,
                "ok"
        );
        assertEquals(EstadoFacturaProveedor.EMITIDO, factura.getEstado());
        assertEquals(fechaEmisionExpected, factura.getFechaEmision());
        assertEquals(fechaPagoProvisoriaExpected, factura.getFechaPagoProvisoria());
    }

    @Test
    void markAsPaid_usesProvisionalDateWhenFechaPagoIsNull() {
        FacturaProveedor factura = nuevaFactura();

        factura.approvePayment(
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 12),
                null,
                null
        );

        factura.markAsPaid(null, "pagada");

        assertEquals(EstadoFacturaProveedor.PAGADO, factura.getEstado());
        assertEquals(LocalDate.of(2026, 3, 20), factura.getFechaPago());
    }

    @Test
    void markAsPaid_withoutAnyDate_throws() {
        FacturaProveedor factura = nuevaFactura();

        factura.approvePayment(null, LocalDate.of(2026, 3, 12), null, null);
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> factura.markAsPaid(null, null)
        );

        assertTrue(ex.getMessage().contains("fecha de pago"));
    }

    @Test
    void cancel_setsEstadoCancelado() {
        FacturaProveedor factura = nuevaFactura();

        factura.cancelPayment("cancelada");

        assertEquals(EstadoFacturaProveedor.CANCELADO, factura.getEstado());
    }

    // EDGE CASES

    @Test
    void applyReviewPatch_partialUpdate_onlyNonNullFields() {
        FacturaProveedor factura = nuevaFactura();

        Proveedor proveedorNuevo = new Proveedor();
        proveedorNuevo.setId(2L);
        proveedorNuevo.setName("Proveedor Nuevo");
        proveedorNuevo.setTaxId("30-99999999-9");

        Comedor comedorNuevo = new Comedor(2L, "Comedor Norte", new ArrayList<>());

        factura.applyReviewPatch(
                proveedorNuevo,
                comedorNuevo,
                null,
                null,
                new BigDecimal("200000.00"),
                null,
                "observacion nueva"
        );

        assertEquals(2L, factura.getProveedor().getId());
        assertEquals(2L, factura.getComedor().getId());
        assertEquals("A-0001-00000001", factura.getNumeroFactura());
        assertEquals(LocalDate.of(2026, 3, 10), factura.getFechaFactura());
        assertEquals(new BigDecimal("200000.00"), factura.getMonto());
        assertEquals(MedioPago.TRANSFERENCIABANCARIA, factura.getMedioPago());
        assertEquals("observacion nueva", factura.getObservaciones());
    }

    @Test
    void applyReviewPatch_allNull_doesNotChangeEntity() {
        FacturaProveedor factura = nuevaFactura();

        Proveedor proveedorBefore = factura.getProveedor();
        Comedor comedorBefore = factura.getComedor();
        String numeroFacturaBefore = factura.getNumeroFactura();
        LocalDate fechaFacturaBefore = factura.getFechaFactura();
        BigDecimal montoBefore = factura.getMonto();
        MedioPago medioPagoBefore = factura.getMedioPago();
        String observacionesBefore = factura.getObservaciones();

        factura.applyReviewPatch(null, null, null, null, null, null, null);

        assertSame(proveedorBefore, factura.getProveedor());
        assertSame(comedorBefore, factura.getComedor());
        assertEquals(numeroFacturaBefore, factura.getNumeroFactura());
        assertEquals(fechaFacturaBefore, factura.getFechaFactura());
        assertEquals(montoBefore, factura.getMonto());
        assertEquals(medioPagoBefore, factura.getMedioPago());
        assertEquals(observacionesBefore, factura.getObservaciones());
    }

    @Test
    void markAsPaid_withExplicitFechaPago_overridesProvisional() {
        FacturaProveedor factura = nuevaFactura();

        factura.approvePayment(
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 12),
                null,
                "aprobada"
        );

        factura.markAsPaid(LocalDate.of(2026, 3, 25), "pagada");

        assertEquals(EstadoFacturaProveedor.PAGADO, factura.getEstado());
        assertEquals(LocalDate.of(2026, 3, 25), factura.getFechaPago());
    }

    @Test
    void markAsPaid_withNullObservaciones_keepsPreviousObservaciones() {
        FacturaProveedor factura = nuevaFactura();

        factura.approvePayment(
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 12),
                null,
                "obs aprobada"
        );

        factura.markAsPaid(LocalDate.of(2026, 3, 21), null);

        assertEquals("obs aprobada", factura.getObservaciones());
    }

    @Test
    void approvePayment_withNullObservaciones_keepsPreviousObservaciones() {
        FacturaProveedor factura = nuevaFactura();

        String observacionesBefore = factura.getObservaciones();

        factura.approvePayment(
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 12),
                null,
                null
        );

        assertEquals(observacionesBefore, factura.getObservaciones());
    }

    @Test
    void cancelPayment_withNullObservaciones_keepsPreviousObservaciones() {
        FacturaProveedor factura = nuevaFactura();

        String observacionesBefore = factura.getObservaciones();

        factura.cancelPayment(null);

        assertEquals(EstadoFacturaProveedor.CANCELADO, factura.getEstado());
        assertEquals(observacionesBefore, factura.getObservaciones());
    }


    // HELPER METHODS

    private FacturaProveedor nuevaFactura() {
        return FacturaProveedor.create(
                proveedor(),
                comedor(),
                "A-0001-00000001",
                LocalDate.of(2026, 3, 10),
                new BigDecimal("150000.00"),
                MedioPago.TRANSFERENCIABANCARIA,
                "test"
        );
    }

    private Proveedor proveedor() {
        Proveedor p = new Proveedor();
        p.setId(1L);
        p.setName("Proveedor Test");
        p.setTaxId("30-12345678-9");
        return p;
    }

    private Comedor comedor() {
        return new Comedor(1L, "Comedor Test", new ArrayList<>());
    }
}
