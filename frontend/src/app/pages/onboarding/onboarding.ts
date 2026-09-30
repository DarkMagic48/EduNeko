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
  preferenciasForm: FormGroup;

  cargando = false;
  mensajeError = '';
  mensajeExito = '';
  pasoActual = 1;

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

    this.preferenciasForm = this.fb.group({
      nivelExplicacion: [null, Validators.required],
      nivelRetoPreferido: [null, Validators.required],
      ritmoEstudio: [null, Validators.required],

      prefiereLecturaEscritura: [false],
      prefiereVisual: [false],
      prefiereAuditivo: [false],
      prefierePractica: [false]
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
        this.mensajeExito = '';
        this.pasoActual = 2;
      },

      error: error => {
        this.mensajeError =
          error.error?.mensaje ??
          'No fue posible guardar el perfil académico.';
      }
    });
  }

  guardarPreferencias(): void {

    this.mensajeError = '';
    this.mensajeExito = '';

    if (this.preferenciasForm.invalid ||
      !this.algunaModalidadSeleccionada()
    ) {
      this.preferenciasForm.markAllAsTouched();

    if (!this.algunaModalidadSeleccionada()) {
    this.mensajeError =
      'Selecciona al menos una forma de aprendizaje.';
    }

      return;
    }

    this.cargando = true;

    this.estudianteService
      .actualizarPreferencias(this.preferenciasForm.value)
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
            'No fue posible guardar las preferencias de aprendizaje.'; 
        }
      });
  }

  algunaModalidadSeleccionada(): boolean {
    const preferencias = this.preferenciasForm.value;

    return !!(
      preferencias.prefiereLecturaEscritura ||
      preferencias.prefiereVisual ||
      preferencias.prefiereAuditivo ||
      preferencias.prefierePractica
    );
  }
}
