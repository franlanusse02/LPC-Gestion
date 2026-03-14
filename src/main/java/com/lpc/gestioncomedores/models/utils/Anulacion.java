package com.lpc.gestioncomedores.models.utils;

import java.time.Instant;

import com.lpc.gestioncomedores.models.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public abstract class Anulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant fechaAnulacion;

    @Column(nullable = false)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id", nullable = false)
    private Usuario anuladoPor;
}
