package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.enums.ContratoEmpleado;
import com.lpc.gestioncomedores.models.sueldos.PagoSueldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import jakarta.persistence.LockModeType;

public interface PagoSueldoRepository extends JpaRepository<PagoSueldo, Long> {
    @Query("""
        select (count(p) > 0) from PagoSueldo p
        where p.empleado.id = :empleadoId
          and p.periodoInicio = :fechaInicio
          and p.periodoFin = :fechaFin
          and p.contrato = :contratoEmpleado
          and p.funcionEmpleado = :funcion
          and p.comedor.id = :comedorId
    """)
    boolean existsDuplicate(
            @Param("empleadoId") Long empleadoId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("contratoEmpleado") ContratoEmpleado contratoEmpleado,
            @Param("funcion") String funcion,
            @Param("comedorId") Long comedorId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PagoSueldo p where p.id = :id")
    Optional<PagoSueldo> findByIdForUpdate(@Param("id") Long id);
}
