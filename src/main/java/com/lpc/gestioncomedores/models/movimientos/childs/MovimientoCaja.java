package com.lpc.gestioncomedores.models.movimientos.childs;

import com.lpc.gestioncomedores.models.personas.Empleado;
import com.lpc.gestioncomedores.models.comedores.PuntoDeVenta;
import com.lpc.gestioncomedores.models.enums.CategoriaCaja;
import com.lpc.gestioncomedores.models.movimientos.Movimiento;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("CAJA")
public class MovimientoCaja extends Movimiento {

    @ManyToOne(fetch = FetchType.LAZY)
    private PuntoDeVenta puntoDeVenta;

    @Enumerated(EnumType.STRING)
    private CategoriaCaja categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_mov_caja_empleado"))
    private Empleado empleado;

}
