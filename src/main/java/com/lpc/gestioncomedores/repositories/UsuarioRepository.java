package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.personas.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
