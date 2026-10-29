
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { PlanAprendizajeService } from '../../services/plan-aprendizaje.service';
import {
  PlanAprendizajeResponse,
  PlanUnidadResponse
} from '../../models/plan-aprendizaje.models';

@Component({
  selector: 'app-plan-aprendizaje',
  imports: [CommonModule],
  templateUrl: './plan-aprendizaje.html',
  styleUrl: './plan-aprendizaje.css'
})
export class PlanAprendizaje implements OnInit {

  plan: PlanAprendizajeResponse | null = null;

  cargando = true;
  error = '';
  sinPlan = false;

  unidadExpandida: number | null = null;

  constructor(
    private readonly planService: PlanAprendizajeService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.cargarPlan();
  }

  cargarPlan(): void {
    this.cargando = true;
    this.error = '';
    this.sinPlan = false;

    this.planService.obtenerPlanActualMatematicas().subscribe({
      next: (respuesta) => {
        this.plan = respuesta;
        this.cargando = false;
      },
      error: (err) => {
        this.cargando = false;

        if (err.status === 404) {
          this.sinPlan = true;
        } else {
          this.error =
            'No fue posible cargar tu plan de aprendizaje.';
        }
      }
    });
  }

  generarPlan(): void {
    this.cargando = true;
    this.error = '';

    this.planService.generarPlanMatematicas().subscribe({
      next: () => this.cargarPlan(),
      error: () => {
        this.cargando = false;
        this.error =
          'No fue posible generar tu plan. Verifica que hayas completado el diagnóstico.';
      }
    });
  }

  alternarUnidad(id: number): void {
    this.unidadExpandida =
      this.unidadExpandida === id ? null : id;
  }

  etiquetaPrioridad(prioridad: number): string {
    switch (prioridad) {
      case 1:
        return 'Requiere refuerzo';
      case 2:
        return 'En desarrollo';
      default:
        return 'Fortaleza observada';
    }
  }

  clasePrioridad(prioridad: number): string {
    switch (prioridad) {
      case 1:
        return 'prioridad-alta';
      case 2:
        return 'prioridad-media';
      default:
        return 'prioridad-baja';
    }
  }

  identificarUnidad(
    index: number,
    unidad: PlanUnidadResponse
  ): number {
    return unidad.id;
  }

  volverDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  irDiagnostico(): void {
    this.router.navigate(['/diagnostico/matematicas']);
  }
}
