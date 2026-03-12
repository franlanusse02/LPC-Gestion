package com.lpc.gestioncomedores.models.comedores;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "punto_de_venta")
public class PuntoDeVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;
}



