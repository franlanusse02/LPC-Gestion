package com.lpc.gestioncomedores.controllers;

import com.lpc.gestioncomedores.dtos.auth.AuthRequest;
import com.lpc.gestioncomedores.dtos.auth.AuthResponse;
import com.lpc.gestioncomedores.dtos.auth.RegisterRequest;
import com.lpc.gestioncomedores.dtos.auth.UsuarioResponse;
import com.lpc.gestioncomedores.models.Usuario;
import com.lpc.gestioncomedores.security.JwtTokenProvider;
import com.lpc.gestioncomedores.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = usuarioService.registrar(request);
        String token = jwtTokenProvider.generateToken(usuario.getCuil(), usuario.getRol().name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, usuario.getCuil(), usuario.getRol().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(request.getCuil()),
                        request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Usuario usuario = usuarioService.buscarPorCuil(request.getCuil());
        String token = jwtTokenProvider.generateToken(usuario.getCuil(), usuario.getRol().name());
        return ResponseEntity.ok(new AuthResponse(token, usuario.getCuil(), usuario.getRol().name()));
    }

    // ─────────────────────────────────────────────
    // AUTHENTICATED
    // ─────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponse> getMe(Authentication authentication) {
        Long cuil = Long.parseLong(authentication.getName());
        Usuario usuario = usuarioService.buscarPorCuil(cuil);
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    // ─────────────────────────────────────────────
    // ADMIN
    // ─────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        List<UsuarioResponse> usuarios = usuarioService.listarTodos()
                .stream()
                .map(UsuarioResponse::from)
                .toList();
        return ResponseEntity.ok(usuarios);
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
