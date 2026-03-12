package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.cajas.CierreCaja;
import com.lpc.gestioncomedores.models.enums.EstadoCierreCaja;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {
    boolean existsByComedorIdAndPuntoDeVentaIdAndFechaOperacionAndEstadoNot(
            Long comedorId,
            Long puntoVentaId,
            LocalDate fechaOperacion,
            EstadoCierreCaja estado
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CierreCaja c where c.id = :id")
    Optional<CierreCaja> findByIdForUpdate(@Param("id") Long id);
}
