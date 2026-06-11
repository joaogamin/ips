import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { CostParameterService } from '../../core/services/cost-parameter.service';
import { CostParameter } from '../../core/models/cost-parameter.model';

interface ParamInfo {
  label: string;
  impact: string;
  formula: string;
  note?: string;
}

const PARAM_INFO: Record<string, ParamInfo> = {
  taxa_devcoin: {
    label: 'Taxa de câmbio Devcoin → Real',
    impact: 'Primeiro passo da cadeia de precificação: converte o custo do notebook de Devcoin (D$) para Reais.',
    formula: 'custoBRL = custoDev × taxa_devcoin',
  },
  taxa_importacao: {
    label: 'Alíquota de importação',
    impact: 'Imposto de importação incidente sobre o custoBRL. O resultado (valorAcumulado) é a base para o cálculo do frete e de toda a cadeia subsequente.',
    formula: 'valorAcumulado = custoBRL × (1 + taxa_importacao)',
  },
  frete_faixa2_fixo: {
    label: 'Frete fixo — faixa R$ 3.500 a R$ 4.999',
    impact: 'Frete cobrado quando o valorAcumulado cai nessa faixa. Acima de R$ 5.000, o frete é zero.',
    formula: 'frete = frete_faixa2_fixo   (se R$ 3.500 ≤ valorAcumulado < R$ 5.000)',
  },
  frete_faixa3_fixo: {
    label: 'Frete fixo — faixa R$ 1.500 a R$ 3.499',
    impact: 'Componente fixo do frete misto nessa faixa — somado ao percentual sobre o valorAcumulado.',
    formula: 'frete = frete_faixa3_fixo + valorAcumulado × frete_faixa3_percentual',
  },
  frete_faixa3_percentual: {
    label: 'Frete variável — faixa R$ 1.500 a R$ 3.499',
    impact: 'Taxa percentual aplicada ao valorAcumulado para compor o frete nessa faixa.',
    formula: 'frete = frete_faixa3_fixo + valorAcumulado × frete_faixa3_percentual',
  },
  frete_faixa4_fixo: {
    label: 'Frete fixo — abaixo de R$ 1.500',
    impact: 'Componente fixo do frete para notebooks de custo de entrada. É a faixa de maior custo logístico proporcional.',
    formula: 'frete = frete_faixa4_fixo + valorAcumulado × frete_faixa4_percentual',
  },
  frete_faixa4_percentual: {
    label: 'Frete variável — abaixo de R$ 1.500',
    impact: 'Taxa percentual aplicada ao valorAcumulado na faixa de menor custo.',
    formula: 'frete = frete_faixa4_fixo + valorAcumulado × frete_faixa4_percentual',
  },
  icms_sp: {
    label: 'ICMS São Paulo (11,87%)',
    impact: 'ICMS cobrado "por fora": divide o preço sem ICMS para embutir o imposto no preço final ao consumidor paulista.',
    formula: 'precoSP = precoSemICMS ÷ (1 − icms_sp)',
  },
  icms_go: {
    label: 'ICMS Goiás (7,01%)',
    impact: 'Menor alíquota entre os 5 estados — gera o menor preço ao consumidor de GO.',
    formula: 'precoGO = precoSemICMS ÷ (1 − icms_go)',
  },
  icms_rs: {
    label: 'ICMS Rio Grande do Sul (25%)',
    impact: 'Maior alíquota entre os 5 estados — gera o maior preço ao consumidor de RS.',
    formula: 'precoRS = precoSemICMS ÷ (1 − icms_rs)',
  },
  icms_am: {
    label: 'ICMS Amazonas (8,76%)',
    impact: 'ICMS do estado do AM, cobrado por fora sobre o preço sem ICMS.',
    formula: 'precoAM = precoSemICMS ÷ (1 − icms_am)',
  },
  icms_ba: {
    label: 'ICMS Bahia (14,55%)',
    impact: 'ICMS do estado da BA, cobrado por fora sobre o preço sem ICMS.',
    formula: 'precoBA = precoSemICMS ÷ (1 − icms_ba)',
  },
  margem_lucro: {
    label: 'Margem de lucro comercial (25%)',
    impact: 'Margem exigida pelo setor comercial sobre o custoFinal (custo + importação + frete). Define o preço base antes do ICMS estadual.',
    formula: 'precoSemICMS = custoFinal ÷ (1 − margem_lucro)',
  },
  reinvestimento_ti: {
    label: 'Reinvestimento em TI',
    impact: 'Percentual do lucro bruto reservado para reinvestimento em TI. Registrado para auditoria, mas o motor de cálculo usa 1/3 fixo internamente.',
    formula: 'reinvestimentoTI = lucroBruto ÷ 3',
  },
  icms_na_base_margem: {
    label: 'Modo de cálculo — ICMS × Margem (Regra 5)',
    impact: 'A Regra 5 do desafio não especifica se o ICMS estadual entra na base sobre a qual incide a margem de 25%. Este parâmetro torna o comportamento configurável. O preço final ao consumidor é matematicamente idêntico nos dois modos — apenas a contabilização do lucro bruto difere.',
    formula:
      'Modo A (valor 0, padrão):\n' +
      '  precoSemICMS = custoFinal ÷ (1 − m)\n' +
      '  precoEstado  = precoSemICMS ÷ (1 − t)\n' +
      '  → lucro bruto idêntico nos 5 estados\n\n' +
      'Modo B (valor 1, literal):\n' +
      '  custoComICMS = custoFinal ÷ (1 − t)   ← ICMS entra no custo antes da margem\n' +
      '  precoEstado  = custoComICMS ÷ (1 − m)\n' +
      '  → lucro bruto varia por estado (RS > BA > SP > AM > GO)\n\n' +
      '  m = margem_lucro · t = alíquota ICMS do estado\n' +
      '  Prova de equivalência: a ÷ [(1−m)(1−t)] = a ÷ [(1−t)(1−m)]',
    note: 'Aceita apenas 0 ou 1. Alterar para 1 não muda o preço cobrado ao consumidor — apenas redistribui como o lucro bruto é contabilizado por estado no painel de vendas.',
  },
};

