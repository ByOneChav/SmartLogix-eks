import { Injectable } from '@angular/core';
import {Observable, of, delay, throwError} from 'rxjs';
import {LoginRequestModel} from '../models/login-request.model';
import {LoginResponseModel} from '../models/login-response.model';
import {environment} from '../../../../environments/environment';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api/auth`;

  constructor(private http: HttpClient) {}

  login(data: LoginRequestModel): Observable<LoginResponseModel> {
    return this.http.post<LoginResponseModel>(`${this.apiUrl}/login`, data);
  }

  register(data: { name: string; email: string; password: string; rol: string }): Observable<LoginResponseModel> {
    return this.http.post<LoginResponseModel>(`${this.apiUrl}/register`, data);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  isLogged(): boolean {
    return !!localStorage.getItem('token');
  }
}
