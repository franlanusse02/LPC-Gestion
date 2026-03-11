package com.lpc.gestioncomedores.repositories;

import com.lpc.gestioncomedores.models.admin.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BancoRepository extends JpaRepository<Banco, Long> {
}
