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
import { Materia } from '../../models/estudiante.models';

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
  materiasDisponibles: Materia[] = [];
  materiasSeleccionadas: number[] = [];

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
          this.mensajeExito = '';
          this.pasoActual = 3;
          this.cargarMaterias();
        },

        error: error => {
          this.mensajeError =
            error.error?.mensaje ??
            'No fue posible guardar las preferencias de aprendizaje.'; 
        }
      });
  }

  cargarMaterias(): void {
  this.mensajeError = '';
  this.materiasDisponibles = [];
  this.materiasSeleccionadas = [];
  this.cargando = true;

  this.estudianteService
    .obtenerMateriasDisponibles()
    .pipe(
      finalize(() => {
        this.cargando = false;
      })
    )
    .subscribe({
      next: materias => {
        this.materiasDisponibles = materias;
      },

      error: error => {
        this.mensajeError =
          error.error?.mensaje ??
          'No fue posible cargar las materias disponibles.';
      }
    });
  }

  alternarMateria(materiaId: number): void {

    if (this.cargando) {
      return;
    }

    const indice = this.materiasSeleccionadas.indexOf(materiaId);

    if (indice >= 0) {
      this.materiasSeleccionadas.splice(indice, 1);
    } else {
      this.materiasSeleccionadas.push(materiaId);
    }

    this.mensajeError = '';
  }

  guardarMaterias(): void {

  this.mensajeError = '';
  this.mensajeExito = '';

  if (this.cargando) {
    return;
  }

  if (this.materiasSeleccionadas.length === 0) {
    this.mensajeError =
      'Debes seleccionar al menos una materia.';
    return;
  }

  this.cargando = true;

  this.estudianteService
    .seleccionarMaterias({
      materiaIds: this.materiasSeleccionadas
    })
    .pipe(
      finalize(() => {
        this.cargando = false;
      })
    )
    .subscribe({
      next: materias => {
        this.materiasSeleccionadas =
          materias.map(materia => materia.id);

        this.mensajeExito =
          'Materias guardadas correctamente.';
      },

      error: error => {
        this.mensajeError =
          error.error?.mensaje ??
          'No fue posible guardar las materias.';
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
