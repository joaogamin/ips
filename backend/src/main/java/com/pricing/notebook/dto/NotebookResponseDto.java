package com.pricing.notebook.dto;

import com.pricing.notebook.Notebook;
import com.pricing.notebook.pricing.PricingResult;
import com.pricing.shared.util.PrecisionMathUtil;

import java.math.BigDecimal;

public record NotebookResponseDto(
        Long id,
        String nome,
        BigDecimal custoDev,
        Integer quantidadeEstoque,
        BigDecimal precoSP,
        BigDecimal precoGO,
        BigDecimal precoRS,
        BigDecimal precoAM,
        BigDecimal precoBA
) {
    public static NotebookResponseDto from(Notebook notebook, PricingResult pricing) {
        return new NotebookResponseDto(
                notebook.getId(),
                notebook.getNome(),
                PrecisionMathUtil.round(notebook.getCustoDev()),
                notebook.getQuantidadeEstoque(),
                PrecisionMathUtil.round(pricing.precoSP()),
                PrecisionMathUtil.round(pricing.precoGO()),
                PrecisionMathUtil.round(pricing.precoRS()),
                PrecisionMathUtil.round(pricing.precoAM()),
                PrecisionMathUtil.round(pricing.precoBA())
        );
    }
}
