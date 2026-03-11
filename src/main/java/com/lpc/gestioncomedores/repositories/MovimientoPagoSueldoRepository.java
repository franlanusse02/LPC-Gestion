package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoPagoSueldo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoPagoSueldoRepository extends JpaRepository<MovimientoPagoSueldo, Long> {

}
