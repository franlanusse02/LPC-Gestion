package com.lpc.gestioncomedores.services.sueldos;


import com.lpc.gestioncomedores.dtos.sueldos.*;
import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.exceptions.childs.ForbiddenException;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.exceptions.childs.UnauthorizedException;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoPagoSueldo;
import com.lpc.gestioncomedores.models.personas.Empleado;
import com.lpc.gestioncomedores.models.personas.Usuario;
import com.lpc.gestioncomedores.models.sueldos.PagoSueldo;
import com.lpc.gestioncomedores.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoSueldoService {
    private final PagoSueldoRepository pagoSueldoRepo;
    private final MovimientoPagoSueldoRepository movPagoSueldoRepo;
    private final ComedorRepository comedorRepo;
    private final UsuarioRepository usuarioRepo; //O CurrentUserProvider -> primero Auth layer
    private final EmpleadoRepository empleadoRepo;

    @Transactional
    public PagoSueldoResponse create(CreatePagoSueldoRequest req, Long usuarioId) {
        if (usuarioId == null) {
            throw new BadRequestException("Usuario Id no puede ser null");
        }

        Comedor comedor = comedorRepo.findById(req.comedorId())
                .orElseThrow(() -> new NotFoundException("Comedor not found"));
        Empleado empleado = empleadoRepo.findById(req.empleadoId())
                .orElseThrow(() -> new NotFoundException("Empleado not found"));
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario not found"));

        assertCanManagePayroll(usuario);

        String funcionNormalizada = req.funcionEmpleado() == null ? null : req.funcionEmpleado().trim();

        if (pagoSueldoRepo.existsDuplicate(
                req.empleadoId(),
                req.periodoInicio(),
                req.periodoFin(),
                req.contrato(),
                funcionNormalizada,
                comedor.getId()
        )) {
            throw new BadRequestException("Ya existe un pago de sueldo en esa combinacion.");
        }

        PagoSueldo pago = PagoSueldo.create(
                empleado,
                comedor,
                req.periodoInicio(),
                req.periodoFin(),
                req.contrato(),
                funcionNormalizada,
                req.fechaPago(),
                req.montoTotal(),
                req.observaciones(),
                usuario
                );

        pago.validarPeriodo(req.periodoInicio(), req.periodoFin());

        pago = pagoSueldoRepo.save(pago);

        return toResponse(pago);
    }


    public PagoSueldoResponse getById(Long id) {

        PagoSueldo pago = getPagoOrThrow(id);

        return toResponse(pago);
    }


    @Transactional
    public PagoSueldoResponse  agregarMovimientoParcial(Long id, AgregarMovimientoParcialRequest req, Long usuarioId) {

        if (usuarioId == null) {
            throw new BadRequestException("UsuarioId cant be null."); //TODO: Reemplazar por validacion real
        } else if (req == null) {
            throw new BadRequestException("Request cant be null.");
        }

        PagoSueldo pago = getPagoOrThrowForUpdate(id);
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario not found"));

        assertCanManagePayroll(usuario);


        pago.agregarMovimientoParcial(
                req.medioPago(),
                req.montoParcial(),
                req.numeroOperacion(),
                req.comentarios()
        );

        pago = pagoSueldoRepo.save(pago);

        return toResponse(pago);
    }


    @Transactional
    public PagoSueldoResponse reemplazarMovimientoParcial(Long id, ReemplazarMovimientoParcialRequest req, Long usuarioId) {

        if (usuarioId == null) { //TODO: REVISAR MANERA EN LA QUE SE ADQUIERE USUARIO
            throw new BadRequestException("Usuario Id cant be null");
        } else if (req == null) {
            throw new BadRequestException("Request cant be null.");
        }
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario not found."));

        assertCanManagePayroll(usuario);

        PagoSueldo pago = getPagoOrThrowForUpdate(id);

        pago.reemplazarMovimientoParcial(
                req.movimientoIdOriginal(),
                req.nuevoMedioPago(),
                req.nuevoMontoParcial(),
                req.nuevoNumeroOperacion(),
                req.nuevoComentario(),
                usuario,
                req.motivo()
        );

        pago = pagoSueldoRepo.save(pago);

        return toResponse(pago);
    }

    @Transactional
    public PagoSueldoResponse anularMovimientoParcial(Long id, AnularMovimientoParcialRequest req, Long usuarioId) {
        if (req == null) {
            throw new BadRequestException("Request cant be null");
        }
        if (usuarioId == null) {
            throw new BadRequestException("Usuario Id cant be null");
        }

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario not found."));

        assertCanManagePayroll(usuario);

        PagoSueldo pago = getPagoOrThrowForUpdate(id);

        pago.anularMovimientoParcial(
                req.movimientoId(),
                usuario,
                req.motivo()
        );

        return toResponse(pagoSueldoRepo.save(pago));
    }


    @Transactional
    public PagoSueldoResponse anularPagoSueldo(Long id, AnularPagoSueldoRequest req, Long usuarioId) {

         if (usuarioId == null) {
            throw new BadRequestException("Usuario Id cant be null");
         } else if (req == null) {
             throw new BadRequestException("Request cant be null.");
         }

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario not found."));


        PagoSueldo pago = getPagoOrThrowForUpdate(id);

        assertCanManagePayroll(usuario);

        pago.anularPagoSueldo(usuario, req.motivo());

        pago = pagoSueldoRepo.save(pago);

        return toResponse(pago);
    }

    public PagoSueldo getPagoOrThrow(Long id) {
        if (id == null) {
            throw new BadRequestException("Id cant be null");
        }

        return pagoSueldoRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Pago not found."));
    }

    public PagoSueldo getPagoOrThrowForUpdate(Long id) {
        if (id == null) {
            throw new BadRequestException("Id cant be null");
        }

        return pagoSueldoRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Pago not found."));
    }


    private PagoSueldoResponse toResponse(PagoSueldo p) {
        return new PagoSueldoResponse(
                p.getId(),
                p.getEmpleado().getId(),
                p.getComedor().getId(),
                p.getPeriodoInicio(),
                p.getPeriodoFin(),
                p.getContrato(),
                p.getFuncionEmpleado(),
                p.getFechaPago(),
                p.getMontoTotal(),
                p.getEstado(),
                p.getObservaciones(),
                p.getCreadoEn(),
                p.getMovimientos().stream()
                        .map(this::toMovimientoResponse)
                        .toList()
        );
    }

    private MovimientoPagoSueldoResponse toMovimientoResponse(MovimientoPagoSueldo m) {
        return new MovimientoPagoSueldoResponse(
                m.getId(),
                m.getMedioPago(),
                m.getMonto(),
                m.getFechaHora(),
                m.getComentarios(),
                m.getNumeroOperacion(),
                m.getEstadoMovimientoPagoSueldo()
        );
    }




    private void assertCanManagePayroll(Usuario usuario) { //TODO: REMOVE AND REPLACE WITH ACTUAL SECURITY AUTH
        if (usuario == null) {
            throw new UnauthorizedException("User not authenticated.");
        } else if (usuario.getRol() == null) {
            throw new ForbiddenException("Rol unconfigured for user.");

        }

        Boolean allowed =
                usuario.getRol() == UsuarioRol.ADMINISTRACION ||
                usuario.getRol() == UsuarioRol.RECURSOS_HUMANOS;

        if (!allowed) {
            throw new ForbiddenException("User does not have the permissions for this method.");
        }

    }

}
