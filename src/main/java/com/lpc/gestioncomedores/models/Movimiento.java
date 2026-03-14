package com.lpc.gestioncomedores.models;

import com.lpc.gestioncomedores.models.enums.MedioPago;
import com.lpc.gestioncomedores.models.utils.Anulacion;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedioPago medioPago;

    @Column(nullable = false)
    private Instant fechaHora = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cierre_caja_id", nullable = false)
    private CierreCaja cierreCaja;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Anulacion anulacion = null;

    private String comentarios = "";

    public Movimiento(BigDecimal monto, MedioPago medioPago, CierreCaja cierreCaja, String comentarios){
        this.monto = monto;
        this.medioPago = medioPago;
        this.cierreCaja = cierreCaja;
        this.comentarios = comentarios;
    }
}
