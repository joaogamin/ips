package com.pricing.notebook;

import com.pricing.notebook.pricing.CostParameters;
import com.pricing.notebook.pricing.PricingCalculatorService;
import com.pricing.notebook.pricing.PricingResult;
import com.pricing.shared.util.PrecisionMathUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingCalculatorGoldenMasterTest {

    private static final BigDecimal CUSTO_DEV = new BigDecimal("1200");

    private PricingCalculatorService service;
    private CostParameters paramsBase;

    @BeforeEach
    void setUp() {
        service = new PricingCalculatorService();
        paramsBase = canonicalParams(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Modo A — custoBRL correto (1200 × 3.7259000231)")
    void modoA_custoBrl() {
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsBase);
        // 1200 × 3.7259000231 = 4471.0800277200 (scale 10)
        assertThat(r.custoBrl()).isEqualByComparingTo(new BigDecimal("4471.0800277200"));
    }

    @Test
    @DisplayName("Modo A — valorAcumulado correto (custoBRL × 1.1731)")
    void modoA_valorAcumulado() {
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsBase);
        assertThat(r.valorAcumulado()).isEqualByComparingTo(new BigDecimal("5245.0239805183"));
    }

    @Test
    @DisplayName("Modo A — frete zero (valorAcumulado > 5000)")
    void modoA_freteZero() {
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsBase);
        assertThat(r.frete()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Modo A — preços por estado maiores que precoSemIcms")
    void modoA_precosEstadoMaioresQueBase() {
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsBase);
        assertThat(r.precoSP()).isGreaterThan(r.precoSemIcms());
        assertThat(r.precoGO()).isGreaterThan(r.precoSemIcms());
        assertThat(r.precoRS()).isGreaterThan(r.precoSemIcms());
        assertThat(r.precoAM()).isGreaterThan(r.precoSemIcms());
        assertThat(r.precoBA()).isGreaterThan(r.precoSemIcms());
    }

    @Test
    @DisplayName("Modo A — RS tem preço mais alto (maior ICMS 25%) e GO o mais baixo (7,01%)")
    void modoA_ordenacaoPrecosPorIcms() {
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsBase);
        assertThat(r.precoRS()).isGreaterThan(r.precoBA());
        assertThat(r.precoBA()).isGreaterThan(r.precoSP());
        assertThat(r.precoSP()).isGreaterThan(r.precoAM());
        assertThat(r.precoAM()).isGreaterThan(r.precoGO());
    }

    @Test
    @DisplayName("Modo A — lucro bruto idêntico nos 5 estados")
    void modoA_lucroBrutoIgualTodosEstados() {
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsBase);
        assertThat(r.lucroBrutoSP()).isEqualByComparingTo(r.lucroBruto());
        assertThat(r.lucroBrutoGO()).isEqualByComparingTo(r.lucroBruto());
        assertThat(r.lucroBrutoRS()).isEqualByComparingTo(r.lucroBruto());
        assertThat(r.lucroBrutoAM()).isEqualByComparingTo(r.lucroBruto());
        assertThat(r.lucroBrutoBA()).isEqualByComparingTo(r.lucroBruto());
    }

    @Test
    @DisplayName("Modo A — reinvestimentoTI = lucroBruto / 3")
    void modoA_reinvestimentoTI() {
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsBase);
        BigDecimal esperado = PrecisionMathUtil.divide(r.lucroBruto(), new BigDecimal("3"));
        assertThat(r.reinvestimentoTI()).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Modo A — margem 25%: precoSemIcms = custoFinal / 0.75")
    void modoA_margemAplicadaCorretamente() {
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsBase);
        BigDecimal esperado = PrecisionMathUtil.divide(r.custoFinal(), new BigDecimal("0.75"));
        assertThat(r.precoSemIcms()).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Modo B — preços apresentados (2 casas) IGUAIS ao Modo A: a/((1-m)(1-t)) == a/((1-t)(1-m))")
    void modoB_precosApresentadosIguaisAoModoA() {
        CostParameters paramsB = canonicalParams(BigDecimal.ONE);
        PricingResult rA = service.calculatePricing(CUSTO_DEV, paramsBase);
        PricingResult rB = service.calculatePricing(CUSTO_DEV, paramsB);

        assertThat(PrecisionMathUtil.round(rB.precoSP())).isEqualByComparingTo(PrecisionMathUtil.round(rA.precoSP()));
        assertThat(PrecisionMathUtil.round(rB.precoGO())).isEqualByComparingTo(PrecisionMathUtil.round(rA.precoGO()));
        assertThat(PrecisionMathUtil.round(rB.precoRS())).isEqualByComparingTo(PrecisionMathUtil.round(rA.precoRS()));
        assertThat(PrecisionMathUtil.round(rB.precoAM())).isEqualByComparingTo(PrecisionMathUtil.round(rA.precoAM()));
        assertThat(PrecisionMathUtil.round(rB.precoBA())).isEqualByComparingTo(PrecisionMathUtil.round(rA.precoBA()));
    }

    @Test
    @DisplayName("Modo B — lucro bruto varia por estado (RS > BA > SP > AM > GO)")
    void modoB_lucroBrutoVariaPorEstado() {
        CostParameters paramsB = canonicalParams(BigDecimal.ONE);
        PricingResult r = service.calculatePricing(CUSTO_DEV, paramsB);

        assertThat(r.lucroBrutoRS()).isGreaterThan(r.lucroBrutoBA());
        assertThat(r.lucroBrutoBA()).isGreaterThan(r.lucroBrutoSP());
        assertThat(r.lucroBrutoSP()).isGreaterThan(r.lucroBrutoAM());
        assertThat(r.lucroBrutoAM()).isGreaterThan(r.lucroBrutoGO());
    }

    @Test
    @DisplayName("Modo B — custo final idêntico nos dois modos (frete/importação não mudam)")
    void modoB_custoFinalIdêntico() {
        CostParameters paramsB = canonicalParams(BigDecimal.ONE);
        PricingResult rA = service.calculatePricing(CUSTO_DEV, paramsBase);
        PricingResult rB = service.calculatePricing(CUSTO_DEV, paramsB);
        assertThat(rB.custoFinal()).isEqualByComparingTo(rA.custoFinal());
    }

    @Test
    @DisplayName("Frete faixa < R$ 1.500,00 — custoDev = 1200 (valorAcumulado ≈ 1407,72)")
    void freteAbaixoDe1500() {
        CostParameters p = new CostParameters(
                BigDecimal.ONE, new BigDecimal("0.1731"),
                new BigDecimal("28.55"), new BigDecimal("36.21"), new BigDecimal("0.0075"),
                new BigDecimal("47.83"), new BigDecimal("0.0099"),
                new BigDecimal("0.1187"), new BigDecimal("0.0701"), new BigDecimal("0.25"),
                new BigDecimal("0.0876"), new BigDecimal("0.1455"), new BigDecimal("0.25"),
                BigDecimal.ZERO);
        PricingResult r = service.calculatePricing(new BigDecimal("1200"), p);
        assertThat(r.frete()).isGreaterThan(new BigDecimal("47"));
    }

    @Test
    @DisplayName("Frete faixa R$ 3.500,00–R$ 4.999,99 — custoDev = 3000 (valorAcumulado ≈ 3519,3)")
    void freteFaixa3500a5000() {
        CostParameters p = new CostParameters(
                BigDecimal.ONE, new BigDecimal("0.1731"),
                new BigDecimal("28.55"), new BigDecimal("36.21"), new BigDecimal("0.0075"),
                new BigDecimal("47.83"), new BigDecimal("0.0099"),
                new BigDecimal("0.1187"), new BigDecimal("0.0701"), new BigDecimal("0.25"),
                new BigDecimal("0.0876"), new BigDecimal("0.1455"), new BigDecimal("0.25"),
                BigDecimal.ZERO);
        PricingResult r = service.calculatePricing(new BigDecimal("3000"), p);
        assertThat(r.frete()).isEqualByComparingTo(new BigDecimal("28.55"));
    }

    private static CostParameters canonicalParams(BigDecimal icmsNaBaseMargem) {
        return new CostParameters(
                new BigDecimal("3.7259000231"),
                new BigDecimal("0.1731"),
                new BigDecimal("28.55"),
                new BigDecimal("36.21"),
                new BigDecimal("0.0075"),
                new BigDecimal("47.83"),
                new BigDecimal("0.0099"),
                new BigDecimal("0.1187"),
                new BigDecimal("0.0701"),
                new BigDecimal("0.25"),
                new BigDecimal("0.0876"),
                new BigDecimal("0.1455"),
                new BigDecimal("0.25"),
                icmsNaBaseMargem
        );
    }
}
