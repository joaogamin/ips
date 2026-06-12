package com.pricing.costparameter;

import com.pricing.costparameter.dto.CostParameterResponse;
import com.pricing.notebook.pricing.CostParameters;
import com.pricing.shared.exception.BusinessException;
import com.pricing.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CostParameterService {

    private static final Logger log = LoggerFactory.getLogger(CostParameterService.class);

    private final CostParameterRepository repository;

    public CostParameterService(CostParameterRepository repository) {
        this.repository = repository;
    }

    public List<CostParameterResponse> findAll() {
        return repository.findAllByOrderByParametroAsc()
                .stream()
                .map(CostParameterResponse::from)
                .toList();
    }

    public CostParameters loadCostParameters() {
        Map<String, BigDecimal> params = repository.findAllByOrderByParametroAsc()
                .stream()
                .collect(Collectors.toMap(CostParameter::getParametro, CostParameter::getValor));

        List<String> required = List.of(
                "taxa_devcoin", "taxa_importacao",
                "frete_faixa2_fixo", "frete_faixa3_fixo", "frete_faixa3_percentual",
                "frete_faixa4_fixo", "frete_faixa4_percentual",
                "icms_sp", "icms_go", "icms_rs", "icms_am", "icms_ba",
                "margem_lucro", "icms_na_base_margem"
        );
        required.stream()
                .filter(k -> !params.containsKey(k))
                .findFirst()
                .ifPresent(k -> {
                    throw new BusinessException("Parâmetro de custo ausente: " + k);
                });

        return new CostParameters(
                params.get("taxa_devcoin"),
                params.get("taxa_importacao"),
                params.get("frete_faixa2_fixo"),
                params.get("frete_faixa3_fixo"),
                params.get("frete_faixa3_percentual"),
                params.get("frete_faixa4_fixo"),
                params.get("frete_faixa4_percentual"),
                params.get("icms_sp"),
                params.get("icms_go"),
                params.get("icms_rs"),
                params.get("icms_am"),
                params.get("icms_ba"),
                params.get("margem_lucro"),
                params.get("icms_na_base_margem")
        );
    }

    @Transactional
    public CostParameterResponse update(Long id, BigDecimal novoValor) {
        if (novoValor == null) {
            throw new BusinessException("O valor do parâmetro não pode ser nulo");
        }
        if (novoValor.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("O valor do parâmetro não pode ser negativo");
        }
        CostParameter parameter = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostParameter", id));
        parameter.setValor(novoValor);
        log.info("Parâmetro atualizado: parametro={}, novoValor={}", parameter.getParametro(), novoValor);
        return CostParameterResponse.from(repository.save(parameter));
    }
}
