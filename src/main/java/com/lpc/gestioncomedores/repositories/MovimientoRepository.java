package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
}
