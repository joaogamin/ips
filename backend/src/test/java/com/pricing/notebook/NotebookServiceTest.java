package com.pricing.notebook;

import com.pricing.costparameter.CostParameterService;
import com.pricing.notebook.dto.NotebookResponseDto;
import com.pricing.notebook.pricing.CostParameters;
import com.pricing.notebook.pricing.PricingCalculatorService;
import com.pricing.notebook.pricing.PricingResult;
import com.pricing.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTest {

    @Mock
    private NotebookRepository repository;

    @Mock
    private CostParameterService costParameterService;

    @Mock
    private PricingCalculatorService pricingCalculatorService;

    @InjectMocks
    private NotebookService service;

    private CostParameters stubParams;
    private PricingResult stubPricing;

    @BeforeEach
    void setup() {
        stubParams = new CostParameters(
                bd("3.7259000231"), bd("0.1731"),
                bd("28.55"), bd("36.21"), bd("0.0075"),
                bd("47.83"), bd("0.0099"),
                bd("0.1187"), bd("0.0701"), bd("0.25"), bd("0.0876"), bd("0.1455"),
                bd("0.25"), BigDecimal.ZERO
        );
        stubPricing = new PricingResult(
                bd("4471.08"), bd("5245.53"), bd("0"), bd("5245.53"), bd("6994.04"),
                bd("1748.51"), bd("582.84"),
                bd("7824.52"), bd("7527.61"), bd("9325.39"), bd("7664.37"), bd("8188.35"),
                bd("1748.51"), bd("1748.51"), bd("1748.51"), bd("1748.51"), bd("1748.51")
        );
    }

    @Test
    void findAll_retornaListaComPrecosCalculados() {
        Notebook nb = notebookWith(1L, "Dell XPS 15", bd("1200"), 10);
        when(costParameterService.loadCostParameters()).thenReturn(stubParams);
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(nb));
        when(pricingCalculatorService.calculatePricing(any(), any())).thenReturn(stubPricing);

        List<NotebookResponseDto> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Dell XPS 15");
        assertThat(result.get(0).quantidadeEstoque()).isEqualTo(10);
        assertThat(result.get(0).precoSP()).isNotNull();
        verify(pricingCalculatorService).calculatePricing(bd("1200"), stubParams);
    }

    @Test
    void findById_retornaNotebookComPrecosCalculados() {
        Notebook nb = notebookWith(1L, "Dell XPS 15", bd("1200"), 5);
        when(repository.findById(1L)).thenReturn(Optional.of(nb));
        when(costParameterService.loadCostParameters()).thenReturn(stubParams);
        when(pricingCalculatorService.calculatePricing(any(), any())).thenReturn(stubPricing);

        NotebookResponseDto result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nome()).isEqualTo("Dell XPS 15");
        assertThat(result.quantidadeEstoque()).isEqualTo(5);
    }

    @Test
    void findById_lancaResourceNotFoundQuandoIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registerLotPurchase_somaEstoqueEmNotebookExistente() {
        Notebook nb = notebookWith(1L, "Dell XPS 15", bd("1200"), 5);
        when(repository.findByNome("Dell XPS 15")).thenReturn(Optional.of(nb));

        service.registerLotPurchase("Dell XPS 15", bd("1300"), 3);

        ArgumentCaptor<Notebook> captor = ArgumentCaptor.forClass(Notebook.class);
        verify(repository).save(captor.capture());
        Notebook saved = captor.getValue();
        assertThat(saved.getQuantidadeEstoque()).isEqualTo(8);
        assertThat(saved.getCustoDev()).isEqualByComparingTo(bd("1300"));
    }

    @Test
    void registerLotPurchase_criaNovoNotebookQuandoNomeNaoExiste() {
        when(repository.findByNome("MacBook Pro")).thenReturn(Optional.empty());

        service.registerLotPurchase("MacBook Pro", bd("2500"), 7);

        ArgumentCaptor<Notebook> captor = ArgumentCaptor.forClass(Notebook.class);
        verify(repository).save(captor.capture());
        Notebook saved = captor.getValue();
        assertThat(saved.getNome()).isEqualTo("MacBook Pro");
        assertThat(saved.getQuantidadeEstoque()).isEqualTo(7);
        assertThat(saved.getCustoDev()).isEqualByComparingTo(bd("2500"));
    }

    private static Notebook notebookWith(Long id, String nome, BigDecimal custoDev, int estoque) {
        Notebook nb = new Notebook();
        nb.setNome(nome);
        nb.setCustoDev(custoDev);
        nb.setQuantidadeEstoque(estoque);
        // reflectively set id since there's no public setter
        try {
            var field = Notebook.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(nb, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return nb;
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
