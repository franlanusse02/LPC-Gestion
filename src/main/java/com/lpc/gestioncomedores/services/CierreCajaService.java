package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.cierreCaja.*;
import com.lpc.gestioncomedores.exceptions.AlreadyRegisteredException;
import com.lpc.gestioncomedores.exceptions.BadRequestException;
import com.lpc.gestioncomedores.exceptions.NotFoundException;
import com.lpc.gestioncomedores.models.CierreCaja;
import com.lpc.gestioncomedores.models.PuntoDeVenta;
import com.lpc.gestioncomedores.models.Usuario;
import com.lpc.gestioncomedores.models.utils.AnulacionCierre;
import com.lpc.gestioncomedores.repositories.CierreCajaRepository;
import com.lpc.gestioncomedores.repositories.PuntoDeVentaRepository;
import com.lpc.gestioncomedores.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CierreCajaService {
    private final CierreCajaRepository cierreCajaRepository;
    private final PuntoDeVentaRepository puntoDeVentaRepository;
    private final UsuarioRepository usuarioRepository;

    public CierreCajaResponse create(CreateCierreCajaRequest req, Authentication authentication){
        Optional<PuntoDeVenta> puntoDeVentaOpt = puntoDeVentaRepository.findById(req.puntoVentaId());
        if(puntoDeVentaOpt.isEmpty()){
            throw new NotFoundException("Punto de venta no encontrado");
        }
        PuntoDeVenta puntoDeVenta = puntoDeVentaOpt.get();
        if (cierreCajaRepository.existsByFechaOperacionAndPuntoDeVentaAndAnulacionIsNull(req.fechaOperacion(), puntoDeVenta)) {
            throw new AlreadyRegisteredException("Ya existe un cierre para esta fecha.");
        } else if (req.fechaOperacion().isAfter(LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")))) {
            throw new BadRequestException("Fecha de operacion no puede ser posterior a hoy.");
        }
        Usuario usuario = usuarioRepository.getReferenceById(Long.parseLong(authentication.getName()));
        CierreCaja cierreCaja = new CierreCaja(
                puntoDeVenta,
                req.fechaOperacion(),
                usuario,
                req.totalPlatosVendidos(),
                req.comentarios()
        );
        cierreCaja = cierreCajaRepository.save(cierreCaja);
        return new CierreCajaResponse(cierreCaja);
    }

    public List<CierreCajaResponse> getAll(Authentication authentication){
        List<CierreCaja> cierreCajas = this.cierreCajaRepository.findAll()
                .stream().filter(c -> c.getCreadoPor().getCuil() == Long.parseLong(authentication.getName())).toList();
        return cierreCajas.stream().map(CierreCajaResponse::new).toList();
    }
    @Transactional
    public List<DetailedCierreCajaResponse> getAllDetailed(){
        List<CierreCaja> cierreCajas = this.cierreCajaRepository.findAll();
        return cierreCajas.stream().map(DetailedCierreCajaResponse::new).toList();
    }

    public DetailedCierreCajaResponse getDetailedById(Long id){
        Optional<CierreCaja> cierreCaja = this.cierreCajaRepository.findById(id);
        if(cierreCaja.isEmpty()){
            throw new NotFoundException("No se encontro ese cierre");
        }
        else return new DetailedCierreCajaResponse(cierreCaja.get());
    }

    public CierreCajaResponse getById(Long id){
        Optional<CierreCaja> cierreCaja = this.cierreCajaRepository.findById(id);
        if(cierreCaja.isEmpty()){
            throw new NotFoundException("No se encontro ese cierre");
        }
        else return new CierreCajaResponse(cierreCaja.get());
    }

    public CierreCajaResponse anularCierre(Long cierreId, Authentication authentication, AnularCierreCajaRequest request){

        Usuario usuario = usuarioRepository.getReferenceById(Long.parseLong(authentication.getName()));
        CierreCaja cierreCaja = cierreCajaRepository.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));

        cierreCaja.anularCierre(usuario, request.motivo());

        cierreCaja = cierreCajaRepository.save(cierreCaja);
        return new CierreCajaResponse(cierreCaja);

    }

    public AnulacionCierreResponse getAnulacion(Long cierreId) {
        CierreCaja cierreCaja = cierreCajaRepository.findById(cierreId)
                .orElseThrow(() -> new NotFoundException("No se encontro el cierre."));
        if (cierreCaja.getAnulacion() == null) {
            throw new NotFoundException("Cierre no tiene anulacion");
        }

        AnulacionCierre anulacion = cierreCaja.getAnulacion();

        return new AnulacionCierreResponse(anulacion);
    }
}
