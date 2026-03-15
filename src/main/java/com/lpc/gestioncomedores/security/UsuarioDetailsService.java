package com.lpc.gestioncomedores.security;

import com.lpc.gestioncomedores.models.Usuario;
import com.lpc.gestioncomedores.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String cuil) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCuil(Long.parseLong(cuil))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + cuil));

        return new org.springframework.security.core.userdetails.User(
                String.valueOf(usuario.getCuil()),
                usuario.getPasshash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
    }
}
