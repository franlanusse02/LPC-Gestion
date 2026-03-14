package com.lpc.gestioncomedores.dtos.cierreCaja;

import jakarta.validation.constraints.NotNull;

public record AnularCierreCajaRequest (
        @NotNull String motivo
){
}
