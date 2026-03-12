package com.lpc.gestioncomedores;

import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.models.cajas.CierreCaja;
import com.lpc.gestioncomedores.models.cajas.CierreCajaLinea;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.comedores.PuntoDeVenta;
import com.lpc.gestioncomedores.models.enums.*;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoCaja;
import com.lpc.gestioncomedores.models.personas.Empleado;
import com.lpc.gestioncomedores.models.personas.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CierreCajaTest {

    private static final LocalDate FECHA_OPERACION_DEFAULT = LocalDate.of(2026, 3, 12);
    private static final Instant CREADO_EN_DEFAULT = Instant.parse("2026-03-12T12:00:00Z");
    private static final String OBS_DEFAULT = "obs inicial";

    @Test
    void create_setsInitialState_andTrimsObservaciones() {
        CierreCaja cierre = nuevoCierreBase("   cierre turno manana   ");

        assertEquals(EstadoCierreCaja.BORRADOR, cierre.getEstado());
        assertEquals("cierre turno manana", cierre.getObservaciones());
        assertEquals(FECHA_OPERACION_DEFAULT, cierre.getFechaOperacion());
        assertNotNull(cierre.getComedor());
        assertNotNull(cierre.getPuntoDeVenta());
        assertNotNull(cierre.getCreadoPor());
        assertNotNull(cierre.getCreadoEn());
        assertTrue(cierre.getLineas().isEmpty());
        assertTrue(cierre.getMovimientos().isEmpty());

        assertNull(cierre.getAnuladoEn());
        assertNull(cierre.getAnuladoPor());
        assertNull(cierre.getMotivoAnulacion());
    }

    @Test
    void create_withBlankObservaciones_setsNull() {
        CierreCaja cierre = nuevoCierreBase("   ");
        assertNull(cierre.getObservaciones());
    }

    @Test
    void create_withNullComedor_throwsBadRequestException() {
        PuntoDeVenta punto = puntoDeVenta(20L, new Comedor(10L, "Comedor Test", new ArrayList<>()));
        Usuario creador = usuario(30L, UsuarioRol.ADMINISTRACION);

        assertThrows(
                BadRequestException.class,
                () -> CierreCaja.create(null, punto, FECHA_OPERACION_DEFAULT, creador, CREADO_EN_DEFAULT, OBS_DEFAULT)
        );
    }

    @Test
    void agregarLinea_addsActiveLine_andUpdatesTotal() {
        CierreCaja cierre = nuevoCierreBase();
        CierreCajaLinea linea = lineaEfectivo(new BigDecimal("120000.00"), "OP-1A");

        cierre.agregarLinea(linea);

        assertEquals(1, cierre.getLineas().size());
        assertTrue(cierre.getLineas().getFirst().estaActiva());
        assertEquals(new BigDecimal("120000.00"), cierre.calcularTotalCierre());
    }

    @Test
    void agregarLinea_duplicateKey_throwsBadRequestException() {
        CierreCaja cierre = nuevoCierreBase();

        cierre.agregarLinea(lineaEfectivo(new BigDecimal("50000.00"), "OP-1A"));

        assertThrows(
                BadRequestException.class,
                () -> cierre.agregarLinea(lineaEfectivo(new BigDecimal("30000.00"), "OP-1B"))
        );
    }

    @Test
    void anularLinea_marksLineAsAnulada_andExcludesFromTotal() {
        CierreCaja cierre = nuevoCierreBase();
        Usuario actor = usuario(90L, UsuarioRol.ADMINISTRACION);

        cierre.agregarLinea(lineaEfectivo(new BigDecimal("50000.00"), "OP-1A"));

        CierreCajaLinea linea = cierre.getLineas().getFirst();
        ReflectionTestUtils.setField(linea, "id", 100L);

        cierre.anularLinea(100L, actor, "error de carga");

        assertEquals(EstadoCierreCajaLinea.ANULADA, linea.getEstado());
        assertEquals(actor, linea.getAnuladoPor());
        assertEquals("error de carga", linea.getMotivoAnulacion());
        assertNotNull(linea.getAnuladoEn());
        assertTrue(linea.getClaveUnicaCierreLinea().contains("|ANULADA|"));
        assertEquals(BigDecimal.ZERO, cierre.calcularTotalCierre());
    }

    @Test
    void anularLinea_whenNotFound_throwsNotFoundException() {
        CierreCaja cierre = nuevoCierreBase();
        Usuario actor = usuario(90L, UsuarioRol.ADMINISTRACION);

        cierre.agregarLinea(lineaEfectivo(new BigDecimal("50000.00"), "OP-1A"));
        ReflectionTestUtils.setField(cierre.getLineas().getFirst(), "id", 100L);

        assertThrows(
                NotFoundException.class,
                () -> cierre.anularLinea(999L, actor, "no existe")
        );
    }

    @Test
    void cerrar_withNoActiveLineas_throwsBadRequestException() {
        CierreCaja cierre = nuevoCierreBase();

        assertThrows(
                BadRequestException.class,
                cierre::cerrar
        );
    }

    @Test
    void cerrar_withOnlyAnuladaLinea_throwsBadRequestException() {
        CierreCaja cierre = nuevoCierreBase();
        Usuario actor = usuario(90L, UsuarioRol.ADMINISTRACION);

        cierre.agregarLinea(lineaEfectivo(new BigDecimal("50000.00"), "OP-1A"));
        CierreCajaLinea linea = cierre.getLineas().getFirst();
        ReflectionTestUtils.setField(linea, "id", 100L);
        cierre.anularLinea(100L, actor, "anular");

        assertThrows(
                BadRequestException.class,
                cierre::cerrar
        );
    }

    @Test
    void cerrar_withActiveLineas_setsEstadoCerrado() {
        CierreCaja cierre = nuevoCierreBase();

        cierre.agregarLinea(lineaEfectivo(new BigDecimal("50000.00"), "OP-1A"));
        cierre.cerrar();

        assertEquals(EstadoCierreCaja.CERRADO, cierre.getEstado());
    }

    @Test
    void agregarLinea_whenCierreCerrado_throwsIllegalStateException() {
        CierreCaja cierre = nuevoCierreBase();

        cierre.agregarLinea(lineaEfectivo(new BigDecimal("50000.00"), "OP-1A"));
        cierre.cerrar();

        assertThrows(
                IllegalStateException.class,
                () -> cierre.agregarLinea(lineaEfectivo(new BigDecimal("30000.00"), "OP-1B"))
        );
    }

    @Test
    void anular_setsEstadoAnulado_andAnulaMovimientosActivos() {
        CierreCaja cierre = nuevoCierreBase();
        Usuario actor = usuario(90L, UsuarioRol.ADMINISTRACION);

        MovimientoCaja movimiento = MovimientoCaja.createDesdeCierre(
                cierre,
                cierre.getComedor(),
                cierre.getPuntoDeVenta(),
                actor,
                new BigDecimal("10000.00"),
                MedioPago.EFECTIVO,
                Instant.now(),
                "mov test"
        );
        cierre.getMovimientos().add(movimiento);

        cierre.anular(actor, "anular cierre");

        assertEquals(EstadoCierreCaja.ANULADO, cierre.getEstado());
        assertEquals(actor, cierre.getAnuladoPor());
        assertEquals("anular cierre", cierre.getMotivoAnulacion());
        assertNotNull(cierre.getAnuladoEn());

        assertEquals(EstadoMovimientoCaja.ANULADO, movimiento.getEstadoMovimientoCaja());
        assertEquals(actor, movimiento.getAnuladoPor());
        assertEquals("anular cierre", movimiento.getMotivoAnulacion());
        assertNotNull(movimiento.getAnuladoEn());
    }

    @Test
    void anular_whenAlreadyAnulado_throwsIllegalStateException() {
        CierreCaja cierre = nuevoCierreBase();
        Usuario actor = usuario(90L, UsuarioRol.ADMINISTRACION);

        cierre.anular(actor, "primera");

        assertThrows(
                IllegalStateException.class,
                () -> cierre.anular(actor, "segunda")
        );
    }

    @Test
    void actualizarObservaciones_updatesTrimmedValue() {
        CierreCaja cierre = nuevoCierreBase();

        cierre.actualizarObservaciones("   nuevo texto   ");

        assertEquals("nuevo texto", cierre.getObservaciones());
    }

    @Test
    void actualizarObservaciones_blank_throwsBadRequestException() {
        CierreCaja cierre = nuevoCierreBase();

        assertThrows(
                BadRequestException.class,
                () -> cierre.actualizarObservaciones("   ")
        );
    }


    // HELPER METHODS

    private CierreCaja nuevoCierreBase() {
        return nuevoCierreBase(OBS_DEFAULT);
    }

    private CierreCaja nuevoCierreBase(String observaciones) {
        Comedor comedor = new Comedor(10L, "Comedor Test", new ArrayList<>());
        PuntoDeVenta punto = puntoDeVenta(20L, comedor);
        Usuario creador = usuario(30L, UsuarioRol.ADMINISTRACION);

        return CierreCaja.create(
                comedor,
                punto,
                FECHA_OPERACION_DEFAULT,
                creador,
                CREADO_EN_DEFAULT,
                observaciones
        );
    }

    private CierreCajaLinea lineaEfectivo(BigDecimal monto, String numeroOperacion) {
        return CierreCajaLinea.create(
                TipoVentaCaja.EFECTIVO,
                monto,
                null,
                null,
                null,
                numeroOperacion,
                null,
                null,
                null
        );
    }

    private PuntoDeVenta puntoDeVenta(Long id, Comedor comedor) {
        PuntoDeVenta p = new PuntoDeVenta();
        ReflectionTestUtils.setField(p, "id", id);
        ReflectionTestUtils.setField(p, "comedor", comedor);
        return p;
    }

    private Empleado empleado(Long id, String taxId, String nombre) {
        Empleado e = new Empleado();
        ReflectionTestUtils.setField(e, "id", id);
        ReflectionTestUtils.setField(e, "taxId", taxId);
        ReflectionTestUtils.setField(e, "nombre", nombre);
        return e;
    }

    private Usuario usuario(Long id, UsuarioRol rol) {
        Empleado e = empleado(id, "20-" + id + "-0", "Usuario " + id);
        return new Usuario(id, e, rol, "hash-test");
    }
}