interface ParametroRow {
  data: CostParameter;
  control: FormControl<number | null>;
  saving: boolean;
  successMessage: string | null;
  errorMessage: string | null;
  expanded: boolean;
}

@Component({
  selector: 'app-parametros',
  templateUrl: './parametros.component.html',
  styleUrl: './parametros.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ParametrosComponent implements OnInit {

  rows: ParametroRow[] = [];
  loadError: string | null = null;

  constructor(private costParameterService: CostParameterService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.costParameterService.getAll().subscribe({
      next: (params) => {
        this.rows = params.map(p => ({
          data: p,
          control: new FormControl<number | null>(p.valor, [
            Validators.required,
            p.parametro === 'icms_na_base_margem' ? Validators.min(0) : Validators.min(0.0000000001),
          ]),
          saving: false,
          successMessage: null,
          errorMessage: null,
          expanded: false,
        }));
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadError = 'Erro ao carregar os parâmetros. Verifique a conexão com o servidor.';
        this.cdr.markForCheck();
      }
    });
  }

  toggle(row: ParametroRow): void {
    row.expanded = !row.expanded;
    this.cdr.markForCheck();
  }

  getInfo(key: string): ParamInfo | undefined {
    return PARAM_INFO[key];
  }

  save(row: ParametroRow): void {
    if (row.control.invalid || row.control.value === null) {
      row.errorMessage = 'O valor é obrigatório e deve ser maior que zero.';
      row.successMessage = null;
      this.cdr.markForCheck();
      return;
    }
    row.saving = true;
    row.successMessage = null;
    row.errorMessage = null;
    this.cdr.markForCheck();

    this.costParameterService.update(row.data.id, row.control.value).subscribe({
      next: (updated) => {
        row.data = updated;
        row.control.setValue(updated.valor);
        row.saving = false;
        row.successMessage = 'Salvo com sucesso!';
        this.cdr.markForCheck();
        setTimeout(() => { row.successMessage = null; this.cdr.markForCheck(); }, 3000);
      },
      error: (err) => {
        row.saving = false;
        row.errorMessage = err?.error?.message ?? 'Erro ao salvar o parâmetro.';
        this.cdr.markForCheck();
      }
    });
  }
}
