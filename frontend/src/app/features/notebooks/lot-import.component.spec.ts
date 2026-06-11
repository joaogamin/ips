import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { of, throwError } from 'rxjs';

import { LotImportComponent } from './lot-import.component';
import { NotebookService } from '../../core/services/notebook.service';

describe('LotImportComponent', () => {
  let component: LotImportComponent;
  let fixture: ComponentFixture<LotImportComponent>;
  let serviceSpy: jasmine.SpyObj<NotebookService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('NotebookService', ['registerLot']);

    await TestBed.configureTestingModule({
      declarations: [LotImportComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        MatIconModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatProgressSpinnerModule,
      ],
      providers: [{ provide: NotebookService, useValue: serviceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(LotImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty/null fields', () => {
    expect(component.form.get('nome')?.value).toBe('');
    expect(component.form.get('custoDev')?.value).toBeNull();
    expect(component.form.get('quantidade')?.value).toBeNull();
  });

  it('should have form invalid when all fields are empty', () => {
    expect(component.form.invalid).toBeTrue();
  });

  it('should have form invalid when custoDev is zero', () => {
    component.form.setValue({ nome: 'Dell XPS 15', custoDev: 0, quantidade: 5 });
    expect(component.form.invalid).toBeTrue();
  });

  it('should have form invalid when quantidade is zero', () => {
    component.form.setValue({ nome: 'Dell XPS 15', custoDev: 1200, quantidade: 0 });
    expect(component.form.invalid).toBeTrue();
  });

  it('should have form valid with correct values', () => {
    component.form.setValue({ nome: 'Dell XPS 15', custoDev: 1200, quantidade: 10 });
    expect(component.form.valid).toBeTrue();
  });

  it('should not call service when form is invalid on submit', () => {
    component.submit();
    expect(serviceSpy.registerLot).not.toHaveBeenCalled();
  });

  it('should call registerLot with form values on valid submit', () => {
    serviceSpy.registerLot.and.returnValue(of('Lote enviado para processamento.'));
    component.form.setValue({ nome: 'Dell XPS 15', custoDev: 1200, quantidade: 10 });

    component.submit();

    expect(serviceSpy.registerLot).toHaveBeenCalledWith('Dell XPS 15', 1200, 10);
  });

  it('should show success message and reset form after successful submit', () => {
    serviceSpy.registerLot.and.returnValue(of('Lote enviado para processamento.'));
    component.form.setValue({ nome: 'Dell XPS 15', custoDev: 1200, quantidade: 10 });

    component.submit();

    expect(component.successMessage).toBeTruthy();
    expect(component.errorMessage).toBe('');
    expect(component.loading).toBeFalse();
    expect(component.form.get('nome')?.value).toBeNull();
  });

  it('should show error message when service returns error', () => {
    serviceSpy.registerLot.and.returnValue(
      throwError(() => ({ error: { message: 'Erro interno' } }))
    );
    component.form.setValue({ nome: 'Dell XPS 15', custoDev: 1200, quantidade: 10 });

    component.submit();

    expect(component.errorMessage).toBe('Erro interno');
    expect(component.successMessage).toBe('');
    expect(component.loading).toBeFalse();
  });
});
