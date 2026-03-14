package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.comedor.ComedorResponse;
import com.lpc.gestioncomedores.dtos.comedor.CreateComedorRequest;
import com.lpc.gestioncomedores.exceptions.BadRequestException;
import com.lpc.gestioncomedores.exceptions.NotFoundException;
import com.lpc.gestioncomedores.models.Comedor;
import com.lpc.gestioncomedores.repositories.ComedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComedorService {

    private final ComedorRepository comedorRepo;

    public ComedorResponse create(CreateComedorRequest req) {
        if (req == null) {
            throw new BadRequestException("CreateComedorRequest no puede ser null.");
        }

        Comedor comedor = new Comedor(null, req.nombre(), new ArrayList<>());
        comedorRepo.save(comedor);
        return ComedorResponse.from(comedor);
    }

    public List<ComedorResponse> getComedores() {
        return comedorRepo.findAll().stream()
                .map(ComedorResponse::from)
                .toList();
    }

    public ComedorResponse getComedorById(Long id) {
        if (id == null) {
            throw new BadRequestException("Comedor id no puede ser null.");
        }
        Comedor comedor = comedorRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontro comedor con ese id"));
        return ComedorResponse.from(comedor);
    }
}
