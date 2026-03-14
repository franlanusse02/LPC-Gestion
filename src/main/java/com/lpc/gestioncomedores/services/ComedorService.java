package com.lpc.gestioncomedores.services;


import com.lpc.gestioncomedores.exceptions.BadRequestException;
import com.lpc.gestioncomedores.exceptions.NotFoundException;
import com.lpc.gestioncomedores.models.Comedor;
import com.lpc.gestioncomedores.repositories.ComedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComedorService {

    private final ComedorRepository comedorRepo;

    public ComedorResponse create(CrreateComedorRequest req) {
       if (req == null) {
           throw new BadRequestException("CreateComedorRequest no puede ser null.");
       }

       Comedor comedor = new Comedor(req.id, req.name);
       comedorRepo.save(comedor);
       return toResponse(comedor);
    }

    public List<ComedorResponse> getComedores () {

       List<Comedor> comedores = comedorRepo.findAll();

       return comedores.stream()
               .map(ComedorResponse::toResponse)
               .toList();
    }

    public ComedorResponse getComedorById(Long id) {
       if (id == null) {
           throw new BadRequestException("Comedor id no puede ser null.");
       }
       Comedor comedor = comedorRepo.findById(id)
               .orElseThrow(() -> new NotFoundException("No se encontro comedor con ese id"));
       return toResponse(comedor);
    }

    private ComedorResponse toResponse(Comedor comedor) {
       return new ComedorResponse(
               comedor.getId(),
               comedor.getName(),
               comedor.getPuntosDeVenta()
       );
    }




}
