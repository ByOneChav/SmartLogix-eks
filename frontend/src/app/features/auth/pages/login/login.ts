import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AuthService} from '../../services/auth';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  login(): void {
    this.loading = true;
    this.error = '';
    this.authService.login({
      email: this.email,
      password: this.password,
    }).subscribe({
      next:(res) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('user', JSON.stringify(res.user));

        this.router.navigate(['/dashboard']);
        // alert('login correcto');

        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    })
  }

}
