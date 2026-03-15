package com.lpc.gestioncomedores.dtos.cierreCaja;

import jakarta.validation.constraints.NotBlank;

public record AnularCierreCajaRequest (
        @NotBlank(message = "Motivo no puede estar vacio.") String motivo
){
}
