package com.pricing.notebook;

import com.pricing.costparameter.CostParameterService;
import com.pricing.notebook.dto.NotebookResponseDto;
import com.pricing.notebook.pricing.CostParameters;
import com.pricing.notebook.pricing.PricingCalculatorService;
import com.pricing.notebook.pricing.PricingResult;
import com.pricing.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NotebookService {

    private static final Logger log = LoggerFactory.getLogger(NotebookService.class);

    private final NotebookRepository repository;
    private final CostParameterService costParameterService;
    private final PricingCalculatorService pricingCalculatorService;

    public NotebookService(NotebookRepository repository,
                           CostParameterService costParameterService,
                           PricingCalculatorService pricingCalculatorService) {
        this.repository = repository;
        this.costParameterService = costParameterService;
        this.pricingCalculatorService = pricingCalculatorService;
    }

    public List<NotebookResponseDto> findAll() {
        log.debug("Buscando todos os notebooks com preços calculados");
        CostParameters params = costParameterService.loadCostParameters();
        List<NotebookResponseDto> result = repository.findAllByOrderByNomeAsc()
                .stream()
                .map(nb -> toDto(nb, params))
                .toList();
        log.debug("Retornando {} notebook(s)", result.size());
        return result;
    }

    public NotebookResponseDto findById(Long id) {
        log.debug("Buscando notebook id={}", id);
        Notebook notebook = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook", id));
        CostParameters params = costParameterService.loadCostParameters();
        return toDto(notebook, params);
    }

    @Transactional
    public void registerLotPurchase(String nome, BigDecimal custoDev, Integer quantidade) {
        Notebook notebook = repository.findByNome(nome).orElse(new Notebook());
        int estoqueAnterior = notebook.getQuantidadeEstoque() == null ? 0 : notebook.getQuantidadeEstoque();
        notebook.setNome(nome);
        notebook.setCustoDev(custoDev);
        notebook.setQuantidadeEstoque(estoqueAnterior + quantidade);
        repository.save(notebook);
        log.info("Estoque atualizado: nome={}, estoque anterior={}, adicionado={}, novo estoque={}",
                nome, estoqueAnterior, quantidade, notebook.getQuantidadeEstoque());
    }

    private NotebookResponseDto toDto(Notebook notebook, CostParameters params) {
        PricingResult pricing = pricingCalculatorService.calculatePricing(notebook.getCustoDev(), params);
        return NotebookResponseDto.from(notebook, pricing);
    }
}
