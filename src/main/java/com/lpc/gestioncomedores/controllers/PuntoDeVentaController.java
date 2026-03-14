package com.lpc.gestioncomedores.controllers;

import com.lpc.gestioncomedores.dtos.ptoVenta.CreatePuntoDeVentaRequest;
import com.lpc.gestioncomedores.dtos.ptoVenta.PuntoDeVentaResponse;
import com.lpc.gestioncomedores.services.PuntoDeVentaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puntodeventa")
@RequiredArgsConstructor
public class PuntoDeVentaController {

    private final PuntoDeVentaService service;

    @PostMapping
    public ResponseEntity<PuntoDeVentaResponse> create(@Valid @RequestBody CreatePuntoDeVentaRequest req) {
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
