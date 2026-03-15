package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.Comedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComedorRepository extends JpaRepository<Comedor, Long> {
}
