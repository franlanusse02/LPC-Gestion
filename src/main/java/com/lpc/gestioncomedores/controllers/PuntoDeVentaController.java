package com.lpc.gestioncomedores.controllers;


import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/puntodeventa")
@RequiredArgsConstructor
public class PuntoDeVentaController {

    private final PuntoDeVentaService service;


    @PostMapping
    public ResponseEntity<PuntoDeVentaResponse> create(CrearPuntoDeVentaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<PuntoDeVentaResponse>> getPuntosDeVenta() {
        return ResponseEntity.ok(service.getPuntosDeVenta());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PuntoDeVentaResponse> getPuntoDeVentaById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getPuntoDeVentaById(id));
    }
}
