package com.pricing.notebook.pricing;

import com.pricing.shared.util.PrecisionMathUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingCalculatorService {

    private static final BigDecimal FAIXA1_THRESHOLD = new BigDecimal("5000");
    private static final BigDecimal FAIXA2_THRESHOLD = new BigDecimal("3500");
    private static final BigDecimal FAIXA3_THRESHOLD = new BigDecimal("1500");
    private static final BigDecimal THREE = new BigDecimal("3");

    public PricingResult calculatePricing(BigDecimal custoDevCoin, CostParameters params) {
        BigDecimal custoBrl = PrecisionMathUtil.multiply(custoDevCoin, params.taxaConversao());

        BigDecimal valorAcumulado = PrecisionMathUtil.multiply(
                custoBrl, PrecisionMathUtil.add(BigDecimal.ONE, params.aliquotaImportacao()));

        BigDecimal frete = computeFreight(valorAcumulado, params);
        BigDecimal custoFinal = PrecisionMathUtil.add(valorAcumulado, frete);

        boolean modoB = params.icmsNaBaseMargem().compareTo(BigDecimal.ZERO) != 0;
        return modoB
                ? buildResultModeB(custoBrl, valorAcumulado, frete, custoFinal, params)
                : buildResultModeA(custoBrl, valorAcumulado, frete, custoFinal, params);
    }

    private PricingResult buildResultModeA(BigDecimal custoBrl, BigDecimal valorAcumulado,
                                               BigDecimal frete, BigDecimal custoFinal,
                                               CostParameters params) {
        BigDecimal umMenosMargem = PrecisionMathUtil.subtract(BigDecimal.ONE, params.margemLucro());
        BigDecimal precoSemIcms = PrecisionMathUtil.divide(custoFinal, umMenosMargem);
        BigDecimal lucroBruto = PrecisionMathUtil.subtract(precoSemIcms, custoFinal);
        BigDecimal reinvestimentoTI = PrecisionMathUtil.divide(lucroBruto, THREE);

        BigDecimal precoSP = computeStatePrice(precoSemIcms, params.aliquotaSP());
        BigDecimal precoGO = computeStatePrice(precoSemIcms, params.aliquotaGO());
        BigDecimal precoRS = computeStatePrice(precoSemIcms, params.aliquotaRS());
        BigDecimal precoAM = computeStatePrice(precoSemIcms, params.aliquotaAM());
        BigDecimal precoBA = computeStatePrice(precoSemIcms, params.aliquotaBA());

        return new PricingResult(
                custoBrl, valorAcumulado, frete, custoFinal, precoSemIcms,
                lucroBruto, reinvestimentoTI,
                precoSP, precoGO, precoRS, precoAM, precoBA,
                lucroBruto, lucroBruto, lucroBruto, lucroBruto, lucroBruto
        );
    }

    private PricingResult buildResultModeB(BigDecimal custoBrl, BigDecimal valorAcumulado,
                                               BigDecimal frete, BigDecimal custoFinal,
                                               CostParameters params) {
        BigDecimal umMenosMargem = PrecisionMathUtil.subtract(BigDecimal.ONE, params.margemLucro());

        BigDecimal custoSP = PrecisionMathUtil.divide(custoFinal, PrecisionMathUtil.subtract(BigDecimal.ONE, params.aliquotaSP()));
        BigDecimal custoGO = PrecisionMathUtil.divide(custoFinal, PrecisionMathUtil.subtract(BigDecimal.ONE, params.aliquotaGO()));
        BigDecimal custoRS = PrecisionMathUtil.divide(custoFinal, PrecisionMathUtil.subtract(BigDecimal.ONE, params.aliquotaRS()));
        BigDecimal custoAM = PrecisionMathUtil.divide(custoFinal, PrecisionMathUtil.subtract(BigDecimal.ONE, params.aliquotaAM()));
        BigDecimal custoBA = PrecisionMathUtil.divide(custoFinal, PrecisionMathUtil.subtract(BigDecimal.ONE, params.aliquotaBA()));

        BigDecimal precoSP = PrecisionMathUtil.divide(custoSP, umMenosMargem);
        BigDecimal precoGO = PrecisionMathUtil.divide(custoGO, umMenosMargem);
        BigDecimal precoRS = PrecisionMathUtil.divide(custoRS, umMenosMargem);
        BigDecimal precoAM = PrecisionMathUtil.divide(custoAM, umMenosMargem);
        BigDecimal precoBA = PrecisionMathUtil.divide(custoBA, umMenosMargem);

        BigDecimal lucroBrutoSP = PrecisionMathUtil.subtract(precoSP, custoSP);
        BigDecimal lucroBrutoGO = PrecisionMathUtil.subtract(precoGO, custoGO);
        BigDecimal lucroBrutoRS = PrecisionMathUtil.subtract(precoRS, custoRS);
        BigDecimal lucroBrutoAM = PrecisionMathUtil.subtract(precoAM, custoAM);
        BigDecimal lucroBrutoBA = PrecisionMathUtil.subtract(precoBA, custoBA);

        BigDecimal somaLucros = PrecisionMathUtil.add(
                PrecisionMathUtil.add(PrecisionMathUtil.add(PrecisionMathUtil.add(lucroBrutoSP, lucroBrutoGO), lucroBrutoRS), lucroBrutoAM),
                lucroBrutoBA);
        BigDecimal lucroBrutoMedio = PrecisionMathUtil.divide(somaLucros, new BigDecimal("5"));
        BigDecimal reinvestimentoTI = PrecisionMathUtil.divide(lucroBrutoMedio, THREE);

        BigDecimal precoSemIcmsRef = PrecisionMathUtil.divide(custoFinal, umMenosMargem);

        return new PricingResult(
                custoBrl, valorAcumulado, frete, custoFinal, precoSemIcmsRef,
                lucroBrutoMedio, reinvestimentoTI,
                precoSP, precoGO, precoRS, precoAM, precoBA,
                lucroBrutoSP, lucroBrutoGO, lucroBrutoRS, lucroBrutoAM, lucroBrutoBA
        );
    }

    private BigDecimal computeFreight(BigDecimal valorAcumulado, CostParameters params) {
        if (valorAcumulado.compareTo(FAIXA1_THRESHOLD) > 0) {
            return BigDecimal.ZERO.setScale(10, java.math.RoundingMode.HALF_UP);
        }
        if (valorAcumulado.compareTo(FAIXA2_THRESHOLD) >= 0) {
            return params.freteFaixa1Fixo();
        }
        if (valorAcumulado.compareTo(FAIXA3_THRESHOLD) >= 0) {
            return PrecisionMathUtil.add(
                    params.freteFaixa2Fixo(),
                    PrecisionMathUtil.multiply(valorAcumulado, params.freteFaixa2Taxa())
            );
        }
        return PrecisionMathUtil.add(
                params.freteFaixa3Fixo(),
                PrecisionMathUtil.multiply(valorAcumulado, params.freteFaixa3Taxa())
        );
    }

    private BigDecimal computeStatePrice(BigDecimal precoSemIcms, BigDecimal aliquotaIcms) {
        BigDecimal umMenosAliquota = PrecisionMathUtil.subtract(BigDecimal.ONE, aliquotaIcms);
        return PrecisionMathUtil.divide(precoSemIcms, umMenosAliquota);
    }
}
