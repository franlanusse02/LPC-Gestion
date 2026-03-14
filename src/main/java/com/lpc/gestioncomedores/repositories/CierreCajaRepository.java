package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {
    boolean existsByFechaOperacion(LocalDate fechaOperacion);
}
