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

    // ALL
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CierreCajaResponse> createCierreCaja(
            Authentication authentication,
            @Valid @RequestBody CreateCierreCajaRequest request
            ){
            CierreCajaResponse responseBody = this.cierreCajaService.create(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    // ADMIN || CONTABILIDAD
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<List<CierreCajaResponse>> getAllCierres(){
        List<CierreCajaResponse> cierres = cierreCajaService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(cierres);
    }
    // ALL/ENCARGADO
    // GET MAPPING getCierresByUsuario/getCierresByMe


    //ADMIN || CONTABILIDAD
    @PostMapping("/{cierreId}/anular")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<CierreCajaResponse> anularCierreCaja(
            Authentication authentication,
            @PathVariable @Positive Long cierreId,
            @RequestBody @Valid AnularCierreCajaRequest request
            ){
        return ResponseEntity.ok(cierreCajaService.anularCierre(cierreId, authentication, request));
    }
    //ADMIN || CONTABILIDAD
    @GetMapping("/anulacion/{cierreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<AnulacionCierreResponse> getAnulacionCierre(
            @PathVariable @Positive Long cierreId
    ){
        return ResponseEntity.ok(cierreCajaService.getAnulacion(cierreId));
    }

}
