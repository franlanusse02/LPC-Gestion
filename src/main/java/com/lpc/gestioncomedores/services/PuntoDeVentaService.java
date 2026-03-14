package com.lpc.gestioncomedores.services;


import com.lpc.gestioncomedores.exceptions.BadRequestException;
import com.lpc.gestioncomedores.exceptions.NotFoundException;
import com.lpc.gestioncomedores.models.Comedor;
import com.lpc.gestioncomedores.models.PuntoDeVenta;
import com.lpc.gestioncomedores.repositories.ComedorRepository;
import com.lpc.gestioncomedores.repositories.PuntoDeVentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PuntoDeVentaService {
    private final PuntoDeVentaRepository puntoDeVentaRepository;

    public PuntoDeVentaResponse create(CrreatePuntoDeVentaRequest req) {
        if (req == null) {
            throw new BadRequestException("CreatePuntoDeVentaRequest no puede ser null.");
        }

        PuntoDeVenta puntoDeVenta = new PuntoDeVenta(req.id, req.name);
        puntoDeVentaRepository.save(puntoDeVenta);
        return toResponse(puntoDeVenta);
    }

    public List<PuntoDeVentaResponse> getPuntosDeVenta () {

        List<PuntoDeVenta> puntosDeVenta = puntoDeVentaRepository.findAll();

        return puntosDeVenta.stream()
                .map(PuntoDeVentaResponse::toResponse)
                .toList();
    }

    public PuntoDeVentaResponse getPuntoDeVentaById(Long id) {
        if (id == null) {
            throw new BadRequestException("Punto de venta id no puede ser null.");
        }
        PuntoDeVenta puntoDeVenta = puntoDeVentaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontro punto de venta con ese id"));
        return toResponse(puntoDeVenta);
    }

    private PuntoDeVentaResponse toResponse(PuntoDeVenta puntoDeVenta) {
        return new ComedorResponse(
                puntoDeVenta.getId(),
                puntoDeVenta.getNombre(),
                puntoDeVenta.getComedor()
        );
    }
}
