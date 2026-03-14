package com.lpc.gestioncomedores.controllers;

import com.lpc.gestioncomedores.dtos.cierreCaja.CierreCajaResponse;
import com.lpc.gestioncomedores.dtos.movimiento.AnulacionMovimientoResponse;
import com.lpc.gestioncomedores.dtos.movimiento.AnularMovimientoRequest;
import com.lpc.gestioncomedores.dtos.movimiento.CreateMovimientoRequest;
import com.lpc.gestioncomedores.dtos.movimiento.MovimientoResponse;
import com.lpc.gestioncomedores.services.MovimientoService;
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
@RequestMapping("/api/movimiento")
@RequiredArgsConstructor
public class MovimientoController{
    private final MovimientoService movimientoService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MovimientoResponse> createMovimiento(
            Authentication authentication,
            @Valid @RequestBody CreateMovimientoRequest request
    ){
        MovimientoResponse responseBody = this.movimientoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MovimientoResponse>> getAllMovimientos(){
        List<MovimientoResponse> movimientos = movimientoService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(movimientos);
    }

    @PostMapping("/{movimientoId}/anular")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MovimientoResponse> anularMovimiento(
            Authentication authentication,
            @PathVariable @Positive Long movimientoId,
            @RequestBody @Valid AnularMovimientoRequest request
    ){
        return ResponseEntity.ok(movimientoService.anularMovimiento(request, authentication, movimientoId));
    }

    @GetMapping("/anulacion/{movimientoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnulacionMovimientoResponse> getAnulacionByMovimientoId(
            @PathVariable @Positive Long movimientoId
    ){
        return ResponseEntity.ok(movimientoService.getAnulacion(movimientoId));
    }
}