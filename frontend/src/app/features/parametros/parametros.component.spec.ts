import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { of, throwError } from 'rxjs';

import { ParametrosComponent } from './parametros.component';
import { CostParameterService } from '../../core/services/cost-parameter.service';
import { CostParameter } from '../../core/models/cost-parameter.model';

const PARAM_STUB: CostParameter = { id: 1, parametro: 'taxa_devcoin', valor: 3.7259, descricao: 'Taxa Devcoin' };
const PARAM_STUB_2: CostParameter = { id: 2, parametro: 'taxa_importacao', valor: 0.1731, descricao: 'Taxa Importação' };

describe('ParametrosComponent', () => {
  let component: ParametrosComponent;
  let fixture: ComponentFixture<ParametrosComponent>;
  let serviceSpy: jasmine.SpyObj<CostParameterService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('CostParameterService', ['getAll', 'update']);
    serviceSpy.getAll.and.returnValue(of([PARAM_STUB, PARAM_STUB_2]));

    await TestBed.configureTestingModule({
      declarations: [ParametrosComponent],
      imports: [ReactiveFormsModule, NoopAnimationsModule, MatIconModule, MatButtonModule],
      providers: [{ provide: CostParameterService, useValue: serviceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(ParametrosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load parameters on init and populate rows', () => {
    expect(serviceSpy.getAll).toHaveBeenCalledOnceWith();
    expect(component.rows.length).toBe(2);
    expect(component.rows[0].data.parametro).toBe('taxa_devcoin');
    expect(component.rows[0].control.value).toBe(3.7259);
  });

  it('should show load error when service fails', () => {
    serviceSpy.getAll.and.returnValue(throwError(() => new Error('network error')));
    component.ngOnInit();
    expect(component.loadError).toBeTruthy();
  });

  it('should set row errorMessage when form control is invalid on save', () => {
    const row = component.rows[0];
    row.control.setValue(null);
    component.save(row);
    expect(row.errorMessage).toBeTruthy();
    expect(serviceSpy.update).not.toHaveBeenCalled();
  });

  it('should call update and set successMessage on successful save', fakeAsync(() => {
    const updated: CostParameter = { ...PARAM_STUB, valor: 4.0 };
    serviceSpy.update.and.returnValue(of(updated));

    const row = component.rows[0];
    row.control.setValue(4.0);
    component.save(row);

    expect(serviceSpy.update).toHaveBeenCalledWith(PARAM_STUB.id, 4.0);
    expect(row.successMessage).toBe('Salvo com sucesso!');
    expect(row.saving).toBeFalse();
    expect(row.data.valor).toBe(4.0);

    tick(3000);
    expect(row.successMessage).toBeNull();
  }));

  it('should set row errorMessage when save fails', () => {
    serviceSpy.update.and.returnValue(throwError(() => ({ error: { message: 'Valor inválido' } })));

    const row = component.rows[0];
    row.control.setValue(1.0);
    component.save(row);

    expect(row.errorMessage).toBe('Valor inválido');
    expect(row.saving).toBeFalse();
  });
});
