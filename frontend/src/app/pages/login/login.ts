import { Component } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.models';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  formularioLogin = new FormGroup({
    correo: new FormControl(''),
    password: new FormControl('')
  });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  iniciarSesion(): void {

    const datosLogin: LoginRequest = {
      correo: this.formularioLogin.value.correo ?? '',
      password: this.formularioLogin.value.password ?? ''
    };

    this.authService.login(datosLogin).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: error => {
        console.error('Error al iniciar sesión:', error);
      }
    });
  }
}