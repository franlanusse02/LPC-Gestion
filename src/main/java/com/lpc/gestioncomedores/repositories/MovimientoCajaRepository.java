package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
}
