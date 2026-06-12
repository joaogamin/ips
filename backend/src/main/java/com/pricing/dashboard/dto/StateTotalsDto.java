package com.pricing.dashboard.dto;

import java.math.BigDecimal;

public record StateTotalsDto(
        BigDecimal totalSP,
        BigDecimal totalGO,
        BigDecimal totalRS,
        BigDecimal totalAM,
        BigDecimal totalBA
) {}
