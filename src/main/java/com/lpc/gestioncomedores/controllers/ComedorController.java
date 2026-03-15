package com.lpc.gestioncomedores.controllers;

import com.lpc.gestioncomedores.dtos.comedor.ComedorResponse;
import com.lpc.gestioncomedores.dtos.comedor.CreateComedorRequest;
import com.lpc.gestioncomedores.services.ComedorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comedor")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ComedorController {

    private final ComedorService service;


    //ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComedorResponse> crearComedor(
            Authentication authentication,
            @Valid @RequestBody CreateComedorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    //ADMIN || CONTABILIDAD
    //GET MAPING getComedoresWithCierres


    @GetMapping
    @PreAuthorize("isAuthenticated() and (!#detailed or hasAnyRole('ADMIN', 'CONTABILIDAD'))")
    public ResponseEntity<List<?>> getComedores(
            @RequestParam(name = "detailed", defaultValue = "false") boolean detailed
    ) {
        return detailed
                ? ResponseEntity.ok(service.getComedoresWithCierres())
                : ResponseEntity.ok(service.getComedores());
    }


    //ALL
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ComedorResponse> getComedorById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getComedorById(id));
    }
}