import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Inventario } from '../models/inventario.model';

@Injectable({
  providedIn: 'root'
})
export class InventarioService {

  private apiUrl = `${environment.apiUrl}/api/inventario`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<Inventario[]> {
    return this.http.get<Inventario[]>(`${this.apiUrl}/getAllInventario`);
  }

  create(data: Inventario): Observable<Inventario> {
    return this.http.post<Inventario>(`${this.apiUrl}/addInventario`, data);
  }

  update(id: number, data: Inventario): Observable<Inventario> {
    return this.http.put<Inventario>(`${this.apiUrl}/updInventario/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delInventario/${id}`);
  }
}
