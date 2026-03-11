package com.lpc.gestioncomedores.models.proveedores;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "proveedores")
public class Proveedor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String taxId;

    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    private List<FacturaProveedor> facturas = new ArrayList<>();

}
