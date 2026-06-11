import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DashboardService } from '../../core/services/dashboard.service';
import { SalesDashboard, NotebookSalesSummary } from '../../core/models/sales-dashboard.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit {

  dashboard: SalesDashboard | null = null;
  loading = false;
  errorMessage = '';

  constructor(private dashboardService: DashboardService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Erro ao carregar painel de vendas.';
        this.dashboard = null;
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  cellValue(nb: NotebookSalesSummary, estado: 'SP' | 'GO' | 'RS' | 'AM' | 'BA'): number {
    const preco = nb[`preco${estado}` as keyof NotebookSalesSummary] as number;
    return preco * nb.quantidadeEstoque;
  }
}
