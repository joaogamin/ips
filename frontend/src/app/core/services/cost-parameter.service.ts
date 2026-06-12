import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CostParameter } from '../models/cost-parameter.model';

@Injectable({
  providedIn: 'root'
})
export class CostParameterService {

  private readonly url = `${environment.apiUrl}/parametros`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<CostParameter[]> {
    return this.http.get<CostParameter[]>(this.url);
  }

  update(id: number, valor: number): Observable<CostParameter> {
    return this.http.put<CostParameter>(`${this.url}/${id}`, { valor });
  }
}
