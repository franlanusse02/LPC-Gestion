package com.lpc.gestioncomedores.models.comedores;

import jakarta.persistence.*;

@Entity
public class PuntoDeVenta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;
}
