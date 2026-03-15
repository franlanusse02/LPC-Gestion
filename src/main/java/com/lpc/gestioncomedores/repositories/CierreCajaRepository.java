package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.CierreCaja;
import com.lpc.gestioncomedores.models.PuntoDeVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {
    boolean existsByFechaOperacionAndPuntoDeVentaAndAnulacionIsNull(LocalDate fechaOperacion, PuntoDeVenta puntoDeVenta);
}
