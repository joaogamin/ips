package com.pricing.notebook.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LotPurchaseRequest(
        @NotBlank(message = "O nome do notebook não pode ser vazio")
        String nome,

        @NotNull(message = "O custo em D$ não pode ser nulo")
        @DecimalMin(value = "0.01", message = "O custo em D$ deve ser maior que zero")
        BigDecimal custoDev,

        @NotNull(message = "A quantidade não pode ser nula")
        @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
        Integer quantidade
) {}
