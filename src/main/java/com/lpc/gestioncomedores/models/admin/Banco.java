package com.lpc.gestioncomedores.models.admin;

import jakarta.persistence.*;

@Entity
@Table(name = "bancos")
public class Banco {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Boolean active;
}
