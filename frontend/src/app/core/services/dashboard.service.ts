import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SalesDashboard } from '../models/sales-dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private readonly url = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<SalesDashboard> {
    return this.http.get<SalesDashboard>(this.url);
  }
}
