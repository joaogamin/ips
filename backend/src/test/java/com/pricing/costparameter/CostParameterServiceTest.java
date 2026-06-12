package com.pricing.costparameter;

import com.pricing.costparameter.dto.CostParameterResponse;
import com.pricing.shared.exception.BusinessException;
import com.pricing.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CostParameterServiceTest {

    @Mock
    private CostParameterRepository repository;

    @InjectMocks
    private CostParameterService service;

    private CostParameter parameter;

    @BeforeEach
    void setUp() {
        parameter = new CostParameter();
        parameter.setId(1L);
        parameter.setParametro("taxa_devcoin");
        parameter.setValor(new BigDecimal("3.7259000231"));
        parameter.setDescricao("Taxa de conversão Devcoin para Real");
    }

    @Test
    void findAll_deveRetornarListaOrdenadaPorNome() {
        CostParameter outro = new CostParameter();
        outro.setId(2L);
        outro.setParametro("taxa_importacao");
        outro.setValor(new BigDecimal("0.1731000000"));
        outro.setDescricao("Taxa de importação");

        when(repository.findAllByOrderByParametroAsc()).thenReturn(List.of(parameter, outro));

        List<CostParameterResponse> resultado = service.findAll();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).parametro()).isEqualTo("taxa_devcoin");
        verify(repository).findAllByOrderByParametroAsc();
    }

    @Test
    void update_deveAtualizarValorComSucesso() {
        BigDecimal novoValor = new BigDecimal("4.0000000000");
        CostParameter atualizado = new CostParameter();
        atualizado.setId(1L);
        atualizado.setParametro("taxa_devcoin");
        atualizado.setValor(novoValor);
        atualizado.setDescricao("Taxa de conversão Devcoin para Real");

        when(repository.findById(1L)).thenReturn(Optional.of(parameter));
        when(repository.save(parameter)).thenReturn(atualizado);

        CostParameterResponse resultado = service.update(1L, novoValor);

        assertThat(resultado.valor()).isEqualByComparingTo(novoValor);
        verify(repository).save(parameter);
    }

    @Test
    void update_deveLancarResourceNotFoundExceptionQuandoIdNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new BigDecimal("1.5")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_deveLancarBusinessExceptionQuandoValorNulo() {
        assertThatThrownBy(() -> service.update(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nulo");
    }

    @Test
    void update_aceitaValorZero_paramsFlagPermitem0() {
        when(repository.findById(1L)).thenReturn(Optional.of(parameter));
        when(repository.save(parameter)).thenReturn(parameter);
        assertThat(service.update(1L, BigDecimal.ZERO)).isNotNull();
    }

    @Test
    void update_deveLancarBusinessExceptionQuandoValorNegativo() {
        assertThatThrownBy(() -> service.update(1L, new BigDecimal("-1.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("negativo");
    }
}
