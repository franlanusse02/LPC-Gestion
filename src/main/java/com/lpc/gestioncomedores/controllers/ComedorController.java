package com.lpc.gestioncomedores.controllers;


import com.lpc.gestioncomedores.services.ComedorService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.function.EntityResponse;

import java.util.List;

@RestController("/api/comedor")
@RequiredArgsConstructor
public class ComedorController {

    private final ComedorService service;



    @PostMapping
    public ResponseEntity<ComedorResponse> crearComedor(@RequestBody CreateComedorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<ComedorResponse>> getComedores() {
        return ResponseEntity.ok(service.getComedores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComedorResponse> getComedorById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getComedorById(id));
    }
}
