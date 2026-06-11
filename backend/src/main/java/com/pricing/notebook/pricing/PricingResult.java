package com.pricing.notebook.pricing;

import java.math.BigDecimal;

public record PricingResult(
        BigDecimal custoBrl,
        BigDecimal valorAcumulado,
        BigDecimal frete,
        BigDecimal custoFinal,
        BigDecimal precoSemIcms,
        BigDecimal lucroBruto,
        BigDecimal reinvestimentoTI,
        BigDecimal precoSP,
        BigDecimal precoGO,
        BigDecimal precoRS,
        BigDecimal precoAM,
        BigDecimal precoBA,
        BigDecimal lucroBrutoSP,
        BigDecimal lucroBrutoGO,
        BigDecimal lucroBrutoRS,
        BigDecimal lucroBrutoAM,
        BigDecimal lucroBrutoBA
) {}
