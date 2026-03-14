package com.lpc.gestioncomedores.services;

import com.lpc.gestioncomedores.dtos.auth.LoginRequest;
import com.lpc.gestioncomedores.dtos.auth.LoginResponse;
import com.lpc.gestioncomedores.dtos.auth.RegisterRequest;
import com.lpc.gestioncomedores.exceptions.AlreadyRegisteredException;
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

    public LoginResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByCuil(request.cuil())) {
            throw new AlreadyRegisteredException("Ya existe un usuario con el CUIL " + request.cuil());
        }
        if(request.cuil().toString().length() != 11){
            throw new IllegalArgumentException("Ingrese un cuil valido sin guiones.");
        }

        Usuario usuario = new Usuario(
                request.cuil(),
                request.name(),
                request.rol(),
                passwordEncoder.encode(request.password()));

        usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generateToken(usuario.getCuil(), usuario.getName(), usuario.getRol().name());
        return new LoginResponse(token, usuario.getCuil(), usuario.getName(), usuario.getRol().name());
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(request.getCuil()),
                        request.getPassword()));

        Usuario usuario = buscarPorCuil(request.getCuil());
        String token = jwtTokenProvider.generateToken(usuario.getCuil(), usuario.getName(), usuario.getRol().name());
        return new LoginResponse(token, usuario.getCuil(), usuario.getName(), usuario.getRol().name());
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
