package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.proveedores.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
}
