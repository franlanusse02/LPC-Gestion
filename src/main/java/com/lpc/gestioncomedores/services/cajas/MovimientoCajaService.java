package com.lpc.gestioncomedores.services.cajas;


import com.lpc.gestioncomedores.dtos.cajas.requests.*;
import com.lpc.gestioncomedores.dtos.cajas.responses.CierreCajaLineaResponse;
import com.lpc.gestioncomedores.dtos.cajas.responses.CierreCajaResponse;
import com.lpc.gestioncomedores.dtos.cajas.responses.MovimientoCajaResponse;
import com.lpc.gestioncomedores.exceptions.childs.BadRequestException;
import com.lpc.gestioncomedores.exceptions.childs.ForbiddenException;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.models.cajas.CierreCaja;
import com.lpc.gestioncomedores.models.cajas.CierreCajaLinea;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.comedores.PuntoDeVenta;
import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoCaja;
import com.lpc.gestioncomedores.models.personas.Usuario;
import com.lpc.gestioncomedores.repositories.*;
import lombok.RequiredArgsConstructor;
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
    public CierreCajaResponse crearCierre(CrearCierreCajaRequest req, Long usuarioId) {
        if (req == null) {
            throw new BadRequestException("CrearCierreCajaRequest no puede ser null.");
        } else if (usuarioId == null) {
            throw new BadRequestException("Usuario Id no puede ser null");
        }
        assertCanAddCaja(usuarioId);

        Comedor comedor = comedorRepo.findById(req.comedorId())
                .orElseThrow(() -> new NotFoundException("No se encontro el comedor"));
        PuntoDeVenta puntoDeVenta = puntoVentaRepo.findById(req.puntoVentaId())
                .orElseThrow(() -> new NotFoundException("No se encontro el punto de venta"));

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("No se encontro el usuario."));

        CierreCaja cierre = CierreCaja.crear(
                comedor,
                puntoDeVenta,
                req.fechaOperacion(),
                usuario,
                req.observaciones(),
                req.totalPlatosVendidos()
        );
        cierreCajaRepo.save(cierre);
        return toResponse(cierre);


    }
    public CierreCajaResponse getCierreById(Long cierreId) {
        if (cierreId == null) {
            throw new BadRequestException("Cierre Id no puede ser null");
        }

        CierreCaja cierre = cierreCajaRepo.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre"));
        return toResponse(cierre);
    }

    public List<CierreCajaResponse> getAllCierres() {
        return cierreCajaRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CierreCajaResponse actualizarObservaciones(
            ActualizarObservacionesCierreCajaRequest req,
            Long cierreId,
            Long usuarioId
    ){
        if (req == null) {
            throw new BadRequestException("Request no puede ser null");
        } else if (cierreId == null) {
            throw new BadRequestException("Cierre Id no puede ser null");
        }else if (usuarioId == null) {
            throw new BadRequestException("Usuario id no puede ser null.");
        }
        CierreCaja cierre = cierreCajaRepo.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));

        assertCanManageCaja(usuarioId);

        cierre.actualizarObservaciones(req.observaciones());
        cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    @Transactional
    public CierreCajaResponse actualizarTotalPlatosVendidos(
            ActualizarTotalPlatosVendidosRequest req,
            Long cierreId,
            Long usuarioId
    ) {
        if (req == null) {
            throw new BadRequestException("Request no puede ser null");
        } else if (cierreId == null) {
            throw new BadRequestException("Cierre Id no puede ser null");
        }else if (usuarioId == null) {
            throw new BadRequestException("Usuario id no puede ser null.");
        }

        CierreCaja cierre = cierreCajaRepo.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));
        assertCanAddCaja(usuarioId);

        cierre.actualizarTotalPlatosVendidos(req.totalPlatosVendidos());
        cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    @Transactional
    public CierreCajaResponse agregarLineaACierre(
            AgregarLineaCierreCajaRequest req,
            Long cierreId,
            Long usuarioId
    ){
        if (req == null) {
            throw new BadRequestException("Request no puede ser null");
        } else if (cierreId == null) {
            throw new BadRequestException("Cierre Id no puede ser null");
        }else if (usuarioId == null) {
            throw new BadRequestException("Usuario id no puede ser null.");
        }

        CierreCaja cierre = cierreCajaRepo.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));
        assertCanAddCaja(usuarioId);

        CierreCajaLinea linea = CierreCajaLinea.crear(req.monto(), req.medioPago());

        cierre.agregarLinea(linea);
        cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    @Transactional
    public CierreCajaResponse reemplazarLineaCierreCaja(
            ReemplazarLineaCierreCajaRequest req,
            Long cierreId,
            Long usuarioId
    ){
        if (req == null) {
            throw new BadRequestException("Request no puede ser null");
        } else if (cierreId == null) {
            throw new BadRequestException("Cierre Id no puede ser null");
        }else if (usuarioId == null) {
            throw new BadRequestException("Usuario id no puede ser null.");
        }
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("No se encontro el usuario."));
        CierreCaja cierre = cierreCajaRepo.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));
        assertCanAddCaja(usuarioId);

        CierreCajaLinea linea = CierreCajaLinea.crear(req.monto(), req.medioPago());

        cierre.reemplazarLinea(linea, req.lineaViejaId(), usuario, req.motivo());
        cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    public CierreCajaLineaResponse anularLineaDeCierre(
            AnularLineaCierreCajaRequest req,
            Long cierreId,
            Long usuarioId

    ){
        if (req == null) {
            throw new BadRequestException("Request no puede ser null");
        } else if (cierreId == null) {
            throw new BadRequestException("Cierre Id no puede ser null");
        }else if (usuarioId == null) {
            throw new BadRequestException("Usuario id no puede ser null.");
        }
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("No se encontro el usuario."));
        CierreCaja cierre = cierreCajaRepo.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));
        assertCanAddCaja(usuarioId);

        CierreCajaLinea linea = cierre.getLineas().stream()
                .filter(l -> req.lineaId().equals(l.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No se encontro la linea"));

        linea.anularLinea(usuario, req.motivo());
        cierreCajaRepo.save(cierre);
        return toResponse(linea);
    }

    public CierreCajaResponse finalizarCierre(Long cierreId, Long usuarioId) {
        if (cierreId == null) {
            throw new BadRequestException("Cierre Id no puede ser null");
        }else if (usuarioId == null) {
            throw new BadRequestException("Usuario id no puede ser null.");
        }
        assertCanManageCaja(usuarioId);

        CierreCaja cierre = cierreCajaRepo.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));

        cierre.finalizarCierre();

        cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    public CierreCajaResponse anularCierre(AnularCierreCajaRequest req, Long cierreId, Long usuarioId) {
        if (req == null) {
            throw new BadRequestException("Request no puede ser null");
        } else if (cierreId == null) {
            throw new BadRequestException("Cierre Id no puede ser null");
        }else if (usuarioId == null) {
            throw new BadRequestException("Usuario id no puede ser null.");
        }
        assertCanManageCaja(usuarioId); //TODO: CHECK IF ENCARGADOS CAN

        CierreCaja cierre = cierreCajaRepo.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("No se encontro el usuario."));

        cierre.anular(usuario, req.motivo());
        cierreCajaRepo.save(cierre);
        return toResponse(cierre);
    }

    public List<MovimientoCajaResponse> getAllMovimientosCaja() {
        return movimientoCajaRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }


    //HELPER METHODS

    private void assertCanManageCaja(Long usuarioId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("No se encontro el usuario."));

        UsuarioRol rol = usuario.getRol();
        if (rol != UsuarioRol.ADMINISTRACION && rol != UsuarioRol.WEB_ADMIN) {
            throw new ForbiddenException("No tienes permiso para manejar cierres creados.");
        }
    }

    private void assertCanAddCaja(Long usuarioId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("No se encontro el usuario."));

        UsuarioRol rol = usuario.getRol();
        if (rol != UsuarioRol.ADMINISTRACION && rol != UsuarioRol.ENCARGADO && rol != UsuarioRol.WEB_ADMIN) {
            throw new ForbiddenException("No tienes permiso para crear cierres.");
        }
    }

    private CierreCajaResponse toResponse(CierreCaja cierre) {
        return new CierreCajaResponse(
                cierre.getId(),
                cierre.getComedor().getId(),
                cierre.getPuntoDeVenta().getId(),
                cierre.getFechaOperacion(),
                cierre.getCreadoEn(),
                cierre.getCreadoPor().getId(),
                cierre.getEstado(),
                cierre.getObservaciones(),
                cierre.getAnuladoEn(),
                cierre.getAnuladoPor() != null ? cierre.getAnuladoPor().getId() : null,
                cierre.getMotivoAnulacion(),
                cierre.calcularMontoTotal(),
                cierre.getLineas().stream()
                        .map(this::toResponse)
                        .toList(),
                cierre.getMovimientos().stream()
                        .map(this::toResponse)
                        .toList(),
                cierre.getTotalPlatosVendidos()
        );
    }

    private CierreCajaLineaResponse toResponse(CierreCajaLinea linea) {
        return new CierreCajaLineaResponse(
                linea.getId(),
                linea.getMedioPago(),
                linea.getMonto(),
                linea.getEstado(),
                linea.getAnuladoEn(),
                linea.getAnuladoPor() != null ? linea.getAnuladoPor().getId() : null,
                linea.getMotivoAnulacion()
        );
    }

    private MovimientoCajaResponse toResponse(MovimientoCaja movimiento) {
        return new MovimientoCajaResponse(
                movimiento.getId(),
                movimiento.getPuntoDeVenta().getId(),
                movimiento.getCategoria(),
                movimiento.getUsuario().getId(),
                movimiento.getCierreCaja() != null ? movimiento.getCierreCaja().getId() : null,
                movimiento.getMonto(),
                movimiento.getMedioPago(),
                movimiento.getSentido(),
                movimiento.getFechaHora(),
                movimiento.getComentarios(),
                movimiento.getEstadoMovimientoCaja(),
                movimiento.getAnuladoEn(),
                movimiento.getAnuladoPor() != null ? movimiento.getAnuladoPor().getId() : null,
                movimiento.getMotivoAnulacion()
        );
    }





}
