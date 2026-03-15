package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.cierreCaja.CierreCajaResponse;
import com.lpc.gestioncomedores.dtos.comedor.ComedorDetailedResponse;
import com.lpc.gestioncomedores.dtos.comedor.ComedorResponse;
import com.lpc.gestioncomedores.dtos.comedor.CreateComedorRequest;
import com.lpc.gestioncomedores.dtos.ptoVenta.PuntoDeVentaDetailedResponse;
import com.lpc.gestioncomedores.exceptions.BadRequestException;
import com.lpc.gestioncomedores.exceptions.NotFoundException;
import com.lpc.gestioncomedores.models.Comedor;
import com.lpc.gestioncomedores.repositories.CierreCajaRepository;
import com.lpc.gestioncomedores.repositories.ComedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComedorService {

    private final ComedorRepository comedorRepository;
    private final CierreCajaRepository cierreCajaRepository;

    public ComedorResponse create(CreateComedorRequest req) {
        if (req == null) {
            throw new BadRequestException("CreateComedorRequest no puede ser null.");
        }

        Comedor comedor = new Comedor(null, req.nombre(), new ArrayList<>());
        comedorRepository.save(comedor);
        return ComedorResponse.from(comedor);
    }

    public List<ComedorResponse> getComedores() {
        return comedorRepository.findAll().stream()
                .map(ComedorResponse::from)
                .toList();
    }

    public ComedorResponse getComedorById(Long id) {
        if (id == null) {
            throw new BadRequestException("Comedor id no puede ser null.");
        }
        Comedor comedor = comedorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontro comedor con ese id"));
        return ComedorResponse.from(comedor);
    }

    public List<ComedorDetailedResponse> getComedoresWithCierres() {
        List<Comedor> comedores = comedorRepository.findAll();

        Map<Long, List<CierreCajaResponse>> cierresPorPuntoVenta = cierreCajaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        cierre -> cierre.getPuntoDeVenta().getId(),
                        Collectors.mapping(CierreCajaResponse::new, Collectors.toList())
                ));

        return comedores.stream()
                .map(comedor -> new ComedorDetailedResponse(
                        comedor.getId(),
                        comedor.getName(),
                        comedor.getPuntosDeVenta().stream()
                                .map(pv -> new PuntoDeVentaDetailedResponse(
                                        pv.getId(),
                                        pv.getNombre(),
                                        comedor.getId(),
                                        cierresPorPuntoVenta.getOrDefault(pv.getId(), List.of())
                                ))
                                .toList()
                ))
                .toList();
    }

}
