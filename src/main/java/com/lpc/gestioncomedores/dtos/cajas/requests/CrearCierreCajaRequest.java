package com.lpc.gestioncomedores.dtos.cajas.requests;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CrearCierreCajaRequest(
        @NotNull @Positive Long comedorId,
        @NotNull @Positive Long puntoVentaId,
        @NotNull LocalDate fechaOperacion,
        String observaciones
        ){}
