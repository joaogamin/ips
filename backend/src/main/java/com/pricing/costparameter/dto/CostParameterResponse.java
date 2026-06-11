package com.pricing.costparameter.dto;

import com.pricing.costparameter.CostParameter;

import java.math.BigDecimal;

public record CostParameterResponse(
        Long id,
        String parametro,
        BigDecimal valor,
        String descricao
) {
    public static CostParameterResponse from(CostParameter entity) {
        return new CostParameterResponse(
                entity.getId(),
                entity.getParametro(),
                entity.getValor(),
                entity.getDescricao()
        );
    }
}
