import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule, registerLocaleData } from '@angular/common';
import { LOCALE_ID } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { of, throwError } from 'rxjs';
import localePt from '@angular/common/locales/pt';

import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../../core/services/dashboard.service';
import { SalesDashboard } from '../../core/models/sales-dashboard.model';

registerLocaleData(localePt, 'pt-BR');

const ESTADO_TOTALS_STUB = {
  totalSP: 78245.20, totalGO: 75276.10, totalRS: 93253.90, totalAM: 76643.70, totalBA: 81883.50
};

const DASHBOARD_STUB: SalesDashboard = {
  notebooks: [
    { nome: 'Dell XPS 15', quantidadeEstoque: 10, precoSP: 7824.52, precoGO: 7527.61, precoRS: 9325.39, precoAM: 7664.37, precoBA: 8188.35 }
  ],
  totaisPorEstado: ESTADO_TOTALS_STUB,
  lucroBrutoPorEstado: { totalSP: 17485.10, totalGO: 17485.10, totalRS: 17485.10, totalAM: 17485.10, totalBA: 17485.10 },
  reinvestimentoTI: 5828.37,
  totalGeral: 405302.40
};

const EMPTY_DASHBOARD: SalesDashboard = {
  notebooks: [],
  totaisPorEstado: { totalSP: 0, totalGO: 0, totalRS: 0, totalAM: 0, totalBA: 0 },
  lucroBrutoPorEstado: { totalSP: 0, totalGO: 0, totalRS: 0, totalAM: 0, totalBA: 0 },
  reinvestimentoTI: 0,
  totalGeral: 0
};

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let serviceSpy: jasmine.SpyObj<DashboardService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('DashboardService', ['getDashboard']);
    serviceSpy.getDashboard.and.returnValue(of(DASHBOARD_STUB));

    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [CommonModule, NoopAnimationsModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
      providers: [
        { provide: DashboardService, useValue: serviceSpy },
        { provide: LOCALE_ID, useValue: 'pt-BR' }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getDashboard on init', () => {
    expect(serviceSpy.getDashboard).toHaveBeenCalledOnceWith();
  });

  it('should populate dashboard after successful load', () => {
    expect(component.dashboard).toEqual(DASHBOARD_STUB);
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('');
  });

  it('should show error message when service fails', () => {
    serviceSpy.getDashboard.and.returnValue(
      throwError(() => ({ error: { message: 'Erro interno do servidor' } }))
    );
    component.load();

    expect(component.errorMessage).toBe('Erro interno do servidor');
    expect(component.loading).toBeFalse();
    expect(component.dashboard).toBeNull();
  });

  it('should use fallback error message when error has no message', () => {
    serviceSpy.getDashboard.and.returnValue(throwError(() => ({})));
    component.load();
    expect(component.errorMessage).toBeTruthy();
  });

  it('should handle empty notebooks list without error', () => {
    serviceSpy.getDashboard.and.returnValue(of(EMPTY_DASHBOARD));
    component.load();

    expect(component.dashboard?.notebooks.length).toBe(0);
    expect(component.errorMessage).toBe('');
  });

  it('should calculate correct cell value for a notebook and state', () => {
    const nb = DASHBOARD_STUB.notebooks[0];
    const cellValue = component.cellValue(nb, 'SP');
    expect(cellValue).toBeCloseTo(78245.20, 1);
  });

  it('should reset error on reload', () => {
    component.errorMessage = 'erro anterior';
    serviceSpy.getDashboard.and.returnValue(of(DASHBOARD_STUB));
    component.load();
    expect(component.errorMessage).toBe('');
  });
});
