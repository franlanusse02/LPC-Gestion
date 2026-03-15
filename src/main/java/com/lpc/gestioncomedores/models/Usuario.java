package com.lpc.gestioncomedores.models;

import com.lpc.gestioncomedores.models.enums.UsuarioRol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {

    @Id
    private Long cuil;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioRol rol;

    @Column(nullable = false)
    private String passhash;


}
