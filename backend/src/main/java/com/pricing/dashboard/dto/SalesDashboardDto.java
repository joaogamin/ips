package com.pricing.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalesDashboardDto(
        List<NotebookSalesSummary> notebooks,
        StateTotalsDto totaisPorEstado,
        StateTotalsDto lucroBrutoPorEstado,
        BigDecimal reinvestimentoTI,
        BigDecimal totalGeral
) {}
