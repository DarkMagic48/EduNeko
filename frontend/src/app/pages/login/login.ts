import { Component } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.models';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  mensajeError = '';
  cargando = false;

  formularioLogin = new FormGroup({
    correo: new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl('', [
      Validators.required
    ])
  });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  iniciarSesion(): void {

    this.mensajeError = '';

    if(this.formularioLogin.invalid) {
      this.formularioLogin.markAllAsTouched();
      return;
    }

    const datosLogin: LoginRequest = {
      correo: this.formularioLogin.value.correo ?? '',
      password: this.formularioLogin.value.password ?? ''
    };

    this.cargando = true;

    this.authService.login(datosLogin)
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
    )
    .subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: error => {
        this.mensajeError =
          error.error?.mensaje ?? 'No fue posible iniciar sesión.';
      }
    });
  }
}