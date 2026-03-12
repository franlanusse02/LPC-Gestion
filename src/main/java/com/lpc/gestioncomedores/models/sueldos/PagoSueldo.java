package com.lpc.gestioncomedores.models.sueldos;


import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.exceptions.childs.InvalidDateRangeException;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.enums.ContratoEmpleado;
import com.lpc.gestioncomedores.models.enums.EstadoPagoSueldo;
import com.lpc.gestioncomedores.models.enums.MedioPago;
import com.lpc.gestioncomedores.models.enums.Sentido;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoPagoSueldo;
import com.lpc.gestioncomedores.models.personas.Empleado;
import com.lpc.gestioncomedores.models.personas.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@Table(
        name = "pagos_sueldo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pago_sueldo",
                columnNames = {
                        "empleado_id",
                        "periodo_inicio",
                        "periodo_fin",
                        "contrato",
                        "funcion_empleado",
                        "comedor_id"}
        )
)
public class PagoSueldo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContratoEmpleado contrato;

    @Column(name = "funcion_empleado", nullable = false)
    private String funcionEmpleado;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "monto_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPagoSueldo estado = EstadoPagoSueldo.REGISTRADO;

    @Column
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pago_sueldo_creado_por"))
    private Usuario creadoPor;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id",
            foreignKey = @ForeignKey(name = "fk_pago_sueldo_anulado_por"))
    private Usuario anuladoPor;

    @Column(name = "anulado_en")
    private Instant anuladoEn;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    @OneToMany(mappedBy = "pagoSueldo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientoPagoSueldo> movimientos = new ArrayList<>();


    protected PagoSueldo (){
        //JPA
    }

    private PagoSueldo(
            Empleado empleado,
            Comedor comedor,
            LocalDate periodoInicio,
            LocalDate periodoFin,
            ContratoEmpleado contrato,
            String funcionEmpleado,
            LocalDate fechaPago,
            BigDecimal montoTotal,
            String observaciones,
            Usuario creadoPor
    ) {
        this.empleado = empleado;
        this.comedor = comedor;
        this.periodoInicio = periodoInicio;
        this.periodoFin = periodoFin;
        this.contrato = contrato;
        this.funcionEmpleado = funcionEmpleado != null ? funcionEmpleado.trim() : null;
        this.fechaPago = fechaPago;
        this.montoTotal = montoTotal;
        this.observaciones = observaciones;
        this.creadoPor = creadoPor;
        this.creadoEn = Instant.now();
        this.estado = EstadoPagoSueldo.REGISTRADO;
    }

    //METODOS

    public static PagoSueldo create(
            Empleado empleado,
            Comedor comedor,
            LocalDate periodoInicio,
            LocalDate periodoFin,
            ContratoEmpleado contrato,
            String funcionEmpleado,
            LocalDate fechaPago,
            BigDecimal montoTotal,
            String observaciones,
            Usuario creadoPor
    ) {
        return new PagoSueldo(
                empleado,
                comedor,
                periodoInicio,
                periodoFin,
                contrato,
                funcionEmpleado,
                fechaPago,
                montoTotal,
                observaciones,
                creadoPor
        );
    }

    public void anularMovimientoParcial(Long movimientoId, Usuario usuario, String motivo) {
        if (this.estado == EstadoPagoSueldo.ANULADO) {
            throw new IllegalStateException("MovimientoParcial ya anulado.");
        }else if (movimientoId == null) {
            throw new BadRequestException("Id Movimiento no puede ser de tipo null.");
        }else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser de tipo null.");
        }else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio.");
        }

        MovimientoPagoSueldo movimiento = this.movimientos.stream()
                .filter(m -> movimientoId.equals(m.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Movimiento parcial no se ha encontrado en este pago."));

        movimiento.anular(usuario, motivo.trim(), Instant.now());
    }

    public void agregarMovimientoParcial(
            MedioPago medioPago,
            BigDecimal montoParcial,
            String numeroOperacion
    ) {

        if (this.estado == EstadoPagoSueldo.ANULADO) {
            throw new IllegalStateException("No se puede agregar MovimientoParcial a un pedido anulado");
        } else if (montoParcial == null || montoParcial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("No se puede agregar MovimientoParcial de monto 0.");
        } else if (numeroOperacion == null || numeroOperacion.isBlank()) { //TODO: CONSULTAR SI PUEDE ESTAR VACIO
            throw new BadRequestException("Numero de operacion no puede estar vacio");
        } else if (medioPago == null) {
            throw new BadRequestException("Medio de pago no puede ser de tipo null");
        }


        BigDecimal totalActivoActual = this.movimientos.stream()
                .filter(m -> !m.estaAnulado())
                .map(MovimientoPagoSueldo::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal nuevoTotalActivo = totalActivoActual.add(montoParcial);
        if (nuevoTotalActivo.compareTo(this.montoTotal) > 0) {
            throw new BadRequestException("La suma de parciales no puede superar el monto total.");
        }

        MovimientoPagoSueldo mov = new MovimientoPagoSueldo();

        mov.setPagoSueldo(this);
        mov.setComedor(this.getComedor());
        mov.setMonto(montoParcial);
        mov.setMedioPago(medioPago);
        mov.setFechaHora(Instant.now());
        mov.setSentido(Sentido.EGRESO);
        mov.setNumeroOperacion(numeroOperacion);


        this.movimientos.add(mov);
    }


    public void reemplazarMovimientoParcial(
            Long movimientoIdOriginal,
            MedioPago nuevoMedioPago,
            BigDecimal nuevoMontoParcial,
            String nuevoNumeroOperacion,
            Usuario usuario,
            String motivo
    ) {
        if (this.estado == EstadoPagoSueldo.ANULADO) {
            throw new IllegalStateException("No se puede reemplazar movimientos en un pago anulado.");
        } else if (movimientoIdOriginal == null) {
            throw new BadRequestException("Id de movimiento original no puede ser de tipo null.");
        }else if (nuevoMedioPago == null) {
            throw new BadRequestException("Medio de pago no puede ser de tipo null.");
        }else if (nuevoMontoParcial == null || nuevoMontoParcial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Monto parcial no puede ser de tipo null.");
        }else if (nuevoNumeroOperacion == null || nuevoNumeroOperacion.isBlank()) {
            throw new BadRequestException("Numero de operacion no puede estar vacio.");
        }else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser de tipo null");
        }else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio");
        }

        MovimientoPagoSueldo original = this.movimientos.stream()
                .filter(m -> movimientoIdOriginal.equals(m.getId()))
                .filter(m -> !m.estaAnulado())
                .findFirst()
                .orElseThrow(() ->new NotFoundException("Pago no encontrado o ya fue anulado."));

        BigDecimal totalActivo = this.movimientos.stream()
                .filter(m -> !m.estaAnulado())
                .map(MovimientoPagoSueldo::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal proyectado = totalActivo
                .subtract(original.getMonto())
                .add(nuevoMontoParcial);

        if (proyectado.compareTo(this.montoTotal) > 0) {
            throw new BadRequestException("La suma de los parciales no puede superar el monto total.");
        }

        original.anular(usuario, motivo.trim(), Instant.now());

        MovimientoPagoSueldo nuevo = new MovimientoPagoSueldo();
        nuevo.setPagoSueldo(this);
        nuevo.setComedor(this.getComedor());
        nuevo.setMonto(nuevoMontoParcial);
        nuevo.setMedioPago(nuevoMedioPago);
        nuevo.setFechaHora(Instant.now());
        nuevo.setSentido(Sentido.EGRESO);
        nuevo.setNumeroOperacion(nuevoNumeroOperacion);

        this.movimientos.add(nuevo);

        this.estado = EstadoPagoSueldo.REGISTRADO;
        this.anuladoPor = null;
        this.anuladoEn = null;
        this.motivoAnulacion = null;

    }

    public void validarPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las dos fechas tienen que ser fechas validas.");
        } else if (fechaInicio.isAfter(fechaFin)) {
            throw new InvalidDateRangeException("fechaInicio tiene que ser antes que fechaFin");
        }
    }

    public void actualizarObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public void anularPagoSueldo(
        Usuario usuario,
        String motivo
    ){
        if (this.estado == EstadoPagoSueldo.ANULADO) {
            throw new IllegalStateException("El pago ya fue anulado.");
        } else if (usuario == null) {
            throw new BadRequestException("Usuario no puede ser null");
        } else if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Motivo no puede estar vacio");
        }

        this.movimientos.stream()
                .filter(m -> !m.estaAnulado())
                .forEach(m -> m.anular(usuario, motivo, Instant.now()));

        this.estado = EstadoPagoSueldo.ANULADO;
        this.anuladoEn = Instant.now();
        this.anuladoPor = usuario;
        this.motivoAnulacion = motivo.trim();
    }
}






