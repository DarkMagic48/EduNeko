
import { Component } from '@angular/core';
import {
  ReactiveFormsModule,
  FormControl,
  FormGroup,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize, switchMap } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import { EstudianteService } from '../../services/estudiante.service';
import { LoginRequest } from '../../models/auth.models';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
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
      Validators.required,
      Validators.minLength(8),
      // Coincide con el requisito de registro, sin revelar credenciales del servidor.
      Validators.pattern(/^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$/)
    ])
  });

  constructor(
    private readonly authService: AuthService,
    private readonly estudianteService: EstudianteService,
    private readonly router: Router
  ) {}

  iniciarSesion(): void {

    // Limpiar mensajes anteriores.
    this.mensajeError = '';

    // Validar el formulario antes de enviar la solicitud.
    if (this.formularioLogin.invalid) {
      this.formularioLogin.markAllAsTouched();
      return;
    }

    // Preparar las credenciales del estudiante.
    const datosLogin: LoginRequest = {
      correo: this.formularioLogin.value.correo ?? '',
      password: this.formularioLogin.value.password ?? ''
    };

    // Un nuevo inicio de sesión no debe reutilizar el token de otra cuenta.
    this.authService.logout();

    // Mantener el estado de carga durante ambas consultas.
    this.cargando = true;

    this.authService.login(datosLogin)
      .pipe(

        // Una vez autenticado, consultar el estado del onboarding.
        // AuthService ya habrá guardado el token JWT.
        switchMap(() =>
          this.estudianteService.obtenerEstadoOnboarding()
        ),

        // Finalizar la carga al terminar las consultas.
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({

        next: (estado) => {

          if (estado.onboardingCompletado) {

            // El estudiante ya terminó su configuración.
            // Puede acceder directamente al Dashboard.
            this.router.navigate(['/dashboard']);

          } else {

            // El estudiante todavía necesita completar
            // su configuración inicial.
            this.router.navigate(['/bienvenida']);
          }
        },

        error: error => {

          // Si falló el login o la verificación posterior, no dejar sesión parcial.
          this.authService.logout();

          // Mostrar el error sin asumir que el onboarding
          // está completo o incompleto.
          // No revelar si existe el correo: evita enumeración de cuentas.
          // Las reglas de formato se muestran directamente en cada campo.
          this.mensajeError = error.status === 401 || error.status === 403
            ? 'Correo o contraseña incorrectos. Revisa tus datos e inténtalo de nuevo.'
            : 'No fue posible iniciar sesión o verificar tu configuración. Intenta nuevamente.';
        }
      });
  }
}
