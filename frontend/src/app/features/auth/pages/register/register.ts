import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  name = '';
  email = '';
  password = '';
  rol = 'USER';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) { }

  register(): void {
    this.loading = true;
    this.error = '';

    this.authService.register({
      name: this.name,
      email: this.email,
      password: this.password,
      rol: this.rol
    })
      .subscribe({
        next: () => {

          this.loading = false;

          Swal.fire({
            icon: 'success',
            title: 'Registro exitoso',
            text: 'Ahora puedes iniciar sesión',
            confirmButtonText: 'Continuar'
          }).then(() => {
            this.router.navigate(['/login']);
          });
        },

        error: (err) => {

          this.loading = false;

          this.error = err.error?.message || 'Error al registrar usuario';
        }
      });
  }
}

