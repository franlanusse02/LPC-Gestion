package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoPagoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoPagoProveedorRepository extends JpaRepository<MovimientoPagoProveedor, Long> {
    boolean existsByFactura_Id(Long facturaId);
}
