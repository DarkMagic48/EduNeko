import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { finalize } from 'rxjs';

import { EstudianteService } from '../../services/estudiante.service';

@Component({
  selector: 'app-onboarding',
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './onboarding.html',
  styleUrl: './onboarding.css',
})
export class Onboarding {

  perfilForm: FormGroup;

  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly estudianteService: EstudianteService
  ){
    this.perfilForm = this.fb.group({
      edad: [
        null,
      [
        Validators.required,
        Validators.min(10),
        Validators.max(18)
      ]
    ],
    gradoEscolar: [
      null,
      Validators.required
    ]
    });
  }

  guardarPerfil(): void {

    this.mensajeError = '';
    this.mensajeExito = '';

    if (this.perfilForm.invalid) {
      this.perfilForm.markAllAsTouched();
      return;
    }

    this.cargando = true;

    this.estudianteService
    .actualizarPerfil(this.perfilForm.value)
    .pipe(
      finalize(() => {
        this.cargando = false;
      })
    )
    .subscribe({
      next: response => {
        this.mensajeExito = response.mensaje;
      },

      error: error => {
        this.mensajeError =
          error.error?.mensaje ??
          'No fue posible guardar el perfil académico.';
      }
    });
  }
}
