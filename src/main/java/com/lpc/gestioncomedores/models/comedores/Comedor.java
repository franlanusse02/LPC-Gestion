package com.lpc.gestioncomedores.models.comedores;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comedores")
public class Comedor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "comedor", fetch = FetchType.LAZY)
    private List<PuntoDeVenta> puntosDeVenta = new ArrayList<>();
}
