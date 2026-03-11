package com.lpc.gestioncomedores.controllers;


import com.lpc.gestioncomedores.dtos.facturaproveedor.*;
import com.lpc.gestioncomedores.services.facturaproveedor.FacturaProveedorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas-proveedor")
@RequiredArgsConstructor
@Validated
public class FacturaProveedorController {

    private final FacturaProveedorService service;

    // POST -> createFacturaProveedor
    @PostMapping
    public ResponseEntity<FacturaProveedorResponse> create(@Valid @RequestBody CreateFacturaProveedorRequest req) {
       return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    // GET -> "/{id}" -> getById
    @GetMapping("/{id}")
    public ResponseEntity<FacturaProveedorResponse>  getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // PATCH -> "/{id}" -> editar factura
    @PatchMapping("/{id}")
    public ResponseEntity<FacturaProveedorResponse> patch(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateFacturaProveedorRequest req
    ) {
        return ResponseEntity.ok(service.patch(id, req));
    }

    // POST -> "/{id]/approve" -> aprobar factura
    @PostMapping("/{id}/approve")
    public ResponseEntity<FacturaProveedorResponse> approve(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ApproveFacturaProveedorRequest req
    ) {
        return ResponseEntity.ok(service.approve(id, req));
    }

    // POST -> "/{id}/pay" -> pagar factura
    @PostMapping("/{id}/pay")
    public ResponseEntity<FacturaProveedorResponse> pay(
            @PathVariable @Positive Long id,
            @Valid @RequestBody PayFacturaProveedorRequest req
    ) {
        return ResponseEntity.ok(service.pay(id, req));
    }

    // POST -> "/{id}/cancel" -> cancelar factura
    @PostMapping("/{id}/cancel")
    public ResponseEntity<FacturaProveedorResponse> cancel(
            @PathVariable @Positive Long id,
            @RequestParam(required = false) String observaciones
    ) {
        return ResponseEntity.ok(service.cancel(id, observaciones));
    }


}
