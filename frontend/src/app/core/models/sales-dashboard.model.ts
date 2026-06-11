export interface NotebookSalesSummary {
  nome: string;
  quantidadeEstoque: number;
  precoSP: number;
  precoGO: number;
  precoRS: number;
  precoAM: number;
  precoBA: number;
}

export interface StateTotals {
  totalSP: number;
  totalGO: number;
  totalRS: number;
  totalAM: number;
  totalBA: number;
}

export interface SalesDashboard {
  notebooks: NotebookSalesSummary[];
  totaisPorEstado: StateTotals;
  lucroBrutoPorEstado: StateTotals;
  reinvestimentoTI: number;
  totalGeral: number;
}
