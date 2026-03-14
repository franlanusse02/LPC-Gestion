package com.lpc.gestioncomedores.controllers;

import com.lpc.gestioncomedores.dtos.comedor.ComedorResponse;
import com.lpc.gestioncomedores.dtos.comedor.CreateComedorRequest;
import com.lpc.gestioncomedores.services.ComedorService;
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
@RequestMapping("/api/comedor")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ComedorController {

    private final ComedorService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ComedorResponse> crearComedor(
            Authentication authentication,
            @Valid @RequestBody CreateComedorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ComedorResponse>> getComedores() {
        return ResponseEntity.ok(service.getComedores());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ComedorResponse> getComedorById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getComedorById(id));
    }
}