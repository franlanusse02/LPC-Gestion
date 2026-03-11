package com.lpc.gestioncomedores.models.personas;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "empleados")
public class Empleado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tax_id", unique = true, nullable = false)
    private String taxId;

    @Column(nullable = false)
    private String nombre;


    //domicilio, etc

}
