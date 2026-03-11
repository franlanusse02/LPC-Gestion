package com.lpc.gestioncomedores.models.movimientos.childs;

import com.lpc.gestioncomedores.models.proveedores.FacturaProveedor;
import com.lpc.gestioncomedores.models.movimientos.Movimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("PAGO_PROVEEDORES")
public class MovimientoPagoProveedor extends Movimiento {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", unique = true)
    private FacturaProveedor factura;


}
