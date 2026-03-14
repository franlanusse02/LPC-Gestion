package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.movimiento.CreateMovimientoRequest;
import com.lpc.gestioncomedores.dtos.movimiento.MovimientoResponse;
import com.lpc.gestioncomedores.models.CierreCaja;
import com.lpc.gestioncomedores.models.Movimiento;
import com.lpc.gestioncomedores.repositories.CierreCajaRepository;
import com.lpc.gestioncomedores.repositories.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService{
    private final MovimientoRepository movimientoRepository;
    private final CierreCajaRepository cierreCajaRepository;

    public MovimientoResponse create(CreateMovimientoRequest req, Authentication authentication){
        CierreCaja cierreCaja = cierreCajaRepository.getReferenceById(req.cierreCajaId());
        Movimiento movimiento = new Movimiento(req.monto(), req.medioPago(), cierreCaja, req.comentarios());
        movimiento = this.movimientoRepository.save(movimiento);
        return new MovimientoResponse(movimiento);
    }

    public List<MovimientoResponse> getAll(){
        List<Movimiento> movimientos = this.movimientoRepository.findAll();
        return movimientos.stream().map(MovimientoResponse::new).toList();
    }
}