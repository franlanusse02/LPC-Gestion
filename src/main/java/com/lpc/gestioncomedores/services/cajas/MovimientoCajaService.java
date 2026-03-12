package com.lpc.gestioncomedores.services.cajas;

import com.lpc.gestioncomedores.dtos.cajas.requests.*;
import com.lpc.gestioncomedores.dtos.cajas.responses.CierreCajaLineasResponse;
import com.lpc.gestioncomedores.dtos.cajas.responses.CierreCajaResponse;
import com.lpc.gestioncomedores.dtos.cajas.responses.MovimientoCajaResponse;
import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.exceptions.childs.ForbiddenException;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.exceptions.childs.UnauthorizedException;
import com.lpc.gestioncomedores.models.cajas.CierreCaja;
import com.lpc.gestioncomedores.models.cajas.CierreCajaLinea;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.comedores.PuntoDeVenta;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCaja;
import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoCaja;
import com.lpc.gestioncomedores.models.personas.Usuario;
import com.lpc.gestioncomedores.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoCajaService {
    private final CierreCajaRepository cierreCajaRepo;
    private final MovimientoCajaRepository movimientoCajaRepo;
    private final ComedorRepository comedorRepo;
    private final UsuarioRepository usuarioRepo;
    private final PuntoDeVentaRepository puntoVentaRepo;


    // METHODS

    @Transactional
    public CierreCajaResponse create(CrearCierreCajaRequest req, Long usuarioId) {
        validarCreateRequest(req);
        Usuario usuario = getUsuarioOrThrow(usuarioId);
        assertCanManageCaja(usuario);
        validarExistsDuplicate(req);
        Comedor comedor = getComedorOrThrow(req.comedorId());
        PuntoDeVenta puntoDeVenta = getPuntoVentaOrThrow(req.puntoVentaId());

        if (!puntoDeVenta.getComedor().getId().equals(comedor.getId())) {
            throw new BadRequestException("El punto de venta no pertenece al comedor indicado");
        }

        CierreCaja cierre = CierreCaja.create(
                comedor,
                puntoDeVenta,
                req.fechaOperacion(),
                usuario,
                Instant.now(),
                req.observaciones()
        );

        cierreCajaRepo.save(cierre);

        return toResponse(cierre);

    }

    public CierreCajaResponse getById(Long id) {
        CierreCaja cierre = getCierreOrThrow(id);
        return toResponse(cierre);
    }

    @Transactional
    public CierreCajaResponse agregarLinea(Long cierreId, AgregarLineaCierreCajaRequest req, Long usuarioId) {
        Usuario usuario = getUsuarioOrThrow(usuarioId);
        assertCanManageCaja(usuario);
        CierreCaja cierre = getCierreOrThrowForUpdate(cierreId);
        CierreCajaLinea linea = buildLineaFromRequest(req);
        cierre.agregarLinea(linea);
        cierreCajaRepo.save(cierre);



        return toResponse(cierre);


    }

    @Transactional
    public CierreCajaResponse reemplazarLinea(Long cierreId, ReemplazarLineaCierreCajaRequest req, Long usuarioId) {
        Usuario usuario = getUsuarioOrThrow(usuarioId);
        assertCanManageCaja(usuario);
        CierreCaja cierre = getCierreOrThrowForUpdate(cierreId);
        CierreCajaLinea linea = buildLineaFromRequest(req);
        cierre.anularLinea(req.lineaIdOriginal(), usuario, req.motivo());
        cierre.agregarLinea(linea);
        cierreCajaRepo.save(cierre);



        return toResponse(cierre);
    }

    @Transactional
    public CierreCajaResponse anularLinea(Long cierreId, AnularLineaCierreCajaRequest req, Long usuarioId) {
        Usuario usuario = getUsuarioOrThrow(usuarioId);
        CierreCaja cierre = getCierreOrThrowForUpdate(cierreId);
        if (req == null) {
            throw new BadRequestException("Request no puede ser null.");
        }

        assertCanManageCaja(usuario);

        cierre.anularLinea(req.lineaId(), usuario, req.motivo());
        cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    @Transactional
    public CierreCajaResponse anular(Long cierreId, AnularCierreCajaRequest req, Long usuarioId) {
        Usuario usuario = getUsuarioOrThrow(usuarioId);
        CierreCaja cierre = getCierreOrThrowForUpdate(cierreId);
        if (req == null) {
            throw new BadRequestException("Request no puede ser null.");
        }

        assertCanManageCaja(usuario);

        cierre.anular(usuario, req.motivo());
        cierreCajaRepo.save(cierre);

        return toResponse(cierre);
    }

    @Transactional
    public CierreCajaResponse cerrar(Long cierreId, Long usuarioId) {
        Usuario usuario = getUsuarioOrThrow(usuarioId);
        CierreCaja cierre = getCierreOrThrowForUpdate(cierreId);

        assertCanManageCaja(usuario);

        cierre.cerrar();
        cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    @Transactional
    public MovimientoCajaResponse crearMovimientoAporte(CrearMovimientoCajaAporteRequest req, Long usuarioId) {
        if (req == null) {
            throw new BadRequestException("Request no puede ser null.");
        }

        Usuario usuario = getUsuarioOrThrow(usuarioId);
        assertCanManageCaja(usuario);

        Comedor comedor = getComedorOrThrow(req.comedorId());
        PuntoDeVenta puntoDeVenta = getPuntoVentaOrThrow(req.puntoVentaId());

        if (!puntoDeVenta.getComedor().getId().equals(comedor.getId())) {
            throw new BadRequestException("El punto de venta no pertenece al comedor indicado.");
        }

        MovimientoCaja movimiento = MovimientoCaja.createAporte(
                comedor,
                puntoDeVenta,
                usuario,
                req.monto(),
                req.medioPago(),
                Instant.now(),
                req.comentarios()
        );

        movimiento = movimientoCajaRepo.save(movimiento);
        return toMovimientoResponse(movimiento);
    }
    @Transactional
    public CierreCajaResponse actualizarObservaciones(
            Long cierreId,
            ActualizarObservacionesCierreCajaRequest req,
            Long usuarioId
    ) {
        if (req == null) {
            throw new BadRequestException("Request no puede ser null.");
        }

        Usuario usuario = getUsuarioOrThrow(usuarioId);
        assertCanManageCaja(usuario);

        CierreCaja cierre = getCierreOrThrowForUpdate(cierreId);

        cierre.actualizarObservaciones(req.observaciones());

        cierre = cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    public List<CierreCajaResponse> getAllCierres() {
        return cierreCajaRepo.findAll(
                        Sort.by(Sort.Direction.DESC, "fechaOperacion")
                                .and(Sort.by(Sort.Direction.DESC, "id"))
                ).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MovimientoCajaResponse> getAllMovimientos(Long usuarioId) {
        Usuario usuario = getUsuarioOrThrow(usuarioId);
        assertCanManageCaja(usuario);

        return movimientoCajaRepo.findAll(
                Sort.by(Sort.Direction.DESC, "fechaHora")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        ).stream().map(this::toMovimientoResponse).toList();
    }


    // PRIVATE HELPER METHODS

    private CierreCaja getCierreOrThrow(Long id) {
        if (id == null) {
            throw new BadRequestException("Id no puede ser null.");
        }
        return cierreCajaRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("CierreCaja no encontrado."));
    }

    private CierreCaja getCierreOrThrowForUpdate(Long id) {
        if (id == null) {
            throw new BadRequestException("Id no puede ser null.");
        }
        return cierreCajaRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("CierreCaja no encontrado."));
    }

    private Usuario getUsuarioOrThrow(Long userId) {
        if (userId == null) {
            throw new BadRequestException("UsuarioId no puede ser null.");
        }
        return usuarioRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado."));
    }

    private Comedor getComedorOrThrow(Long comedorId) {
        if (comedorId == null) {
            throw new BadRequestException("ComedorId no puede ser null.");
        }
        return comedorRepo.findById(comedorId)
                .orElseThrow(() -> new NotFoundException("Comedor no encontrado."));
    }

    private PuntoDeVenta getPuntoVentaOrThrow(Long puntoVentaId) {
        if (puntoVentaId == null) {
            throw new BadRequestException("PuntoDeVentaId no puede ser null.");
        }
        return puntoVentaRepo.findById(puntoVentaId)
                .orElseThrow(() -> new NotFoundException("PuntoDeVenta no encontrado."));
    }

    private void validarCreateRequest(CrearCierreCajaRequest req) {
        if (req == null) {
            throw new BadRequestException("Request no puede ser null.");
        }
    }

    private void validarExistsDuplicate(CrearCierreCajaRequest req) {
        boolean duplicado = cierreCajaRepo.existsByComedorIdAndPuntoDeVentaIdAndFechaOperacionAndEstadoNot(
                req.comedorId(),
                req.puntoVentaId(),
                req.fechaOperacion(),
                EstadoCierreCaja.ANULADO
        );
        if (duplicado) {
            throw new BadRequestException("Ya existe un cierre para ese comedor, punto de venta y fecha.");
        }
    }

    private void assertCanManageCaja(Usuario usuario) {
        if (usuario == null) {
            throw new UnauthorizedException("Usuario no autenticado.");
        } else if (usuario.getRol() == null) {
            throw new ForbiddenException("Rol no configurado para el usuario.");
        }

        boolean allowed =
                usuario.getRol() == UsuarioRol.ADMINISTRACION ||
                        usuario.getRol() == UsuarioRol.RECURSOS_HUMANOS;

        if (!allowed) {
            throw new ForbiddenException("El usuario no tiene permisos para gestionar caja.");
        }
    }

    private CierreCajaLinea buildLineaFromRequest(AgregarLineaCierreCajaRequest req) {
        if (req == null) {
            throw new BadRequestException("Request no puede ser null.");
        }

        return CierreCajaLinea.create(
                req.tipoVenta(),
                req.monto(),
                req.precioMenuUnitarioSnapshot(),
                req.cobradoEvento(),
                req.modoPagoEvento(),
                req.numeroOperacion(),
                req.numeroOrdenEvento(),
                req.cantidadPaxEvento(),
                req.lugarPisoEvento()
        );
    }

    private CierreCajaLinea buildLineaFromRequest(ReemplazarLineaCierreCajaRequest req) {
        if (req == null) {
            throw new BadRequestException("Request no puede ser null.");
        }

        return CierreCajaLinea.create(
                req.tipoVenta(),
                req.monto(),
                req.precioMenuUnitarioSnapshot(),
                req.cobradoEvento(),
                req.modoPagoEvento(),
                req.numeroOperacion(),
                req.numeroOrdenEvento(),
                req.cantidadPaxEvento(),
                req.lugarPisoEvento()
        );
    }

    private CierreCajaResponse toResponse(CierreCaja c) {
        return new CierreCajaResponse(
                c.getId(),
                c.getComedor().getId(),
                c.getPuntoDeVenta().getId(),
                c.getFechaOperacion(),
                c.getCreadoEn(),
                c.getCreadoPor().getId(),
                c.getEstado(),
                c.getObservaciones(),
                c.getAnuladoEn(),
                c.getAnuladoPor() != null ? c.getAnuladoPor().getId() : null,
                c.getMotivoAnulacion(),
                c.calcularTotalCierre(),
                c.getLineas().stream()
                        .map(this::toLineaResponse)
                        .toList(),
                c.getMovimientos().stream()
                        .map(this::toMovimientoResponse)
                        .toList()
        );
    }

    private CierreCajaLineasResponse toLineaResponse(CierreCajaLinea l) {
        return new CierreCajaLineasResponse(
                l.getId(),
                l.getTipoVenta(),
                l.getMonto(),
                l.getPrecioMenuUnitarioSnapshot(),
                l.getCobradoEvento(),
                l.getModoPagoEvento(),
                l.getNumeroOperacion(),
                l.getNumeroOrdenEvento(),
                l.getCantidadPaxEvento(),
                l.getLugarPisoEvento(),
                l.getEstado(),
                l.getClaveUnicaCierreLinea(),
                l.getAnuladoEn(),
                l.getAnuladoPor() != null ? l.getAnuladoPor().getId() : null,
                l.getMotivoAnulacion()
        );
    }

    private MovimientoCajaResponse toMovimientoResponse(MovimientoCaja m) {
        return new MovimientoCajaResponse(
                m.getId(),
                m.getPuntoDeVenta() != null ? m.getPuntoDeVenta().getId() : null,
                m.getCategoria(),
                m.getUsuario() != null ? m.getUsuario().getId() : null,
                m.getCierreCaja() != null ? m.getCierreCaja().getId() : null,
                m.getMonto(),
                m.getMedioPago(),
                m.getSentido(),
                m.getFechaHora(),
                m.getComentarios(),
                m.getEstadoMovimientoCaja(),
                m.getAnuladoEn(),
                m.getAnuladoPor() != null ? m.getAnuladoPor().getId() : null,
                m.getMotivoAnulacion()
        );
    }


}
