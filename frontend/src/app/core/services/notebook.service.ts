import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notebook } from '../models/notebook.model';

@Injectable({
  providedIn: 'root'
})
export class NotebookService {

  private readonly url = `${environment.apiUrl}/notebooks`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Notebook[]> {
    return this.http.get<Notebook[]>(this.url);
  }

  registerLot(nome: string, custoDev: number, quantidade: number): Observable<string> {
    return this.http.post(this.url + '/lote', { nome, custoDev, quantidade }, { responseType: 'text' });
  }
}
