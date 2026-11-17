import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { EstudianteService } from '../../services/estudiante.service';
import { ActualizarPreferenciasRequest } from '../../models/estudiante.models';

/** Edición independiente de perfil y preferencias sin reabrir el onboarding. */
@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css'
})
export class Perfil implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly estudiantes = inject(EstudianteService);

  cargando = true;
  guardandoPerfil = false;
  guardandoPreferencias = false;
  error = '';
  errorPerfil = '';
  errorPreferencias = '';
  exitoPerfil = '';
  exitoPreferencias = '';
  gradoGuardado: number | null = null;

  readonly perfilForm = this.fb.nonNullable.group({
    edad: [12, [Validators.required, Validators.min(10), Validators.max(18)]],
    gradoEscolar: [1, [Validators.required, Validators.min(1), Validators.max(3)]]
  });

  readonly preferenciasForm = this.fb.nonNullable.group({
    nivelExplicacion: this.fb.nonNullable.control<'BREVE' | 'EQUILIBRADA' | 'DETALLADA'>(
      'EQUILIBRADA', Validators.required),
    nivelRetoPreferido: this.fb.nonNullable.control<'BAJO' | 'EQUILIBRADO' | 'ALTO'>(
      'EQUILIBRADO', Validators.required),
    ritmoEstudio: this.fb.nonNullable.control<'TRANQUILO' | 'NORMAL' | 'INTENSIVO'>(
      'NORMAL', Validators.required),
    prefiereLecturaEscritura: false,
    prefiereVisual: false,
    prefiereAuditivo: false,
    prefierePractica: false
  });

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;
    this.error = '';
    forkJoin({
      perfil: this.estudiantes.obtenerPerfil(),
      preferencias: this.estudiantes.obtenerPreferencias()
    }).pipe(finalize(() => { this.cargando = false; }))
      .subscribe({
        next: ({ perfil, preferencias }) => {
          if (perfil.edad === null || perfil.gradoEscolar === null) {
            this.error = 'Completa primero tu configuración inicial.';
            return;
          }
          this.gradoGuardado = perfil.gradoEscolar;
          this.perfilForm.setValue({
            edad: perfil.edad,
            gradoEscolar: perfil.gradoEscolar
          });
          this.preferenciasForm.setValue(preferencias);
        },
        error: () => {
          this.error = 'No fue posible consultar tu perfil. Inténtalo nuevamente.';
        }
      });
  }

  guardarPerfil(): void {
    if (this.perfilForm.invalid || this.cargando || this.guardandoPerfil ||
        this.gradoGuardado === null) {
      this.perfilForm.markAllAsTouched();
      return;
    }
    this.errorPerfil = '';
    this.exitoPerfil = '';
    const datos = this.perfilForm.getRawValue();

    if (datos.gradoEscolar !== this.gradoGuardado) {
      // El backend cancelará solo los intentos pendientes, nunca el historial.
      const confirmar = window.confirm(
        'Al cambiar de grado comenzarás un diagnóstico para el nuevo grado. ' +
        'Si tienes un diagnóstico pendiente del grado anterior, se cancelará ' +
        'sin borrar tu historial. El plan del grado anterior se conservará, ' +
        'pero dejará de ser el plan del grado actual. ¿Deseas continuar?'
      );
      if (!confirmar) {
        return;
      }
    }

    this.guardandoPerfil = true;
    this.estudiantes.actualizarPerfil(datos)
      .pipe(finalize(() => { this.guardandoPerfil = false; }))
      .subscribe({
        next: respuesta => {
          this.gradoGuardado = respuesta.gradoEscolar;
          this.exitoPerfil = 'Perfil académico actualizado correctamente.';
        },
        error: () => {
          this.errorPerfil = 'No se pudo actualizar el perfil. Inténtalo nuevamente.';
        }
      });
  }

  guardarPreferencias(): void {
    if (this.preferenciasForm.invalid || this.cargando || this.guardandoPreferencias) {
      this.preferenciasForm.markAllAsTouched();
      return;
    }
    this.errorPreferencias = '';
    this.exitoPreferencias = '';
    const datos: ActualizarPreferenciasRequest = this.preferenciasForm.getRawValue();
    if (!(datos.prefiereLecturaEscritura || datos.prefiereVisual ||
          datos.prefiereAuditivo || datos.prefierePractica)) {
      this.errorPreferencias = 'Selecciona al menos una forma de aprendizaje.';
      return;
    }
    this.guardandoPreferencias = true;
    this.estudiantes.actualizarPreferencias(datos)
      .pipe(finalize(() => { this.guardandoPreferencias = false; }))
      .subscribe({
        next: () => {
          this.exitoPreferencias = 'Preferencias actualizadas correctamente.';
        },
        error: () => {
          this.errorPreferencias = 'No fue posible guardar las preferencias.';
        }
      });
  }
}
