package com.pricing.notebook.pricing;

import java.math.BigDecimal;

public record CostParameters(
        BigDecimal taxaConversao,
        BigDecimal aliquotaImportacao,
        BigDecimal freteFaixa1Fixo,
        BigDecimal freteFaixa2Fixo,
        BigDecimal freteFaixa2Taxa,
        BigDecimal freteFaixa3Fixo,
        BigDecimal freteFaixa3Taxa,
        BigDecimal aliquotaSP,
        BigDecimal aliquotaGO,
        BigDecimal aliquotaRS,
        BigDecimal aliquotaAM,
        BigDecimal aliquotaBA,
        BigDecimal margemLucro,
        BigDecimal icmsNaBaseMargem
) {}
