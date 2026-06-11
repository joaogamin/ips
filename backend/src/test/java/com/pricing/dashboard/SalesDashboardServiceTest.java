package com.pricing.dashboard;

import com.pricing.costparameter.CostParameterService;
import com.pricing.dashboard.dto.SalesDashboardDto;
import com.pricing.notebook.Notebook;
import com.pricing.notebook.NotebookRepository;
import com.pricing.notebook.pricing.CostParameters;
import com.pricing.notebook.pricing.PricingCalculatorService;
import com.pricing.notebook.pricing.PricingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesDashboardServiceTest {

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private PricingCalculatorService pricingCalculatorService;

    @Mock
    private CostParameterService costParameterService;

    @InjectMocks
    private SalesDashboardService service;

    private CostParameters stubParams;

    @BeforeEach
    void setup() {
        stubParams = new CostParameters(
                bd("3.7259000231"), bd("0.1731"),
                bd("28.55"), bd("36.21"), bd("0.0075"),
                bd("47.83"), bd("0.0099"),
                bd("0.1187"), bd("0.0701"), bd("0.25"), bd("0.0876"), bd("0.1455"),
                bd("0.25"), BigDecimal.ZERO
        );
    }

    @Test
    void getDashboard_doisNotebooks_calcularTotaisPorEstadoCorretamente() {
        Notebook nb1 = notebookWith("Dell XPS 15", bd("1200"), 10);
        Notebook nb2 = notebookWith("Lenovo X1 Carbon", bd("3000"), 3);

        PricingResult pricing1 = pricingResult(
                bd("1000"), bd("2000"), bd("3000"), bd("4000"), bd("5000"), bd("500")
        );
        PricingResult pricing2 = pricingResult(
                bd("2000"), bd("3000"), bd("4000"), bd("5000"), bd("6000"), bd("800")
        );

        when(notebookRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(nb1, nb2));
        when(costParameterService.loadCostParameters()).thenReturn(stubParams);
        when(pricingCalculatorService.calculatePricing(any(), any()))
                .thenReturn(pricing1)
                .thenReturn(pricing2);

        SalesDashboardDto result = service.getDashboard();

        assertThat(result.notebooks()).hasSize(2);

        // totaisPorEstado: nb1(qty=10) + nb2(qty=3)
        // SP: 1000*10 + 2000*3 = 10000 + 6000 = 16000
        assertThat(result.totaisPorEstado().totalSP()).isEqualByComparingTo(bd("16000"));
        // GO: 2000*10 + 3000*3 = 20000 + 9000 = 29000
        assertThat(result.totaisPorEstado().totalGO()).isEqualByComparingTo(bd("29000"));
        // RS: 3000*10 + 4000*3 = 30000 + 12000 = 42000
        assertThat(result.totaisPorEstado().totalRS()).isEqualByComparingTo(bd("42000"));
        // AM: 4000*10 + 5000*3 = 40000 + 15000 = 55000
        assertThat(result.totaisPorEstado().totalAM()).isEqualByComparingTo(bd("55000"));
        // BA: 5000*10 + 6000*3 = 50000 + 18000 = 68000
        assertThat(result.totaisPorEstado().totalBA()).isEqualByComparingTo(bd("68000"));
    }

    @Test
    void getDashboard_doisNotebooks_lucroBrutoTotalSomandoAmbos() {
        Notebook nb1 = notebookWith("Dell XPS 15", bd("1200"), 10);
        Notebook nb2 = notebookWith("Lenovo X1 Carbon", bd("3000"), 3);

        PricingResult pricing1 = pricingResult(
                bd("1000"), bd("2000"), bd("3000"), bd("4000"), bd("5000"), bd("500")
        );
        PricingResult pricing2 = pricingResult(
                bd("2000"), bd("3000"), bd("4000"), bd("5000"), bd("6000"), bd("800")
        );

        when(notebookRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(nb1, nb2));
        when(costParameterService.loadCostParameters()).thenReturn(stubParams);
        when(pricingCalculatorService.calculatePricing(any(), any()))
                .thenReturn(pricing1)
                .thenReturn(pricing2);

        SalesDashboardDto result = service.getDashboard();

        // lucroBrutoTotal: 500*10 + 800*3 = 5000 + 2400 = 7400
        BigDecimal expectedLucroBruto = bd("7400");
        assertThat(result.lucroBrutoPorEstado().totalSP()).isEqualByComparingTo(expectedLucroBruto);
        assertThat(result.lucroBrutoPorEstado().totalGO()).isEqualByComparingTo(expectedLucroBruto);
        assertThat(result.lucroBrutoPorEstado().totalRS()).isEqualByComparingTo(expectedLucroBruto);
        assertThat(result.lucroBrutoPorEstado().totalAM()).isEqualByComparingTo(expectedLucroBruto);
        assertThat(result.lucroBrutoPorEstado().totalBA()).isEqualByComparingTo(expectedLucroBruto);
    }

    @Test
    void getDashboard_doisNotebooks_reinvestimentoTIIgualLucroBrutoDivididoPorTres() {
        Notebook nb1 = notebookWith("Dell XPS 15", bd("1200"), 10);
        Notebook nb2 = notebookWith("Lenovo X1 Carbon", bd("3000"), 3);

        PricingResult pricing1 = pricingResult(
                bd("1000"), bd("2000"), bd("3000"), bd("4000"), bd("5000"), bd("500")
        );
        PricingResult pricing2 = pricingResult(
                bd("2000"), bd("3000"), bd("4000"), bd("5000"), bd("6000"), bd("800")
        );

        when(notebookRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(nb1, nb2));
        when(costParameterService.loadCostParameters()).thenReturn(stubParams);
        when(pricingCalculatorService.calculatePricing(any(), any()))
                .thenReturn(pricing1)
                .thenReturn(pricing2);

        SalesDashboardDto result = service.getDashboard();

        // lucroBrutoTotal = 7400; reinvestimentoTI = 7400 / 3 = 2466.67 (arredondado HALF_UP 2 casas)
        BigDecimal expectedReinvestimento = bd("2466.67");
        assertThat(result.reinvestimentoTI().compareTo(expectedReinvestimento)).isZero();
    }

    @Test
    void getDashboard_semNotebooks_retornaListaVaziaETotaisZerados() {
        when(notebookRepository.findAllByOrderByNomeAsc()).thenReturn(List.of());

        SalesDashboardDto result = service.getDashboard();

        assertThat(result.notebooks()).isEmpty();
        assertThat(result.totaisPorEstado().totalSP()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totaisPorEstado().totalGO()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totaisPorEstado().totalRS()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totaisPorEstado().totalAM()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totaisPorEstado().totalBA()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.lucroBrutoPorEstado().totalSP()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.reinvestimentoTI()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalGeral()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getDashboard_doisNotebooks_totalGeralSomaTodosOsEstados() {
        Notebook nb1 = notebookWith("Dell XPS 15", bd("1200"), 10);
        Notebook nb2 = notebookWith("Lenovo X1 Carbon", bd("3000"), 3);

        PricingResult pricing1 = pricingResult(
                bd("1000"), bd("2000"), bd("3000"), bd("4000"), bd("5000"), bd("500")
        );
        PricingResult pricing2 = pricingResult(
                bd("2000"), bd("3000"), bd("4000"), bd("5000"), bd("6000"), bd("800")
        );

        when(notebookRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(nb1, nb2));
        when(costParameterService.loadCostParameters()).thenReturn(stubParams);
        when(pricingCalculatorService.calculatePricing(any(), any()))
                .thenReturn(pricing1)
                .thenReturn(pricing2);

        SalesDashboardDto result = service.getDashboard();

        // totalGeral = 16000 + 29000 + 42000 + 55000 + 68000 = 210000
        assertThat(result.totalGeral()).isEqualByComparingTo(bd("210000"));
    }

    private static Notebook notebookWith(String nome, BigDecimal custoDev, int estoque) {
        Notebook nb = new Notebook();
        nb.setNome(nome);
        nb.setCustoDev(custoDev);
        nb.setQuantidadeEstoque(estoque);
        return nb;
    }

    private static PricingResult pricingResult(
            BigDecimal precoSP, BigDecimal precoGO, BigDecimal precoRS,
            BigDecimal precoAM, BigDecimal precoBA, BigDecimal lucroBruto) {
        BigDecimal zero = BigDecimal.ZERO;
        return new PricingResult(
                zero, zero, zero, zero, zero,
                lucroBruto, lucroBruto.divide(new BigDecimal("3"), 10, java.math.RoundingMode.HALF_UP),
                precoSP, precoGO, precoRS, precoAM, precoBA,
                lucroBruto, lucroBruto, lucroBruto, lucroBruto, lucroBruto
        );
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
