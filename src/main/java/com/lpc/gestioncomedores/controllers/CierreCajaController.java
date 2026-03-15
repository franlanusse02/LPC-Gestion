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
    public ResponseEntity<List<?>> getAllCierres(
            @RequestParam(name = "detailed", required = false, defaultValue = "false") Boolean detailed
    ){
        List<?> cierres;
        if(detailed) {
            cierres = cierreCajaService.getAllDetailed();
        }else {
            cierres = cierreCajaService.getAll();
        }
        return ResponseEntity.status(HttpStatus.OK).body(cierres);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTABILIDAD')")
    public ResponseEntity<?> getCierreById(
            @RequestParam(name = "detailed", required = false, defaultValue = "false") Boolean detailed,
            @PathVariable Long id
    ){
        if(detailed) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    cierreCajaService.getDetailedById(id)
                    );
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(
                    cierreCajaService.getById(id)
            );
        }
    }
    // ADMIN || CONTABILIDAD
    // GET MAPPING getCierresByUsuario

    //  ENCARGADO
    // GET MAPPING getCierresByMe

    //ADMIN || CONTABILIDAD
    // GET MAPPING getCierresByComedorId

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
