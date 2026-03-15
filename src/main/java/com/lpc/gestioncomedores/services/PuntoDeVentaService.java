package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.ptoVenta.CreatePuntoDeVentaRequest;
import com.lpc.gestioncomedores.dtos.ptoVenta.PuntoDeVentaResponse;
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
    private final ComedorRepository comedorRepository;

    public PuntoDeVentaResponse create(CreatePuntoDeVentaRequest req) {
        if (req == null) {
            throw new BadRequestException("CreatePuntoDeVentaRequest no puede ser null.");
        }

        Comedor comedor = comedorRepository.findById(req.comedorId())
                .orElseThrow(() -> new NotFoundException("No se encontro comedor con ese id"));

        // PuntoDeVenta needs @AllArgsConstructor — see note below
        PuntoDeVenta puntoDeVenta = new PuntoDeVenta(null, comedor, req.nombre());
        puntoDeVentaRepository.save(puntoDeVenta);
        return PuntoDeVentaResponse.from(puntoDeVenta);
    }

    public List<PuntoDeVentaResponse> getPuntosDeVenta() {
        return puntoDeVentaRepository.findAll().stream()
                .map(PuntoDeVentaResponse::from)
                .toList();
    }

    public PuntoDeVentaResponse getPuntoDeVentaById(Long id) {
        if (id == null) {
            throw new BadRequestException("Punto de venta id no puede ser null.");
        }
        PuntoDeVenta puntoDeVenta = puntoDeVentaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontro punto de venta con ese id"));
        return PuntoDeVentaResponse.from(puntoDeVenta);
    }
}
