package com.lpc.gestioncomedores;

import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.models.cajas.CierreCajaLinea;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCajaLinea;
import com.lpc.gestioncomedores.models.enums.ModoPagoEventoCaja;
import com.lpc.gestioncomedores.models.enums.TipoVentaCaja;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CierreCajaLineaTest {

    @Test
    void create_eventos_withoutCobradoEvento_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> CierreCajaLinea.create(
                        TipoVentaCaja.EVENTOS,
                        new BigDecimal("1000.00"),
                        null,
                        null,
                        ModoPagoEventoCaja.LINK_DE_PAGO,
                        "OP-EVT",
                        "ORD-1",
                        10,
                        "Piso 1"
                )
        );
    }

    @Test
    void create_eventos_withoutModoPagoEvento_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> CierreCajaLinea.create(
                        TipoVentaCaja.EVENTOS,
                        new BigDecimal("1000.00"),
                        null,
                        true,
                        null,
                        "OP-EVT",
                        "ORD-1",
                        10,
                        "Piso 1"
                )
        );
    }

    @Test
    void create_eventos_withoutNumeroOrdenEvento_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> CierreCajaLinea.create(
                        TipoVentaCaja.EVENTOS,
                        new BigDecimal("1000.00"),
                        null,
                        true,
                        ModoPagoEventoCaja.LINK_DE_PAGO,
                        "OP-EVT",
                        "   ",
                        10,
                        "Piso 1"
                )
        );
    }

    @Test
    void create_eventos_withCantidadPaxEventoZero_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> CierreCajaLinea.create(
                        TipoVentaCaja.EVENTOS,
                        new BigDecimal("1000.00"),
                        null,
                        true,
                        ModoPagoEventoCaja.LINK_DE_PAGO,
                        "OP-EVT",
                        "ORD-1",
                        0,
                        "Piso 1"
                )
        );
    }

    @Test
    void create_menuDelDia_withoutPrecioMenuUnitarioSnapshot_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> CierreCajaLinea.create(
                        TipoVentaCaja.MENU_DEL_DIA,
                        new BigDecimal("5000.00"),
                        null,
                        null,
                        null,
                        "OP-MENU",
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void create_menuDelDia_withPrecioMenuUnitarioSnapshotZero_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> CierreCajaLinea.create(
                        TipoVentaCaja.MENU_DEL_DIA,
                        new BigDecimal("5000.00"),
                        BigDecimal.ZERO,
                        null,
                        null,
                        "OP-MENU",
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void create_nonEvento_withEventoFields_throwsBadRequestException() {
        assertThrows(
                BadRequestException.class,
                () -> CierreCajaLinea.create(
                        TipoVentaCaja.EFECTIVO,
                        new BigDecimal("1000.00"),
                        null,
                        true,
                        ModoPagoEventoCaja.TARJETA_CORPORATIVA,
                        "OP-1",
                        "ORD-1",
                        5,
                        "Piso 1"
                )
        );
    }

    @Test
    void create_eventos_withRequiredFields_setsClaveUnicaEvento() {
        CierreCajaLinea linea = CierreCajaLinea.create(
                TipoVentaCaja.EVENTOS,
                new BigDecimal("1000.00"),
                null,
                true,
                ModoPagoEventoCaja.LINK_DE_PAGO,
                "OP-EVT",
                "  ORD-1  ",
                10,
                "Piso 1"
        );

        assertEquals(EstadoCierreCajaLinea.ACTIVA, linea.getEstado());
        assertEquals("EVENTOS|ORD-1", linea.getClaveUnicaCierreLinea());
        assertEquals("ORD-1", linea.getNumeroOrdenEvento());
    }

    @Test
    void create_menuDelDia_withPrecioValido_setsClaveUnica() {
        CierreCajaLinea linea = CierreCajaLinea.create(
                TipoVentaCaja.MENU_DEL_DIA,
                new BigDecimal("5000.00"),
                new BigDecimal("1200.00"),
                null,
                null,
                "OP-MENU",
                null,
                null,
                null
        );

        assertEquals(EstadoCierreCajaLinea.ACTIVA, linea.getEstado());
        assertEquals("MENU_DEL_DIA", linea.getClaveUnicaCierreLinea());
    }

    @Test
    void create_nonEvento_validFields_setsClaveUnicaAndNormalizesNumeroOperacion() {
        CierreCajaLinea linea = CierreCajaLinea.create(
                TipoVentaCaja.EFECTIVO,
                new BigDecimal("1500.00"),
                null,
                null,
                null,
                "  OP-1  ",
                null,
                null,
                null
        );

        assertEquals(EstadoCierreCajaLinea.ACTIVA, linea.getEstado());
        assertEquals("EFECTIVO", linea.getClaveUnicaCierreLinea());
        assertEquals("OP-1", linea.getNumeroOperacion());
    }
}
