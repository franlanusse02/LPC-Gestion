package com.lpc.gestioncomedores.controllers;

import com.lpc.gestioncomedores.dtos.cierreCaja.*;
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
    public ResponseEntity<List<CierreCajaResponse>> getAllCierres(Authentication authentication){
        List<CierreCajaResponse> cierres= cierreCajaService.getAll(authentication);
        return ResponseEntity.status(HttpStatus.OK).body(cierres);
    }

    @GetMapping("/detailed")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<List<DetailedCierreCajaResponse>> getAllDetailedCierres(){
        List<DetailedCierreCajaResponse> cierres = cierreCajaService.getAllDetailed();
        return ResponseEntity.status(HttpStatus.OK).body(cierres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCierreById(
            @PathVariable Long id
    ){
        return ResponseEntity.status(HttpStatus.OK).body(
                cierreCajaService.getById(id)
        );
    }

    @GetMapping("/detailed/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<?> getDetailedCierreById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(
                cierreCajaService.getDetailedById(id)
        );
    }

    @PostMapping("/{cierreId}/anular")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<CierreCajaResponse> anularCierreCaja(
            Authentication authentication,
            @PathVariable @Positive Long cierreId,
            @RequestBody @Valid AnularCierreCajaRequest request
            ){
        return ResponseEntity.ok(cierreCajaService.anularCierre(cierreId, authentication, request));
    }

    @GetMapping("/anulacion/{cierreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<AnulacionCierreResponse> getAnulacionCierre(
            @PathVariable @Positive Long cierreId
    ){
        return ResponseEntity.ok(cierreCajaService.getAnulacion(cierreId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<CierreCajaResponse> patchCierreCaja(
            @PathVariable @Positive Long id,
            @RequestBody @Valid PatchCierreCajaRequest request){
        return ResponseEntity.ok(cierreCajaService.patchCierreCaja(request, id));
    }


}
