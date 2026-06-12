package com.pricing.dashboard.dto;

import java.math.BigDecimal;

public record NotebookSalesSummary(
        String nome,
        Integer quantidadeEstoque,
        BigDecimal precoSP,
        BigDecimal precoGO,
        BigDecimal precoRS,
        BigDecimal precoAM,
        BigDecimal precoBA
) {}
