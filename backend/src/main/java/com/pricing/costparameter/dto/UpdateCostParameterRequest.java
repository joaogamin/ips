package com.pricing.costparameter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateCostParameterRequest(
        @NotNull(message = "O valor não pode ser nulo")
        @PositiveOrZero(message = "O valor não pode ser negativo")
        BigDecimal valor
) {}
