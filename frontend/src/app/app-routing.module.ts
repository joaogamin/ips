import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ParametrosComponent } from './features/parametros/parametros.component';
import { LotImportComponent } from './features/notebooks/lot-import.component';
import { NotebookListComponent } from './features/notebooks/notebook-list.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';

const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent },
  { path: 'parametros', component: ParametrosComponent },
  { path: 'notebooks', component: NotebookListComponent },
  { path: 'notebooks/lote', component: LotImportComponent },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
