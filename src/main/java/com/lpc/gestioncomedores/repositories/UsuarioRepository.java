package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCuil(Long cuil);

    boolean existsByCuil(Long cuil);
}
