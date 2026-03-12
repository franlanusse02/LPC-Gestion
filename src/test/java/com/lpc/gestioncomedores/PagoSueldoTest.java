package com.lpc.gestioncomedores;

import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.exceptions.childs.InvalidDateRangeException;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.enums.*;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoPagoSueldo;
import com.lpc.gestioncomedores.models.personas.Empleado;
import com.lpc.gestioncomedores.models.personas.Usuario;
import com.lpc.gestioncomedores.models.sueldos.PagoSueldo;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class PagoSueldoTest {


    private static final BigDecimal MONTO_DEFAULT = new BigDecimal("150000.00");


    @Test
    void create_setsInitialState_andTrimsFuncionEmpleado() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        assertEquals(EstadoPagoSueldo.REGISTRADO, pago.getEstado());
        assertEquals("Cocinero", pago.getFuncionEmpleado());
        assertEquals(LocalDate.of(2026, 3, 1), pago.getPeriodoInicio());
        assertEquals(LocalDate.of(2026, 3, 31), pago.getPeriodoFin());
        assertEquals(new BigDecimal("150000.00"), pago.getMontoTotal());
        assertEquals("obs inicial", pago.getObservaciones());

        assertNotNull(pago.getCreadoEn());
        assertNotNull(pago.getCreadoPor());
        assertTrue(pago.getMovimientos().isEmpty());

        assertNull(pago.getAnuladoEn());
        assertNull(pago.getAnuladoPor());
        assertNull(pago.getMotivoAnulacion());

    }

    @Test
    void validarPeriodo_invalidRange_throws() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        assertThrows(
                InvalidDateRangeException.class,
                () -> pago.validarPeriodo(
                        LocalDate.of(2026, 3, 31),
                        LocalDate.of(2026, 3, 1)
                )
        );
    }

    @Test
    void agregarMovimientoParcial_addsOneMovimiento() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);
        pago.agregarMovimientoParcial(MedioPago.MERCADOPAGO, new BigDecimal("100000.00"),"00-1A");

        MovimientoPagoSueldo movimiento = pago.getMovimientos().getFirst();
        assertEquals(1, pago.getMovimientos().size());
        assertEquals(EstadoMovimientoPagoSueldo.REGISTRADO, movimiento.getEstadoMovimientoPagoSueldo());
        assertEquals(MedioPago.MERCADOPAGO, movimiento.getMedioPago());
        assertEquals(new BigDecimal("100000.00"), movimiento.getMonto());
        assertEquals("00-1A", movimiento.getNumeroOperacion());
    }

    @Test
    void agregarMovimientoParcial_exceedsMontoTotal_throws() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);
        pago.agregarMovimientoParcial(MedioPago.MERCADOPAGO, new BigDecimal("100000.00"),"00-1A");

        assertThrows(
                BadRequestException.class,
                () -> pago.agregarMovimientoParcial(
                        MedioPago.MERCADOPAGO,
                        new BigDecimal("100000.00"),
                        "00-1B"
                )
        );
    }

    @Test
    void reemplazarMovimientoParcial_annulsOriginalAndAddsNew() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        pago.agregarMovimientoParcial(
                MedioPago.TRANSFERENCIABANCARIA,
                new BigDecimal("50000.00"),
                "00-1A"
        );

        setMovimientoId(pago, 0, 100L);

        Usuario actor = usuario(90L, UsuarioRol.RECURSOS_HUMANOS);

        pago.reemplazarMovimientoParcial(
                100L,
                MedioPago.MERCADOPAGO,
                new BigDecimal("30000.00"),
                "00-1A-NEW",
                actor,
                "Nuevo");

        assertEquals(2, pago.getMovimientos().size());
        MovimientoPagoSueldo original = pago.getMovimientos().stream()
                .filter(m -> m.getId() == 100L)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Original movement not found"));
        assertEquals(EstadoMovimientoPagoSueldo.ANULADO, original.getEstadoMovimientoPagoSueldo());
        assertNotNull(original.getAnuladoEn());
        assertEquals(actor, original.getAnuladoPor());
        assertEquals("Nuevo", original.getMotivoAnulacion());


        MovimientoPagoSueldo nuevo = pago.getMovimientos().stream()
                .filter(m -> "00-1A-NEW".equals(m.getNumeroOperacion()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Replacement movement not found"));
        assertEquals(EstadoMovimientoPagoSueldo.REGISTRADO, nuevo.getEstadoMovimientoPagoSueldo());
        assertEquals(new BigDecimal("30000.00"), nuevo.getMonto());
        assertEquals(MedioPago.MERCADOPAGO, nuevo.getMedioPago());

    }

    @Test
    void anularMovimientoParcial_annulsTargetMovimiento() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        pago.agregarMovimientoParcial(
                MedioPago.TRANSFERENCIABANCARIA,
                new BigDecimal("50000.00"),
                "00-1A"
        );

        setMovimientoId(pago, 0, 100L);

        Usuario actor = usuario(90L, UsuarioRol.RECURSOS_HUMANOS);

        pago.anularMovimientoParcial(100L, actor, "anular");

        MovimientoPagoSueldo movimiento = pago.getMovimientos().getFirst();

        assertEquals(EstadoMovimientoPagoSueldo.ANULADO, movimiento.getEstadoMovimientoPagoSueldo());
        assertEquals(actor, movimiento.getAnuladoPor());
        assertEquals("anular", movimiento.getMotivoAnulacion());
        assertNotNull(movimiento.getAnuladoEn());

    }

    @Test
    void actualizarObservaciones_updatesValue() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        pago.actualizarObservaciones("nuevotexto");

        assertEquals("nuevotexto", pago.getObservaciones());


    }

    @Test
    void anularPagoSueldo_anullsPagoSueldo_andAnullsAllActiveChildMovimientos() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        pago.agregarMovimientoParcial(
                MedioPago.TRANSFERENCIABANCARIA,
                new BigDecimal("50000.00"),
                "00-1A"
        );
        pago.agregarMovimientoParcial(
                MedioPago.MERCADOPAGO,
                new BigDecimal("30000.00"),
                "00-1B"
        );

        Usuario actor = usuario(90L, UsuarioRol.RECURSOS_HUMANOS);

        pago.anularPagoSueldo(actor, "mtv");

        assertEquals(EstadoPagoSueldo.ANULADO, pago.getEstado());
        assertEquals(actor, pago.getAnuladoPor());
        assertEquals("mtv", pago.getMotivoAnulacion());
        assertNotNull(pago.getAnuladoEn());

        assertEquals(2, pago.getMovimientos().size());

        for (MovimientoPagoSueldo movimiento : pago.getMovimientos()) {
            assertEquals(EstadoMovimientoPagoSueldo.ANULADO, movimiento.getEstadoMovimientoPagoSueldo());
            assertEquals(actor, movimiento.getAnuladoPor());
            assertEquals("mtv", movimiento.getMotivoAnulacion());
            assertNotNull(movimiento.getAnuladoEn());
        }
    }



    // EDGE CASES

    @Test
    void validarPeriodo_validRange_doesNotThrow() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        assertDoesNotThrow(() ->
                pago.validarPeriodo(
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 3, 31)
                )
        );
    }

    @Test
    void validarPeriodo_nullDate_throwsBadRequestException() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        assertThrows(
                BadRequestException.class,
                () -> pago.validarPeriodo(null, LocalDate.of(2026, 3, 31))
        );

        assertThrows(
                BadRequestException.class,
                () -> pago.validarPeriodo(LocalDate.of(2026, 3, 1), null)
        );
    }

    @Test
    void agregarMovimientoParcial_withNullMedioPago_throwsBadRequestException() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        assertThrows(
                BadRequestException.class,
                () -> pago.agregarMovimientoParcial(
                        null,
                        new BigDecimal("1000.00"),
                        "00-1A"
                )
        );
    }

    @Test
    void agregarMovimientoParcial_withMontoZero_throwsBadRequestException() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        assertThrows(
                BadRequestException.class,
                () -> pago.agregarMovimientoParcial(
                        MedioPago.MERCADOPAGO,
                        BigDecimal.ZERO,
                        "00-1A"
                )
        );
    }

    @Test
    void agregarMovimientoParcial_withBlankNumeroOperacion_throwsBadRequestException() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        assertThrows(
                BadRequestException.class,
                () -> pago.agregarMovimientoParcial(
                        MedioPago.MERCADOPAGO,
                        new BigDecimal("1000.00"),
                        "   "
                )
        );
    }

    @Test
    void anularPagoSueldo_whenAlreadyAnulado_throwsIllegalStateException() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);
        Usuario actor = usuario(90L, UsuarioRol.RECURSOS_HUMANOS);

        pago.anularPagoSueldo(actor, "mtv");

        assertThrows(
                IllegalStateException.class,
                () -> pago.anularPagoSueldo(actor, "mtv2")
        );
    }

    @Test
    void agregarMovimientoParcial_whenPagoAnulado_throwsIllegalStateException() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);
        Usuario actor = usuario(90L, UsuarioRol.RECURSOS_HUMANOS);

        pago.anularPagoSueldo(actor, "mtv");

        assertThrows(
                IllegalStateException.class,
                () -> pago.agregarMovimientoParcial(
                        MedioPago.MERCADOPAGO,
                        new BigDecimal("1000.00"),
                        "00-1A"
                )
        );
    }

    @Test
    void reemplazarMovimientoParcial_whenMovimientoNotFound_throwsNotFoundException() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        pago.agregarMovimientoParcial(
                MedioPago.TRANSFERENCIABANCARIA,
                new BigDecimal("50000.00"),
                "00-1A"
        );
        setMovimientoId(pago, 0, 100L);

        Usuario actor = usuario(90L, UsuarioRol.RECURSOS_HUMANOS);

        assertThrows(
                NotFoundException.class,
                () -> pago.reemplazarMovimientoParcial(
                        999L,
                        MedioPago.MERCADOPAGO,
                        new BigDecimal("30000.00"),
                        "00-1A-NEW",
                        actor,
                        "Nuevo"
                )
        );
    }

    @Test
    void anularMovimientoParcial_whenMovimientoNotFound_throwsNotFoundException() {
        PagoSueldo pago = nuevoPagoBase(MONTO_DEFAULT);

        pago.agregarMovimientoParcial(
                MedioPago.TRANSFERENCIABANCARIA,
                new BigDecimal("50000.00"),
                "00-1A"
        );
        setMovimientoId(pago, 0, 100L);

        Usuario actor = usuario(90L, UsuarioRol.RECURSOS_HUMANOS);

        assertThrows(
                NotFoundException.class,
                () -> pago.anularMovimientoParcial(999L, actor, "anular")
        );
    }








    //HELPER METHODS

    private PagoSueldo nuevoPagoBase(BigDecimal montoTotal) {
        BigDecimal monto = (montoTotal != null) ? montoTotal : MONTO_DEFAULT;

        return PagoSueldo.create(
                empleado(10L, "20-11111111-1", "Empleado Test"),
                new Comedor(20L, "Comedor Test", new ArrayList<>()),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                ContratoEmpleado.MENSUAL,
                "  Cocinero  ",
                LocalDate.of(2026, 3, 31),
                monto,
                "obs inicial",
                usuario(10L, UsuarioRol.RECURSOS_HUMANOS)
        );
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

    private void setMovimientoId(PagoSueldo pago, int index, long id) {
        MovimientoPagoSueldo mov = pago.getMovimientos().get(index);
        ReflectionTestUtils.setField(mov, "id", id); // field declared in superclass Movimiento
    }

}
