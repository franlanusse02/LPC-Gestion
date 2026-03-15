package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.Movimiento;
import com.lpc.gestioncomedores.models.enums.MedioPago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    boolean existsByCierreCaja_IdAndMedioPago(Long cierreCajaId, MedioPago medioPago);
}
