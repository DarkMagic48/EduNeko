
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';

import { Leccion } from './leccion';
import { LeccionService } from '../../services/leccion.service';
import { ActividadService } from '../../services/actividad.service';
import {
  ProgresoService,
  LogrosResponse
} from '../../services/progreso.service';

describe('EduNeko - Desbloqueo de insignias', () => {

  let componente: Leccion;

  // Simulación del servicio de progreso.
  const obtenerLogrosMock = vi.fn();

  beforeEach(() => {

    obtenerLogrosMock.mockReset();

    TestBed.configureTestingModule({
      providers: [

        // Evitar llamadas reales al backend.
        {
          provide: ProgresoService,
          useValue: {
            obtenerLogros: obtenerLogrosMock
          }
        },

        // Estos servicios no participan en esta prueba.
        {
          provide: LeccionService,
          useValue: {}
        },
        {
          provide: ActividadService,
          useValue: {}
        },

        // Simular las dependencias de navegación.
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1'
              }
            }
          }
        },
        {
          provide: Router,
          useValue: {}
        }
      ]
    });

    // Crear la instancia sin ejecutar ngOnInit().
    // Así evitamos cargar lecciones y actividades.
    componente = TestBed.runInInjectionContext(
      () => new Leccion(
        TestBed.inject(LeccionService),
        TestBed.inject(ActividadService),
        TestBed.inject(ProgresoService),
        TestBed.inject(ActivatedRoute),
        TestBed.inject(Router)
      )
    );
  });

  it('debe celebrar Constancia al detectar su desbloqueo', () => {

    // Estado inicial: Constancia bloqueada.
    const estadoInicial: LogrosResponse = {
      logros: [
        {
          codigo: 'CONSTANCIA',
          nombre: 'Constancia',
          descripcion: 'Aprendiste durante tres días consecutivos.',
          desbloqueado: false
        }
      ]
    };

    // Nueva respuesta: Constancia desbloqueada.
    const estadoActualizado: LogrosResponse = {
      logros: [
        {
          ...estadoInicial.logros[0],
          desbloqueado: true
        }
      ]
    };

    // Preparar la respuesta simulada del servicio.
    obtenerLogrosMock.mockReturnValue(
      of(estadoActualizado)
    );

    // Establecer el estado inicial conocido.
    (componente as any).logrosAnteriores =
      estadoInicial.logros.map(logro => ({ ...logro }));

    componente.logrosInicializados = true;

    // Ejecutar la lógica real de detección.
    (componente as any).verificarNuevosLogros();

    // Comprobar que se abrió la celebración.
    expect(componente.logroEnCelebracion?.codigo)
      .toBe('CONSTANCIA');

    // Cerrar la celebración.
    componente.cerrarCelebracionLogro();

    expect(componente.logroEnCelebracion)
      .toBeNull();

    // Consultar nuevamente el mismo estado.
    (componente as any).verificarNuevosLogros();

    // No debe celebrarse dos veces.
    expect(componente.logroEnCelebracion)
      .toBeNull();

    expect(obtenerLogrosMock)
      .toHaveBeenCalledTimes(2);
  });

});
