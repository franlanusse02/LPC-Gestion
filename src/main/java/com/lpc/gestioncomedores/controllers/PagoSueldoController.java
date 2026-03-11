package com.lpc.gestioncomedores.controllers;


import com.lpc.gestioncomedores.dtos.sueldos.*;
import com.lpc.gestioncomedores.services.sueldos.PagoSueldoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/sueldos/pagos")
@RequiredArgsConstructor
@Validated
public class PagoSueldoController {

    private final PagoSueldoService service;

    //POST /api/sueldos/pagos
    @PostMapping
    public ResponseEntity<PagoSueldoResponse> create(@Valid @RequestBody CreatePagoSueldoRequest req, @RequestParam @Positive Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req, userId));
    }

    //GET /api/sueldos/pagos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PagoSueldoResponse> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    //POST /api/sueldos/pagos/{id}/movimientos
    @PostMapping("/{id}/movimientos")
    public ResponseEntity<PagoSueldoResponse> createMovimientoPagoSueldo(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AgregarMovimientoParcialRequest req,
            @RequestParam @Positive Long userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarMovimientoParcial(id, req, userId));

    }

    //PUT /api/sueldos/pagos/{id}/movimientos/reemplazar
    @PutMapping("/{id}/movimientos/reemplazar")
    public ResponseEntity<PagoSueldoResponse> reemplazarMovimientoPagoSueldo(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ReemplazarMovimientoParcialRequest req,
            @RequestParam @Positive Long userId
    ) {
        return ResponseEntity.ok(service.reemplazarMovimientoParcial(id, req, userId));
    }

    //PUT /api/sueldos/pagos/{id}/movimientos/anular
    @PutMapping("/{id}/movimientos/anular")
    public ResponseEntity<PagoSueldoResponse> anularMovimientoPagoSueldo(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AnularMovimientoParcialRequest req,
            @RequestParam @Positive Long userId
    ) {
        return ResponseEntity.ok(service.anularMovimientoParcial(id, req, userId));
    }

    //PUT /api/sueldos/pagos/{id}/anular
    @PutMapping("/{id}/anular")
    public ResponseEntity<PagoSueldoResponse> anular(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AnularPagoSueldoRequest req,
            @RequestParam @Positive Long userId
    ) {
        return ResponseEntity.ok(service.anularPagoSueldo(id, req, userId));
    }
}
