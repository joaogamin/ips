import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { NotebookService } from '../../core/services/notebook.service';

@Component({
  selector: 'app-lot-import',
  templateUrl: './lot-import.component.html',
  styleUrls: ['./lot-import.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LotImportComponent {

  @ViewChild(FormGroupDirective) formDirective!: FormGroupDirective;

  form: FormGroup;
  successMessage = '';
  errorMessage = '';
  loading = false;

  constructor(private fb: FormBuilder, private notebookService: NotebookService, private cdr: ChangeDetectorRef) {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(1)]],
      custoDev: [null, [Validators.required, Validators.min(0.01)]],
      quantidade: [null, [Validators.required, Validators.min(1)]]
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    const { nome, custoDev, quantidade } = this.form.value;

    this.notebookService.registerLot(nome, custoDev, quantidade).subscribe({
      next: () => {
        this.successMessage = 'Lote enviado para processamento. O estoque será atualizado em breve.';
        this.formDirective.resetForm();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.message || 'Erro ao enviar o lote.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }
}
