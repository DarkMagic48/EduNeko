import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { LeccionService } from '../../services/leccion.service';
import { LeccionResponse } from '../../models/leccion.models';
import { ActividadService } from '../../services/actividad.service';
import { ActividadResponse } from '../../models/actividad.models';

@Component({
  selector: 'app-leccion',
  imports: [CommonModule],
  templateUrl: './leccion.html',
  styleUrl: './leccion.css'
})
export class Leccion implements OnInit {

  leccion: LeccionResponse | null = null;
  unidadId: number | null = null;

  cargando = true;
  generando = false;
  error = '';
  actividades: ActividadResponse[] = [];
  cargandoActividades = false;
  generandoActividades = false;
  errorActividades = '';

  respuestasSeleccionadas: Record<number, string> = {};

  constructor(
    private readonly leccionService: LeccionService,
    private readonly actividadService: ActividadService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(
      this.route.snapshot.paramMap.get('unidadId')
    );

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.cargando = false;
      this.error = 'La unidad solicitada no es válida.';
      return;
    }

    this.unidadId = id;
    this.cargarLeccion();
  }

  cargarLeccion(): void {
    if (this.unidadId === null) {
      return;
    }

    this.cargando = true;
    this.generando = false;
    this.error = '';
    this.leccion = null;

    this.leccionService.obtenerLeccion(this.unidadId).subscribe({
      next: (respuesta) => {
        this.leccion = respuesta;
        this.cargando = false;
        this.cargarActividades();
      },
      error: (err: HttpErrorResponse) => {
        this.cargando = false;

        if (err.status === 404) {
          // No existe una lección: generarla automáticamente.
          this.generarLeccion();
        } else if (err.status === 403) {
          this.error = 'No tienes acceso a esta unidad.';
        } else {
          this.error = 'No fue posible cargar la lección.';
        }
      }
    });
  }

  generarLeccion(): void {
    if (this.unidadId === null || this.generando) {
      return;
    }

    this.generando = true;
    this.error = '';

    this.leccionService.generarLeccion(this.unidadId).subscribe({
      next: (respuesta) => {
        this.leccion = respuesta;
        this.generando = false;
        this.cargarActividades();
      },
      error: () => {
        this.generando = false;
        this.error =
          'No fue posible preparar tu lección. Inténtalo nuevamente.';
      }
    });
  }

  reintentar(): void {
    this.cargarLeccion();
  }

  cargarActividades(): void {
  if (this.unidadId === null || this.cargandoActividades
      || this.generandoActividades) {
    return;
  }

  this.cargandoActividades = true;
  this.errorActividades = '';

  this.actividadService.obtenerActividades(this.unidadId).subscribe({
    next: (actividades) => {
      this.cargandoActividades = false;

      if (actividades.length > 0) {
        this.actividades = actividades;
      } else {
        this.generarActividades();
      }
    },
    error: () => {
      this.cargandoActividades = false;
      this.errorActividades =
        'No fue posible cargar las actividades.';
    }
  });
}

  generarActividades(): void {
    if (this.unidadId === null || this.generandoActividades) {
      return;
    }

    this.generandoActividades = true;
    this.errorActividades = '';

    this.actividadService.generarActividades(this.unidadId).subscribe({
      next: (actividades) => {
        this.actividades = actividades;
        this.generandoActividades = false;
      },
      error: () => {
        this.generandoActividades = false;
        this.errorActividades =
          'No fue posible preparar los ejercicios. Inténtalo nuevamente.';
      }
    });
  }

  seleccionarRespuesta(
    actividadId: number,
    respuesta: string
  ): void {
    this.respuestasSeleccionadas[actividadId] = respuesta;
  }
  volverAlPlan(): void {
    this.router.navigate(['/plan/matematicas']);
  }
}