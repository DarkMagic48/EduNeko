
import { Component } from '@angular/core';
import { Router } from '@angular/router';
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

  // Formularios existentes del onboarding.
  perfilForm: FormGroup;
  preferenciasForm: FormGroup;

  // Estados de la interfaz.
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  // Nuevo orden:
  // 1 = Preferencias
  // 2 = Perfil académico
  // 3 = Materias
  pasoActual = 1;

  materiasDisponibles: Materia[] = [];
  materiasSeleccionadas: number[] = [];
  // Indica si las materias se guardaron correctamente.
  materiasGuardadas = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly estudianteService: EstudianteService,
    private readonly router: Router
  ) {

    // Formulario del perfil académico.
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

    // Formulario de preferencias de aprendizaje.
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

  /**
   * PASO 1: Guardar las preferencias de aprendizaje.
   *
   * Después de guardar correctamente, avanzamos
   * al perfil académico.
   */
  guardarPreferencias(): void {

    this.mensajeError = '';
    this.mensajeExito = '';

    // Validar los campos obligatorios y comprobar
    // que exista al menos una modalidad seleccionada.
    if (
      this.preferenciasForm.invalid ||
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

        next: () => {

          // Preferencias guardadas.
          // Continuar con el perfil académico.
          this.mensajeExito = '';
          this.pasoActual = 2;
        },

        error: error => {
          this.mensajeError =
            error.error?.mensaje ??
            'No fue posible guardar las preferencias de aprendizaje.';
        }
      });
  }

  /**
   * PASO 2: Guardar el perfil académico.
   *
   * El grado escolar es necesario para consultar
   * las materias disponibles en el siguiente paso.
   */
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

        next: () => {

          // Perfil guardado.
          // Avanzar a materias y cargar el catálogo.
          this.mensajeExito = '';
          this.pasoActual = 3;
          this.cargarMaterias();
        },

        error: error => {
          this.mensajeError =
            error.error?.mensaje ??
            'No fue posible guardar el perfil académico.';
        }
      });
  }

  /**
   * PASO 3: Consultar las materias disponibles
   * según el grado escolar del estudiante.
   */
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

  /**
   * Agregar o quitar una materia de la selección.
   */
  
  /**
   * Agrega o elimina una materia de la selección.
   * Si el estudiante modifica su selección,
   * deberá guardar nuevamente las materias antes
   * de poder continuar al diagnóstico.
   */
  alternarMateria(materiaId: number): void {

    // Evitar modificaciones mientras se guardan datos.
    if (this.cargando) {
      return;
    }

    // Invalidar el guardado anterior al cambiar la selección.
    this.materiasGuardadas = false;
    this.mensajeExito = '';

    // Buscar si la materia ya está seleccionada.
    const indice =
      this.materiasSeleccionadas.indexOf(materiaId);

    if (indice >= 0) {
      // Si ya estaba seleccionada, eliminarla.
      this.materiasSeleccionadas.splice(indice, 1);
    } else {
      // Si no estaba seleccionada, agregarla.
      this.materiasSeleccionadas.push(materiaId);
    }

    // Limpiar mensajes de error anteriores.
    this.mensajeError = '';
  }

  /**
   * Guarda las materias seleccionadas por el estudiante.
   * No realiza ninguna redirección automática.
   */
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

    // Ocultar el botón de diagnóstico mientras se guarda.
    this.materiasGuardadas = false;
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

        next: (materias) => {

          // Actualizar la selección confirmada por el backend.
          this.materiasSeleccionadas =
            materias.map(materia => materia.id);

          // Confirmar que las materias se guardaron.
          this.materiasGuardadas = true;

          // Mostrar el mensaje sin cambiar de pantalla.
          this.mensajeExito =
            'Materias guardadas correctamente.';
        },

        error: (error) => {

          this.materiasGuardadas = false;

          this.mensajeError =
            error.error?.mensaje ??
            'No fue posible guardar las materias.';
        }

      });
  }

  /**
   * Comprobar que el estudiante haya seleccionado
   * al menos una modalidad de aprendizaje.
   */
  algunaModalidadSeleccionada(): boolean {

    const preferencias = this.preferenciasForm.value;

    return !!(
      preferencias.prefiereLecturaEscritura ||
      preferencias.prefiereVisual ||
      preferencias.prefiereAuditivo ||
      preferencias.prefierePractica
    );
  }

  /**
   * Redirige al estudiante al diagnóstico inicial
   * de Matemáticas después de guardar sus materias.
   */
  irAlDiagnostico(): void {

    // Evitar la navegación mientras se guardan datos.
    if (this.cargando) {
      return;
    }

    // Confirmar que las materias se guardaron correctamente.
    if (!this.materiasGuardadas) {
      return;
    }

    // Abrir el diagnóstico existente.
    this.router.navigate(['/diagnostico/matematicas']);
  }
}
