
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
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

  
/**
 * Referencia al contenedor horizontal de las unidades.
 * Se utiliza para desplazar las tarjetas con las flechas.
 */
@ViewChild('pistaUnidades')
pistaUnidades?: ElementRef<HTMLDivElement>;

  /**
   * Desplaza el carrusel una tarjeta hacia
   * la izquierda o hacia la derecha.
   *
   * direccion:
   * -1 = izquierda
   *  1 = derecha
   */
  moverCarrusel(direccion: number): void {
    const pista = this.pistaUnidades?.nativeElement;

    if (!pista) {
      return;
    }

    // Calcula el ancho de la primera tarjeta.
    const tarjeta = pista.querySelector<HTMLElement>('.tarjeta-unidad');

    if (!tarjeta) {
      return;
    }

    // Incluye el espacio entre las tarjetas.
    const estilos = window.getComputedStyle(pista);
    const separacion = parseFloat(estilos.columnGap) || 0;

    const desplazamiento = tarjeta.getBoundingClientRect().width + separacion;

    // Realiza un desplazamiento horizontal suave.
    pista.scrollBy({
      left: direccion * desplazamiento,
      behavior: 'smooth'
    });
  }

  calcularProgresoGeneral(): number {
  if (!this.plan || this.plan.unidades.length === 0) {
    return 0;
  }

  const sumaProgresos = this.plan.unidades.reduce(
    (total, unidad) => total + unidad.progresoPorcentaje,
    0
  );

    return Math.round(
      sumaProgresos / this.plan.unidades.length
    );
  }

  
  /**
   * Selecciona un icono representativo según
   * el tema de la unidad personalizada.
   *
   * Los iconos son caracteres Unicode, por lo
   * que no necesitamos instalar librerías.
   */
  obtenerIconoUnidad(titulo: string): string {

    // Normalizar el título para facilitar la búsqueda.
    const tema = titulo
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');

    // Geometría, figuras y medición.
    if (
      tema.includes('geometr') ||
      tema.includes('medicion') ||
      tema.includes('figuras')
    ) {
      return '📐';
    }

    // Álgebra, patrones y expresiones.
    if (
      tema.includes('algebra') ||
      tema.includes('patrones') ||
      tema.includes('ecuacion')
    ) {
      return '🔢';
    }

    // Números enteros y recta numérica.
    if (
      tema.includes('numeros') ||
      tema.includes('recta numerica') ||
      tema.includes('enteros')
    ) {
      return '↔️';
    }

    // Operaciones y jerarquía.
    if (
      tema.includes('operaciones') ||
      tema.includes('jerarquia') ||
      tema.includes('aritmetica')
    ) {
      return '➗';
    }

    // Estadística, gráficas y probabilidad.
    if (
      tema.includes('estadistica') ||
      tema.includes('graficas') ||
      tema.includes('probabilidad')
    ) {
      return '📊';
    }

    // Proporcionalidad y porcentajes.
    if (
      tema.includes('proporcionalidad') ||
      tema.includes('porcentajes')
    ) {
      return '⚖️';
    }

    // Icono general para temas no identificados.
    return '📘';
  }


  volverDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  irDiagnostico(): void {
    this.router.navigate(['/diagnostico/matematicas']);
  }

  comenzarLeccion(unidad: PlanUnidadResponse): void {
  if (unidad.estado === 'BLOQUEADA') {
    return;
  }

  this.router.navigate([
    '/lecciones/unidades',
    unidad.id
    ]);
  }
}
