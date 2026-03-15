package com.lpc.gestioncomedores.servicios;

import com.lpc.gestioncomedores.dtos.auth.LoginRequest;
import com.lpc.gestioncomedores.dtos.auth.LoginResponse;
import com.lpc.gestioncomedores.dtos.auth.RegisterRequest;
import com.lpc.gestioncomedores.models.Usuario;
import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import com.lpc.gestioncomedores.repositories.UsuarioRepository;
import com.lpc.gestioncomedores.security.JwtTokenProvider;
import com.lpc.gestioncomedores.services.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(20304050607L, "Martin", UsuarioRol.ADMIN, "hashed_password");
    }

    // ── registrar ────────────────────────────────────────────────────────────

    @Test
    void registrar_withNewCuil_shouldReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest(20304050607L, "Martin", UsuarioRol.ADMIN, "password123");

        when(usuarioRepository.existsByCuil(request.cuil())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtTokenProvider.generateToken(20304050607L, "Martin", "ADMIN")).thenReturn("jwt-token");

        LoginResponse response = usuarioService.registrar(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.cuil()).isEqualTo(20304050607L);
        assertThat(response.rol()).isEqualTo("ADMIN");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registrar_withExistingCuil_shouldThrowException() {
        RegisterRequest request = new RegisterRequest(20304050607L, "Martin", UsuarioRol.ADMIN, "password123");
        when(usuarioRepository.existsByCuil(request.cuil())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un usuario con el CUIL");

        verify(usuarioRepository, never()).save(any());
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_shouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        // Use reflection or a setter — adjust if AuthRequest uses records
        org.springframework.test.util.ReflectionTestUtils.setField(request, "cuil", 20304050607L);
        org.springframework.test.util.ReflectionTestUtils.setField(request, "password", "password123");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("20304050607", "password123"));
        when(usuarioRepository.findByCuil(20304050607L)).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generateToken(20304050607L, "Martin", "ADMIN")).thenReturn("jwt-token");

        LoginResponse response = usuarioService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.cuil()).isEqualTo(20304050607L);
    }

    @Test
    void login_withInvalidCredentials_shouldThrowException() {
        LoginRequest request = new LoginRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "cuil", 20304050607L);
        org.springframework.test.util.ReflectionTestUtils.setField(request, "password", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> usuarioService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ── buscarPorCuil ────────────────────────────────────────────────────────

    @Test
    void buscarPorCuil_withExistingCuil_shouldReturnUsuario() {
        when(usuarioRepository.findByCuil(20304050607L)).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.buscarPorCuil(20304050607L);

        assertThat(result.getCuil()).isEqualTo(20304050607L);
        assertThat(result.getRol()).isEqualTo(UsuarioRol.ADMIN);
    }

    @Test
    void buscarPorCuil_withMissingCuil_shouldThrowException() {
        when(usuarioRepository.findByCuil(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorCuil(99999999999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ── listarTodos ──────────────────────────────────────────────────────────

    @Test
    void listarTodos_shouldReturnAllUsuarios() {
        Usuario otro = new Usuario(27000000001L, "Martin", UsuarioRol.ADMIN, "hash2");
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario, otro));

        List<Usuario> result = usuarioService.listarTodos();

        assertThat(result).hasSize(2);
    }

    // ── eliminar ─────────────────────────────────────────────────────────────

    @Test
    void eliminar_withExistingCuil_shouldDeleteUsuario() {
        when(usuarioRepository.existsByCuil(20304050607L)).thenReturn(true);

        usuarioService.eliminar(20304050607L);

        verify(usuarioRepository).deleteById(20304050607L);
    }

    @Test
    void eliminar_withMissingCuil_shouldThrowException() {
        when(usuarioRepository.existsByCuil(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.eliminar(99999999999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(usuarioRepository, never()).deleteById(any());
    }
}
