package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.cierreCaja.AnulacionCierreResponse;
import com.lpc.gestioncomedores.dtos.cierreCaja.AnularCierreCajaRequest;
import com.lpc.gestioncomedores.dtos.cierreCaja.CierreCajaResponse;
import com.lpc.gestioncomedores.dtos.cierreCaja.CreateCierreCajaRequest;
import com.lpc.gestioncomedores.dtos.movimiento.AnulacionMovimientoResponse;
import com.lpc.gestioncomedores.exceptions.NotFoundException;
import com.lpc.gestioncomedores.models.CierreCaja;
import com.lpc.gestioncomedores.models.PuntoDeVenta;
import com.lpc.gestioncomedores.models.Usuario;
import com.lpc.gestioncomedores.models.utils.Anulacion;
import com.lpc.gestioncomedores.models.utils.AnulacionCierre;
import com.lpc.gestioncomedores.repositories.CierreCajaRepository;
import com.lpc.gestioncomedores.repositories.PuntoDeVentaRepository;
import com.lpc.gestioncomedores.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CierreCajaService {
    private final CierreCajaRepository cierreCajaRepository;
    private final PuntoDeVentaRepository puntoDeVentaRepository;
    private final UsuarioRepository usuarioRepository;

    public CierreCajaResponse create(CreateCierreCajaRequest req, Authentication authentication){
        PuntoDeVenta puntoDeVenta = puntoDeVentaRepository.getReferenceById(req.puntoVentaId());
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

    public List<CierreCajaResponse> getAll(){
        List<CierreCaja> cierreCajas = this.cierreCajaRepository.findAll();
        return cierreCajas.stream().map(CierreCajaResponse::new).toList();
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
