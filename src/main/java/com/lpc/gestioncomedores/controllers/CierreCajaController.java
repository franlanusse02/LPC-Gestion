package com.lpc.gestioncomedores.controllers;

import com.lpc.gestioncomedores.dtos.cierreCaja.AnulacionCierreResponse;
import com.lpc.gestioncomedores.dtos.cierreCaja.AnularCierreCajaRequest;
import com.lpc.gestioncomedores.dtos.cierreCaja.CierreCajaResponse;
import com.lpc.gestioncomedores.dtos.cierreCaja.CreateCierreCajaRequest;
import com.lpc.gestioncomedores.services.CierreCajaService;
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
@RequestMapping("/api/cierre")
@RequiredArgsConstructor
public class CierreCajaController {
    private final CierreCajaService cierreCajaService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CierreCajaResponse> createCierreCaja(
            Authentication authentication,
            @Valid @RequestBody CreateCierreCajaRequest request
            ){
            CierreCajaResponse responseBody = this.cierreCajaService.create(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CierreCajaResponse>> getAllCierres(){
        List<CierreCajaResponse> cierres = cierreCajaService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(cierres);
    }

    @PatchMapping("/{cierreId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CierreCajaResponse> anularCierreCaja(
            Authentication authentication,
            @PathVariable @Positive Long cierreId,
            @RequestBody @Valid AnularCierreCajaRequest request
            ){
        return ResponseEntity.ok(cierreCajaService.anularCierre(cierreId, authentication, request));
    }

    @GetMapping("/anulacion/{cierreId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnulacionCierreResponse> getAnulacionCierre(
            @PathVariable @Positive Long cierreId
    ){
        return ResponseEntity.ok(cierreCajaService.getAnulacion(cierreId));
    }

}
