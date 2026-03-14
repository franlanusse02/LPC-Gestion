package com.lpc.gestioncomedores.controllers;

import com.lpc.gestioncomedores.dtos.auth.LoginRequest;
import com.lpc.gestioncomedores.dtos.auth.LoginResponse;
import com.lpc.gestioncomedores.dtos.auth.RegisterRequest;
import com.lpc.gestioncomedores.dtos.auth.UsuarioResponse;
import com.lpc.gestioncomedores.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponse> getMe(Authentication authentication) {
        Long cuil = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.buscarPorCuil(cuil)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(
                usuarioService.listarTodos().stream()
                        .map(UsuarioResponse::from)
                        .toList());
    }

    @GetMapping("/{cuil}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> getByCuil(@PathVariable Long cuil) {
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.buscarPorCuil(cuil)));
    }

    @DeleteMapping("/{cuil}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long cuil) {
        usuarioService.eliminar(cuil);
        return ResponseEntity.noContent().build();
    }
}
