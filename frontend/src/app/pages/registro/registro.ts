import { Component } from '@angular/core';
import {
  ReactiveFormsModule,
  FormControl,
  FormGroup,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import { RegistroRequest } from '../../models/auth.models';

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class Registro {

  mensajePassword = '';
  mensajeError = '';
  cargando = false;

  formularioRegistro = new FormGroup({
    nombre: new FormControl('', [
      Validators.required
    ]),
    correo: new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl('', [
      Validators.required
    ]),
    confirmarPassword: new FormControl('', [
      Validators.required
    ])
  });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  registrar(): void {

    this.mensajePassword = '';
    this.mensajeError = '';

    if (this.formularioRegistro.invalid) {
      this.formularioRegistro.markAllAsTouched();
      return;
    }

    if (
      this.formularioRegistro.value.password !==
      this.formularioRegistro.value.confirmarPassword
    ) {
      this.mensajePassword = 'Las contraseñas no coinciden.';
      return;
    }

    const datosRegistro: RegistroRequest = {
      nombre: this.formularioRegistro.value.nombre ?? '',
      correo: this.formularioRegistro.value.correo ?? '',
      password: this.formularioRegistro.value.password ?? '',
      confirmarPassword:
        this.formularioRegistro.value.confirmarPassword ?? ''
    };

    this.cargando = true;

    this.authService.registrar(datosRegistro).pipe(
      finalize(() => {
        this.cargando = false;
      })
    ).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: error => {
        this.mensajeError =
          error.error?.mensaje ?? 'No fue posible crear la cuenta.';
      }
    });
  }
}