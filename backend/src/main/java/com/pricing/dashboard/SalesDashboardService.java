package com.pricing.dashboard;

import com.pricing.costparameter.CostParameterService;
import com.pricing.dashboard.dto.NotebookSalesSummary;
import com.pricing.dashboard.dto.SalesDashboardDto;
import com.pricing.dashboard.dto.StateTotalsDto;
import com.pricing.notebook.Notebook;
import com.pricing.notebook.NotebookRepository;
import com.pricing.notebook.pricing.CostParameters;
import com.pricing.notebook.pricing.PricingCalculatorService;
import com.pricing.notebook.pricing.PricingResult;
import com.pricing.shared.util.PrecisionMathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesDashboardService {

    private static final Logger log = LoggerFactory.getLogger(SalesDashboardService.class);

    private static final BigDecimal THREE = new BigDecimal("3");

    private final NotebookRepository notebookRepository;
    private final PricingCalculatorService pricingCalculatorService;
    private final CostParameterService costParameterService;

    public SalesDashboardService(NotebookRepository notebookRepository,
                                  PricingCalculatorService pricingCalculatorService,
                                  CostParameterService costParameterService) {
        this.notebookRepository = notebookRepository;
        this.pricingCalculatorService = pricingCalculatorService;
        this.costParameterService = costParameterService;
    }

    public SalesDashboardDto getDashboard() {
        List<Notebook> notebooks = notebookRepository.findAllByOrderByNomeAsc();
        log.debug("Gerando painel de vendas para {} notebook(s)", notebooks.size());

        if (notebooks.isEmpty()) {
            log.debug("Nenhum notebook cadastrado, retornando painel vazio");
            return emptyDashboard();
        }

        CostParameters params = costParameterService.loadCostParameters();

        List<NotebookSalesSummary> summaries = new ArrayList<>();
        BigDecimal accSP = BigDecimal.ZERO;
        BigDecimal accGO = BigDecimal.ZERO;
        BigDecimal accRS = BigDecimal.ZERO;
        BigDecimal accAM = BigDecimal.ZERO;
        BigDecimal accBA = BigDecimal.ZERO;
        BigDecimal accLucroBruto = BigDecimal.ZERO;   // base para reinvestimentoTI
        BigDecimal accLucroBrutoSP = BigDecimal.ZERO;
        BigDecimal accLucroBrutoGO = BigDecimal.ZERO;
        BigDecimal accLucroBrutoRS = BigDecimal.ZERO;
        BigDecimal accLucroBrutoAM = BigDecimal.ZERO;
        BigDecimal accLucroBrutoBA = BigDecimal.ZERO;

        for (Notebook notebook : notebooks) {
            PricingResult pricing = pricingCalculatorService.calculatePricing(notebook.getCustoDev(), params);
            BigDecimal qty = BigDecimal.valueOf(notebook.getQuantidadeEstoque());

            summaries.add(new NotebookSalesSummary(
                    notebook.getNome(),
                    notebook.getQuantidadeEstoque(),
                    PrecisionMathUtil.round(pricing.precoSP()),
                    PrecisionMathUtil.round(pricing.precoGO()),
                    PrecisionMathUtil.round(pricing.precoRS()),
                    PrecisionMathUtil.round(pricing.precoAM()),
                    PrecisionMathUtil.round(pricing.precoBA())
            ));

            accSP = PrecisionMathUtil.add(accSP, PrecisionMathUtil.multiply(pricing.precoSP(), qty));
            accGO = PrecisionMathUtil.add(accGO, PrecisionMathUtil.multiply(pricing.precoGO(), qty));
            accRS = PrecisionMathUtil.add(accRS, PrecisionMathUtil.multiply(pricing.precoRS(), qty));
            accAM = PrecisionMathUtil.add(accAM, PrecisionMathUtil.multiply(pricing.precoAM(), qty));
            accBA = PrecisionMathUtil.add(accBA, PrecisionMathUtil.multiply(pricing.precoBA(), qty));

            accLucroBruto = PrecisionMathUtil.add(accLucroBruto, PrecisionMathUtil.multiply(pricing.lucroBruto(), qty));
            accLucroBrutoSP = PrecisionMathUtil.add(accLucroBrutoSP, PrecisionMathUtil.multiply(pricing.lucroBrutoSP(), qty));
            accLucroBrutoGO = PrecisionMathUtil.add(accLucroBrutoGO, PrecisionMathUtil.multiply(pricing.lucroBrutoGO(), qty));
            accLucroBrutoRS = PrecisionMathUtil.add(accLucroBrutoRS, PrecisionMathUtil.multiply(pricing.lucroBrutoRS(), qty));
            accLucroBrutoAM = PrecisionMathUtil.add(accLucroBrutoAM, PrecisionMathUtil.multiply(pricing.lucroBrutoAM(), qty));
            accLucroBrutoBA = PrecisionMathUtil.add(accLucroBrutoBA, PrecisionMathUtil.multiply(pricing.lucroBrutoBA(), qty));
        }

        BigDecimal reinvestimentoTI = PrecisionMathUtil.divide(accLucroBruto, THREE);

        BigDecimal totalGeral = PrecisionMathUtil.add(
                PrecisionMathUtil.add(PrecisionMathUtil.add(PrecisionMathUtil.add(accSP, accGO), accRS), accAM),
                accBA);

        log.info("Painel gerado: {} notebooks, totalGeral={}, lucroBruto={}, reinvestimentoTI={}",
                summaries.size(), PrecisionMathUtil.round(totalGeral),
                PrecisionMathUtil.round(accLucroBruto), PrecisionMathUtil.round(reinvestimentoTI));

        return new SalesDashboardDto(
                summaries,
                new StateTotalsDto(
                        PrecisionMathUtil.round(accSP),
                        PrecisionMathUtil.round(accGO),
                        PrecisionMathUtil.round(accRS),
                        PrecisionMathUtil.round(accAM),
                        PrecisionMathUtil.round(accBA)
                ),
                new StateTotalsDto(
                        PrecisionMathUtil.round(accLucroBrutoSP),
                        PrecisionMathUtil.round(accLucroBrutoGO),
                        PrecisionMathUtil.round(accLucroBrutoRS),
                        PrecisionMathUtil.round(accLucroBrutoAM),
                        PrecisionMathUtil.round(accLucroBrutoBA)
                ),
                PrecisionMathUtil.round(reinvestimentoTI),
                PrecisionMathUtil.round(totalGeral)
        );
    }

    private SalesDashboardDto emptyDashboard() {
        StateTotalsDto zeros = new StateTotalsDto(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        return new SalesDashboardDto(List.of(), zeros, zeros, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
