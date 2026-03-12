package com.lpc.gestioncomedores.controllers;


import com.lpc.gestioncomedores.dtos.cajas.requests.*;
import com.lpc.gestioncomedores.dtos.cajas.responses.CierreCajaResponse;
import com.lpc.gestioncomedores.dtos.cajas.responses.MovimientoCajaResponse;
import com.lpc.gestioncomedores.services.cajas.MovimientoCajaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cajas")
@RequiredArgsConstructor
@Validated
public class MovimientoCajaController {

    private final MovimientoCajaService service;

    @PostMapping("/cierres")
    public ResponseEntity<CierreCajaResponse> crear(
            @Valid @RequestBody CrearCierreCajaRequest req,
            @RequestParam @Positive Long usuarioId
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req, usuarioId));
    }

    @GetMapping("/cierres/{id}")
    public ResponseEntity<CierreCajaResponse> getById(
            @PathVariable @Positive Long id
    ){
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/cierres/{id}/lineas")
    public ResponseEntity<CierreCajaResponse> crearLinea(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long usuarioId,
            @RequestBody @Valid AgregarLineaCierreCajaRequest req
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarLinea(id, req, usuarioId));
    }

    @PutMapping("/cierres/{id}/lineas/reemplazar")
    public ResponseEntity<CierreCajaResponse> reemplazarLinea(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long usuarioId,
            @RequestBody @Valid ReemplazarLineaCierreCajaRequest req
    ){
        return ResponseEntity.ok(service.reemplazarLinea(id, req, usuarioId));
    }

    @PutMapping("/cierres/{id}/lineas/anular")
    public ResponseEntity<CierreCajaResponse> anularLinea(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long usuarioId,
            @RequestBody @Valid AnularLineaCierreCajaRequest req
    ){
        return ResponseEntity.ok(service.anularLinea(id, req, usuarioId));
    }

    @PutMapping("/cierres/{id}/observaciones")
    public ResponseEntity<CierreCajaResponse> actualizarObservaciones(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long usuarioId,
            @RequestBody @Valid ActualizarObservacionesCierreCajaRequest req
    ){
        return ResponseEntity.ok(service.actualizarObservaciones(id, req, usuarioId));
    }

    @PutMapping("/cierres/{id}/finalizar")
    public ResponseEntity<CierreCajaResponse> finalizarCierre(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long usuarioId
    ){
        return ResponseEntity.ok(service.cerrar(id, usuarioId));
    }

    @PutMapping("/cierres/{id}/anular")
    public ResponseEntity<CierreCajaResponse> anularCierre(
            @PathVariable @Positive Long id,
            @RequestParam @Positive Long usuarioId,
            @RequestBody @Valid AnularCierreCajaRequest req
    ){
        return  ResponseEntity.ok(service.anular(id, req, usuarioId));
    }

    @PostMapping("/movimientos/aportes")
    public ResponseEntity<MovimientoCajaResponse> crearAporte(
            @RequestParam @Positive Long usuarioId,
            @RequestBody @Valid CrearMovimientoCajaAporteRequest req
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearMovimientoAporte(req, usuarioId));
    }

    @GetMapping("/cierres")
    public ResponseEntity<List<CierreCajaResponse>> getAllCierres() {
        return ResponseEntity.ok(service.getAllCierres());
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoCajaResponse>> getAllMovimientos(
            @RequestParam @Positive Long usuarioId
    ) {
        return ResponseEntity.ok(service.getAllMovimientos(usuarioId));
    }
}
