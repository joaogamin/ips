import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotebookService } from '../../core/services/notebook.service';
import { Notebook } from '../../core/models/notebook.model';

@Component({
  selector: 'app-notebook-list',
  templateUrl: './notebook-list.component.html',
  styleUrls: ['./notebook-list.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotebookListComponent implements OnInit {

  notebooks: Notebook[] = [];
  loading = false;
  errorMessage = '';

  constructor(private notebookService: NotebookService, private router: Router, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.notebookService.getAll().subscribe({
      next: (data) => {
        this.notebooks = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar notebooks.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  goToLote(): void {
    this.router.navigate(['/notebooks/lote']);
  }
}
