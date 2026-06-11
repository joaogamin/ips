package com.pricing.notebook;

import com.pricing.notebook.pricing.CostParameters;
import com.pricing.notebook.pricing.PricingCalculatorService;
import com.pricing.notebook.pricing.PricingResult;
import com.pricing.shared.util.PrecisionMathUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingCalculatorServiceTest {

    private PricingCalculatorService service;
    private CostParameters params;

    @BeforeEach
    void setUp() {
        service = new PricingCalculatorService();

        params = new CostParameters(
                new BigDecimal("1"),
                new BigDecimal("0.1731"),
                new BigDecimal("28.55"),
                new BigDecimal("36.21"),
                new BigDecimal("0.0075"),
                new BigDecimal("47.83"),
                new BigDecimal("0.0099"),
                new BigDecimal("0.1187"),
                new BigDecimal("0.0701"),
                new BigDecimal("0.2500"),
                new BigDecimal("0.0876"),
                new BigDecimal("0.1455"),
                new BigDecimal("0.25"),
                BigDecimal.ZERO
        );
    }

    @Test
    void custoDev1200_freteNaFaixaMenorQue1500_ePrecosPorEstadoCalculados() {
        PricingResult result = service.calculatePricing(new BigDecimal("1200"), params);

        BigDecimal valorAcumuladoEsperado = PrecisionMathUtil.of("1407.72");
        BigDecimal freteEsperado = PrecisionMathUtil.add(
                PrecisionMathUtil.of("47.83"),
                PrecisionMathUtil.multiply(valorAcumuladoEsperado, PrecisionMathUtil.of("0.0099"))
        );

        assertThat(result.valorAcumulado().compareTo(valorAcumuladoEsperado)).isZero();
        assertThat(result.frete().compareTo(freteEsperado)).isZero();
        assertThat(result.frete().compareTo(BigDecimal.ZERO)).isGreaterThan(0);

        assertThat(result.precoSP().compareTo(result.precoSemIcms())).isGreaterThan(0);
        assertThat(result.precoGO().compareTo(result.precoSemIcms())).isGreaterThan(0);
        assertThat(result.precoRS().compareTo(result.precoSemIcms())).isGreaterThan(0);
        assertThat(result.precoAM().compareTo(result.precoSemIcms())).isGreaterThan(0);
        assertThat(result.precoBA().compareTo(result.precoSemIcms())).isGreaterThan(0);

        assertThat(result.precoRS().compareTo(result.precoSP())).isGreaterThan(0);
        assertThat(result.precoGO().compareTo(result.precoBA())).isLessThan(0);
    }

    @Test
    void custoDev3000_freteNaFaixaEntre3500e4999() {
        PricingResult result = service.calculatePricing(new BigDecimal("3000"), params);

        BigDecimal valorAcumuladoEsperado = PrecisionMathUtil.of("3519.3");
        BigDecimal freteEsperado = PrecisionMathUtil.of("28.55");

        assertThat(result.valorAcumulado().compareTo(valorAcumuladoEsperado)).isZero();
        assertThat(result.frete().compareTo(freteEsperado)).isZero();
    }

    @Test
    void custoDev5000_freteGratis() {
        PricingResult result = service.calculatePricing(new BigDecimal("5000"), params);

        assertThat(result.frete().compareTo(BigDecimal.ZERO)).isZero();
        assertThat(result.custoFinal().compareTo(result.valorAcumulado())).isZero();
    }

    @Test
    void reinvestimentoTI_ehExatamenteLucroBrutoDivididoPor3() {
        PricingResult result = service.calculatePricing(new BigDecimal("1200"), params);

        BigDecimal reinvestimentoEsperado = PrecisionMathUtil.divide(result.lucroBruto(), new BigDecimal("3"));
        assertThat(result.reinvestimentoTI().compareTo(reinvestimentoEsperado)).isZero();
    }

    @Test
    void precoEstado_calculadoCorretamenteSobrePrecoSemIcms() {
        PricingResult result = service.calculatePricing(new BigDecimal("3000"), params);

        // precoSP = precoSemIcms / (1 - 0.1187)
        BigDecimal precoSPEsperado = PrecisionMathUtil.divide(
                result.precoSemIcms(),
                PrecisionMathUtil.subtract(BigDecimal.ONE, new BigDecimal("0.1187"))
        );
        assertThat(result.precoSP().compareTo(precoSPEsperado)).isZero();

        // precoRS = precoSemIcms / (1 - 0.25)
        BigDecimal precoRSEsperado = PrecisionMathUtil.divide(
                result.precoSemIcms(),
                PrecisionMathUtil.subtract(BigDecimal.ONE, new BigDecimal("0.2500"))
        );
        assertThat(result.precoRS().compareTo(precoRSEsperado)).isZero();
    }

    @Test
    void custoDev2000_freteNaFaixaEntre1500e3499() {
        // custoDev = 2000, taxaConversao = 1 → custoBrl = 2000
        // valorAcumulado = 2000 * 1.1731 = 2346.2 (1500 ≤ x ≤ 3499.99)
        PricingResult result = service.calculatePricing(new BigDecimal("2000"), params);

        BigDecimal valorAcumuladoEsperado = PrecisionMathUtil.of("2346.2");
        BigDecimal freteEsperado = PrecisionMathUtil.add(
                PrecisionMathUtil.of("36.21"),
                PrecisionMathUtil.multiply(valorAcumuladoEsperado, PrecisionMathUtil.of("0.0075"))
        );

        assertThat(result.valorAcumulado().compareTo(valorAcumuladoEsperado)).isZero();
        assertThat(result.frete().compareTo(freteEsperado)).isZero();
    }
}
