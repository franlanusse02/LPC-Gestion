package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.enums.EstadoMovimientoPagoSueldo;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoPagoSueldo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoPagoSueldoRepository extends JpaRepository<MovimientoPagoSueldo, Long> {

}
