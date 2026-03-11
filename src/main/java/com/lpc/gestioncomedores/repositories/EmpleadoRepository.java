package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.personas.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
}
