package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.auth.AuthRequest;
import com.lpc.gestioncomedores.dtos.auth.AuthResponse;
import com.lpc.gestioncomedores.dtos.auth.RegisterRequest;
import com.lpc.gestioncomedores.models.Usuario;
import com.lpc.gestioncomedores.repositories.UsuarioRepository;
import com.lpc.gestioncomedores.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByCuil(request.cuil())) {
            throw new RuntimeException("Ya existe un usuario con el CUIL " + request.cuil());
        }

        Usuario usuario = new Usuario(
                request.cuil(),
                request.rol(),
                passwordEncoder.encode(request.password()));

        usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generateToken(usuario.getCuil(), usuario.getRol().name());
        return new AuthResponse(token, usuario.getCuil(), usuario.getRol().name());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(request.getCuil()),
                        request.getPassword()));

        Usuario usuario = buscarPorCuil(request.getCuil());
        String token = jwtTokenProvider.generateToken(usuario.getCuil(), usuario.getRol().name());
        return new AuthResponse(token, usuario.getCuil(), usuario.getRol().name());
    }

    public Usuario buscarPorCuil(Long cuil) {
        return usuarioRepository.findByCuil(cuil)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + cuil));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public void eliminar(Long cuil) {
        if (!usuarioRepository.existsByCuil(cuil)) {
            throw new RuntimeException("Usuario no encontrado: " + cuil);
        }
        usuarioRepository.deleteById(cuil);
    }
}
