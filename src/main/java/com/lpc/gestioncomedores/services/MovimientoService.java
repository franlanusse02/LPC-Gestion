package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.movimiento.AnulacionMovimientoResponse;
import com.lpc.gestioncomedores.dtos.movimiento.AnularMovimientoRequest;
import com.lpc.gestioncomedores.dtos.movimiento.CreateMovimientoRequest;
import com.lpc.gestioncomedores.dtos.movimiento.MovimientoResponse;
import com.lpc.gestioncomedores.exceptions.NotFoundException;
import com.lpc.gestioncomedores.models.CierreCaja;
import com.lpc.gestioncomedores.models.Movimiento;
import com.lpc.gestioncomedores.models.Usuario;
import com.lpc.gestioncomedores.models.utils.Anulacion;
import com.lpc.gestioncomedores.models.utils.AnulacionMovimiento;
import com.lpc.gestioncomedores.repositories.CierreCajaRepository;
import com.lpc.gestioncomedores.repositories.MovimientoRepository;
import com.lpc.gestioncomedores.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService{
    private final MovimientoRepository movimientoRepository;
    private final CierreCajaRepository cierreCajaRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoResponse create(CreateMovimientoRequest req){
        CierreCaja cierreCaja = cierreCajaRepository.getReferenceById(req.cierreCajaId());
        Movimiento movimiento = new Movimiento(req.monto(), req.medioPago(), cierreCaja, req.comentarios());
        movimiento = this.movimientoRepository.save(movimiento);
        return new MovimientoResponse(movimiento);
    }

    public List<MovimientoResponse> getAll(){
        List<Movimiento> movimientos = this.movimientoRepository.findAll();
        return movimientos.stream().map(MovimientoResponse::new).toList();
    }

    public MovimientoResponse anularMovimiento(AnularMovimientoRequest request, Authentication authentication, Long movimientoId){
        Usuario usuario = usuarioRepository.getReferenceById(Long.parseLong(authentication.getName()));
        Movimiento movimiento = movimientoRepository.findById(movimientoId)
                .orElseThrow(() -> new NotFoundException("No se encontro el movimiento."));
        movimiento.anularMovimiento(request.motivo(), usuario);
        movimiento = this.movimientoRepository.save(movimiento);
        return new MovimientoResponse(movimiento);
    }

    public AnulacionMovimientoResponse getAnulacion(Long movimientoId) {
        Movimiento movimiento = movimientoRepository.findById(movimientoId)
                .orElseThrow(() -> new NotFoundException("No se encontro el movimiento."));
        if (movimiento.getAnulacion() == null) {
            throw new NotFoundException("Movimiento no tiene anulacion");
        }

        AnulacionMovimiento anulacion = movimiento.getAnulacion();

        return new AnulacionMovimientoResponse(anulacion);
    }
}
