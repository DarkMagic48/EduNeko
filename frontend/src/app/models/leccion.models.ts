
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
 * Información algebraica opcional para resolver
 * una dimensión desconocida de un rectángulo.
 *
 * Solo se utiliza cuando el ejercicio realmente
 * contiene una incógnita.
 */
export interface AlgebraRectanguloDatos {

  // Variable utilizada en el procedimiento algebraico.
  incognita: 'x';

  // Expresión algebraica de la base.
  expresionBase: string;

  // Expresión algebraica de la altura.
  expresionAltura: string;

  // Área conocida indicada en el problema.
  areaConocida: number;
}

/**
 * Representación de figuras geométricas de EduNeko.
 *
 * Conserva la compatibilidad con los recursos
 * visuales que ya están implementados.
 */
export interface FiguraGeometrica2DDatos {
  figura: TipoFigura2D;

  lado?: number;
  base?: number;
  altura?: number;
  radio?: number;

  unidad: string;

  // Información opcional para ejercicios algebraicos.
  // Los recursos anteriores funcionan sin ella.
  algebra?: AlgebraRectanguloDatos;
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
/** Gráfica de funciones lineales y cuadráticas: y = ax² + bx + c. */
export interface GraficaFuncionDatos {
  a: number;
  b: number;
  c: number;
  minimoX: number;
  maximoX: number;
  valorX: number;
}

/** Modelo de proporcionalidad directa con cantidades positivas. */
export interface ModeloProporcionalidadDatos {
  cantidadBase: number;
  cantidadCorrespondiente: number;
  unidadBase: string;
  unidadCorrespondiente: string;
}

/** Triángulo rectángulo: la hipotenusa se calcula con Pitágoras. */
export interface TrianguloRectanguloDatos {
  catetoA: number;
  catetoB: number;
  unidad: string;
}

/** Modelo de probabilidad clásica con casos equiprobables. */
export interface ModeloProbabilidadDatos {
  favorables: number;
  total: number;
  evento: string;
}

/** Prisma rectangular con dimensiones reales y volumen. */
export interface PrismaRectangularDatos {
  largo: number;
  ancho: number;
  alto: number;
  unidad: string;
}

/**
 * Recursos visuales compatibles con EduNeko.
 *
 * Cada tipo de recurso tiene su propia estructura
 * de datos, lo que permite que Angular identifique
 * correctamente las propiedades correspondientes.
 */

/** Segmento: longitud unidimensional, nunca se dibuja como rectángulo. */
export interface SegmentoGeometricoDatos { longitud: number; unidad: string; }

/** Ecuación de primer grado ax + b = c, con solución única. */
export interface ModeloEcuacionDatos { a: number; b: number; c: number; }

/** Tabla de pares numéricos: patrones, proporcionalidad, funciones o conversiones. */
export interface TablaValoresDatos {
  encabezadoX: string; encabezadoY: string;
  pares: { x: number; y: number }[];
}

/** Diagrama conceptual para temas sin representación geométrica válida. */
export interface MapaConceptualDatos {
  conceptos: { etiqueta: string; detalle: string }[];
}

/** Ángulo medido en grados sexagesimales. */
export interface AnguloGeometricoDatos { grados: number; }

/** Distribución porcentual de un total (suma 100). */
export interface GraficaCircularDatos {
  segmentos: { etiqueta: string; porcentaje: number }[];
}

/** Construcciones trazadas hasta el resultado final, no solo el segmento o ángulo inicial. */
export type ConstruccionGeometricaDatos = {
  construccion: 'mediatriz' | 'bisectriz' | 'mediatriz_y_bisectriz';
  // Opcionales: la construcción también puede explicarse sin medidas dadas.
  // Nunca se muestra una medida predeterminada como si fuese del ejemplo.
  longitud?: number;
  unidad?: string;
  grados?: number;
};

/** Sólidos de revolución: dimensiones explícitas del ejemplo. */
export interface SolidoRevolucionDatos {
  forma: 'cilindro' | 'cono' | 'esfera';
  radio: number;
  altura?: number; // Requerida para cono y cilindro; no se usa en esfera.
  unidad: string;
}

/** Dos rectas ax + by = c con una intersección única. */
export interface SistemaLinealDatos {
  a1: number; b1: number; c1: number;
  a2: number; b2: number; c2: number;
  minimoX: number; maximoX: number;
  minimoY: number; maximoY: number;
}

export interface TriangulosCongruentesDatos {

  // Primera versión: criterio Lado-Lado-Lado.
  criterio: 'LLL';

  // Longitudes del triángulo ABC.
  // Correspondencias: AB=DE, BC=EF y AC=DF.
  ladoAB: number;
  ladoBC: number;
  ladoAC: number;

  // Unidad de medida utilizada en la lección.
  unidad: 'mm' | 'cm' | 'm' | 'km';
}

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
      tipo: 'plano_cartesiano';
      titulo: string;
      datos: PlanoCartesianoDatos;
    }
  | {
      tipo: 'grafica_estadistica';
      titulo: string;
      datos: GraficaEstadisticaDatos;
    }
  | {
      tipo: 'grafica_funcion';
      titulo: string;
      datos: GraficaFuncionDatos;
    }
  | {
      tipo: 'modelo_proporcionalidad';
      titulo: string;
      datos: ModeloProporcionalidadDatos;
    }
  | {
      tipo: 'triangulo_rectangulo';
      titulo: string;
      datos: TrianguloRectanguloDatos;
    }
  | {
      tipo: 'modelo_probabilidad';
      titulo: string;
      datos: ModeloProbabilidadDatos;
    }
  | {
      tipo: 'prisma_rectangular';
      titulo: string;
      datos: PrismaRectangularDatos;
    }
  | { tipo: 'segmento_geometrico'; titulo: string; datos: SegmentoGeometricoDatos }
  | { tipo: 'modelo_ecuacion'; titulo: string; datos: ModeloEcuacionDatos }
  | { tipo: 'tabla_valores'; titulo: string; datos: TablaValoresDatos }
  | { tipo: 'mapa_conceptual'; titulo: string; datos: MapaConceptualDatos }
  | { tipo: 'angulo_geometrico'; titulo: string; datos: AnguloGeometricoDatos }
  | { tipo: 'grafica_circular'; titulo: string; datos: GraficaCircularDatos }
  | { tipo: 'construccion_geometrica'; titulo: string; datos: ConstruccionGeometricaDatos }
  | { tipo: 'solido_revolucion'; titulo: string; datos: SolidoRevolucionDatos }
  | { tipo: 'sistema_lineal'; titulo: string; datos: SistemaLinealDatos }
  | {
      tipo: 'triangulos_congruentes';
      titulo: string;
      datos: TriangulosCongruentesDatos;
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
 * Recurso visual personalizado de la lección.
 *
 * Puede ser null cuando la lección no necesita
 * un diagrama o todavía no tiene uno generado.
 *
 * Es opcional para mantener compatibilidad
 * con respuestas anteriores del backend.
 */
  recurso_visual?: RecursoVisualLeccion | null;
  estado_recurso_visual?: 'PENDIENTE' | 'GENERADO' | 'NO_APLICABLE' | 'RECHAZADO';
}
