import { interval } from "rxjs";

export type EstadoLeccion =
  | 'BLOQUEADA'
  | 'DISPONIBLE'
  | 'EN_PROGRESO'
  | 'COMPLETADA';

export interface BarraFraccionariaDatos {
  numerador: number;
  denominador: number;
}

export interface RectaNumericaDatos {
  minimo: number;
  maximo: number;
  intervalo: number;
  valorDestacado: number;
}

/**
 * Figuras geométricas que EduNeko podrá representar
 * en las lecciones de Matemáticas.
 */
export type TipoFigura2D =
  | 'cuadrado'
  | 'rectangulo'
  | 'triangulo'
  | 'circulo';

export type ModalidadPatronFiguras2D =
  | 'crecimiento'
  | 'repetitivo';

  /**
 * Describe una figura individual dentro de una sucesión.
 *
 * Reutilizamos TipoFigura2D para mantener la compatibilidad
 * con el componente de figuras geométricas ya implementado.
 */
export interface ElementoPatronFiguras2D {
  posicion: number;
  figura: TipoFigura2D;

  // Dimensiones opcionales según el tipo de figura.
  lado?: number;
  base?: number;
  altura?: number;
  radio?: number;
  etiqueta?: string;
  perimetro?: number;
}

export interface PatronFiguras2DDatos {
  modalidad: ModalidadPatronFiguras2D;

  // Unidad utilizada en las medidas: mm, cm, m o km.
  unidad: string;

  // Entre 2 y 6 elementos para mantener la legibilidad.
  elementos: ElementoPatronFiguras2D[];

  // Regla matemática o descripción breve del patrón.
  regla: string;
}

/**
 * Datos de una figura geométrica.
 *
 * Solo se utilizan las dimensiones que correspondan
 * al tipo de figura seleccionado.
 */
export interface FiguraGeometrica2DDatos {
  figura: TipoFigura2D;

  // Dimensiones expresadas en la unidad indicada.
  lado?: number;
  base?: number;
  altura?: number;
  radio?: number;

  // Unidad de medida, por ejemplo: cm, m.
  unidad: string;
}

export interface PuntoCartesiano {
  x: number;
  y: number;
  etiqueta: string;
}

export interface PlanoCartesianoDatos {
  minimoX: number;
  maximoX: number;
  minimoY: number;
  maximoY: number;
  intervalo: number;
  puntos: PuntoCartesiano[];
}

export interface DatosEstadistico {
  etiqueta: string;
  valor: number;
}

export interface GraficaEstadisticaDatos {
  modalidad: 'barras' | 'lineas';
  unidad: string;
  elementos: DatosEstadistico[];
}
/**
 * Recursos visuales compatibles con EduNeko.
 *
 * Cada tipo de recurso tiene su propia estructura
 * de datos, lo que permite que Angular identifique
 * correctamente las propiedades correspondientes.
 */
export type RecursoVisualLeccion =
  | {
      tipo: 'barra_fraccionaria';
      titulo: string;
      datos: BarraFraccionariaDatos;
    }
  | {
      tipo: 'recta_numerica';
      titulo: string;
      datos: RectaNumericaDatos;
    }
  | {
    tipo: 'figura_geometrica_2d';
    titulo: string;
    datos: FiguraGeometrica2DDatos;
    }
  | {
    tipo: 'patron_figuras_2d';
    titulo: string;
    datos: PatronFiguras2DDatos;
    }
  | {
      tipo: 'patron_figuras_2d';
      titulo: string;
      datos: PatronFiguras2DDatos;
    }
  | {
      tipo: 'plano_cartesiano';
      titulo: string;
      datos: PlanoCartesianoDatos;
    }
  | {
      tipo: 'grafica_estadistica';
      titulo: string;
      datos: GraficaEstadisticaDatos;
    };

export interface LeccionResponse {
  id: number;
  plan_unidad_id: number;
  titulo: string;
  introduccion: string;
  explicacion: string;
  ejemplo_resuelto: string;
  orden: number;
  estado: EstadoLeccion;

  /**
   * Recurso visual personalizado.
   *
   * Puede ser null cuando la lección no necesita
   * un diagrama o fue generada anteriormente.
   *
   * El signo ? mantiene la compatibilidad si
   * alguna respuesta antigua omite el campo.
   *//**
   */ 
  recurso_visual?: RecursoVisualLeccion | null;
}
