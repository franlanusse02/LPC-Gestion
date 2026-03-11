package com.lpc.gestioncomedores.models.personas;

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

    //Crear columna Id y mapear a
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id", foreignKey = @ForeignKey(name = "fk_usuario_empleado"))
    private Empleado empleado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioRol rol;

    @Column(nullable = false)
    private String passhash;


}
