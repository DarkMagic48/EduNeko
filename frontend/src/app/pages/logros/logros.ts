import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NekoMensaje } from '../../shared/neko-mensaje/neko-mensaje';

import {
  ProgresoService,
  LogroResponse
} from '../../services/progreso.service';

/**
 * Página independiente de logros e insignias de EduNeko.
 *
 * Consulta las insignias del estudiante autenticado utilizando el endpoint GET /api/progreso/logros.
 *
 * Los estados de desbloqueo se calculan en el backend, por lo que Angular solamente presenta la información.
 */
@Component({
  selector: 'app-logros',
  imports: [NekoMensaje],
  templateUrl: './logros.html',
  styleUrl: './logros.css'
})
export class Logros implements OnInit {

  // Insignias obtenidas desde el backend.
  logros: LogroResponse[] = [];

  puntosTotales: number | null = null;

  mensajeFelicitacionNeko = '';

  hitoFelicitacionActual: number | null = null;

  correoEstudiante: string | null = null;

  identidadConsultada = false;

  /**
   * Indica si se están consultando los XP.
   */
  cargandoPuntos = false;

  /**
   * Mensaje de error al consultar los XP.
   */
  errorPuntos = '';

  // Estado de carga de la información.
  cargandoLogros = false;

  // Mensaje en caso de error al consultar las insignias.
  errorLogros = '';

  constructor(
    private readonly progresoService: ProgresoService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}


   // Consultar las insignias al ingresar a la página.
  ngOnInit(): void {
    this.cargarLogros();
    this.cargarEstudianteActual();
    this.cargarPuntosTotales();
  }

 private cargarEstudianteActual(): void {

  this.correoEstudiante = null;
  this.identidadConsultada = false;
  this.mensajeFelicitacionNeko = '';

  this.authService.obtenerUsuarioActual().subscribe({

    next: (usuario) => {

      // Identificar la cuenta autenticada.
      this.correoEstudiante =
        usuario.correo?.trim().toLowerCase() || null;

      // Marcar la consulta como finalizada.
      this.identidadConsultada = true;

      // Evaluar los XP solo si ya están disponibles.
      if (
        this.correoEstudiante &&
        this.puntosTotales !== null &&
        !this.cargandoPuntos
      ) {
        this.detectarHitoXP();
      }
    },

    error: () => {

      // No mostrar felicitaciones sin una
      // identidad de estudiante confirmada.
      this.correoEstudiante = null;
      this.identidadConsultada = true;
      this.mensajeFelicitacionNeko = '';
    }
  });
}

  /**
   * Recupera el catálogo de logros del estudiante.
   *
   * La consulta utiliza la autenticación JWT configurada en la aplicación.
   */
  cargarLogros(): void {

    // Iniciar la carga y limpiar errores anteriores.
    this.cargandoLogros = true;
    this.errorLogros = '';

    this.progresoService.obtenerLogros().subscribe({

      next: (respuesta) => {

        // Guardar las insignias recibidas del backend.
        this.logros = respuesta.logros;

        // Finalizar el estado de carga.
        this.cargandoLogros = false;
      },

      error: () => {

        // Limpiar los datos para evitar información desactualizada.
        this.logros = [];

        this.cargandoLogros = false;

        this.errorLogros =
          'No fue posible consultar tus logros. Inténtalo nuevamente.';
      }
    });
  }

  /**
   * EDUNEKO V1 - CONSULTA DE XP ACUMULADOS
   *
   * Obtiene los puntos reales del estudiante.
   * No modifica los XP ni las insignias.
   */
  cargarPuntosTotales(): void {

    this.cargandoPuntos = true;
    this.errorPuntos = '';
    this.puntosTotales = null;

    this.progresoService.obtenerPuntosTotales().subscribe({

      next: (respuesta) => {

        // Guardar los XP confirmados por el backend.
        this.puntosTotales =
          Number.isFinite(respuesta.puntosTotales)
            ? respuesta.puntosTotales
            : null;

        // Detectar el hito de XP alcanzado.
        this.detectarHitoXP();

        this.cargandoPuntos = false;
      },

      error: () => {

        this.puntosTotales = null;
        this.cargandoPuntos = false;

        this.errorPuntos =
          'No fue posible consultar tus puntos XP.';
      }
    });
  }


  /**
   * EDUNEKO V1 - DETECCIÓN DE HITOS DE XP
   *
   * Identifica el hito más alto alcanzado
   * utilizando los puntos reales del backend.
   *
   * No modifica los puntos ni las insignias.
   */
  private detectarHitoXP(): void {

  // Limpiar cualquier felicitación anterior.
  this.mensajeFelicitacionNeko = '';
  this.hitoFelicitacionActual = null;

  // No evaluar sin identidad y XP confirmados.
  if (
    this.puntosTotales === null ||
    !this.identidadConsultada ||
    !this.correoEstudiante
  ) {
    return;
  }

  const clave = this.obtenerClaveFelicitaciones();

  if (!clave) {
    return;
  }

  // Recuperar los hitos que ya fueron felicitados.
  let hitosVistos: number[] = [];

  try {
    const guardados = localStorage.getItem(clave);

    if (guardados) {
      const datos: unknown = JSON.parse(guardados);

      if (Array.isArray(datos)) {
        hitosVistos = datos.filter(
          (valor): valor is number =>
            typeof valor === 'number' &&
            Number.isFinite(valor)
        );
      }
    }
  } catch {
    // Si el almacenamiento falla, no se muestra
    // una felicitación que no podamos registrar.
    return;
  }

  // Catálogo de hitos de experiencia.
  const hitos = [
    {
      puntos: 500,
      mensaje: '🏆 ¡Impresionante! Alcanzaste 500 XP. ¡Eres una inspiración para Neko!'
    },
    {
      puntos: 250,
      mensaje: '🌟 ¡Extraordinario! Ya tienes 250 XP. ¡Tu dedicación está dando frutos!'
    },
    {
      puntos: 100,
      mensaje: '🎉 ¡Felicidades! Alcanzaste 100 XP. ¡Neko está muy orgulloso de ti!'
    },
    {
      puntos: 50,
      mensaje: '⭐ ¡Muy bien! Alcanzaste tus primeros 50 XP. ¡Sigue aprendiendo!'
    }
  ];

  // Seleccionar el hito más alto que esté pendiente.
  const hitoPendiente = hitos.find(
    hito =>
      this.puntosTotales! >= hito.puntos &&
      !hitosVistos.includes(hito.puntos)
  );

  if (!hitoPendiente) {
    return;
  }

  // Preparar la felicitación para mostrarla.
  this.hitoFelicitacionActual = hitoPendiente.puntos;
  this.mensajeFelicitacionNeko = hitoPendiente.mensaje;
}

cerrarFelicitacionNeko(): void {

  const clave = this.obtenerClaveFelicitaciones();
  const hitoActual = this.hitoFelicitacionActual;

  // No registrar felicitaciones sin identidad
  // o sin un hito válido.
  if (!clave || hitoActual === null) {
    return;
  }

  try {

    // Recuperar el historial existente.
    const guardados = localStorage.getItem(clave);

    const datos: unknown = guardados
      ? JSON.parse(guardados)
      : [];

    const hitosVistos: number[] = Array.isArray(datos)
      ? datos.filter(
          (valor): valor is number =>
            typeof valor === 'number' &&
            Number.isFinite(valor)
        )
      : [];

    // Registrar el hito actual y los anteriores.
    // Así evitamos mostrar felicitaciones atrasadas.
    const hitosActualizados = new Set([
      ...hitosVistos,
      ...[50, 100, 250, 500].filter(
        puntos => puntos <= hitoActual
      )
    ]);

    // Guardar el historial del estudiante.
    localStorage.setItem(
      clave,
      JSON.stringify([...hitosActualizados])
    );

    // Ocultar la felicitación solamente
    // después de guardar correctamente.
    this.mensajeFelicitacionNeko = '';
    this.hitoFelicitacionActual = null;

  } catch {

    // Si falla el almacenamiento, conservar
    // la felicitación para no perderla.
    console.warn(
      'No fue posible guardar la felicitación de Neko.'
    );
  }
}

  private obtenerClaveFelicitaciones(): string | null {

  if (!this.correoEstudiante) {
    return null;
  }

  const bytes = new TextEncoder().encode(
    this.correoEstudiante
  );

  const identificador = Array.from(bytes)
    .map(byte => byte.toString(16).padStart(2, '0'))
    .join('');

    return `eduneko_felicitaciones_xp_${identificador}`;
  }


    obtenerMensajeNeko(): string {

    // Identificar si el estudiante desbloqueó un logro.
    const tieneLogro = (codigo: string): boolean =>
      this.logros.some(
        logro => logro.codigo === codigo && logro.desbloqueado
      );

    // Priorizar el logro de constancia.
    if (tieneLogro('CONSTANCIA')) {
      return '🔥 ¡Tres días aprendiendo! Tu constancia está dando frutos.';
    }

    // Reconocer los primeros 50 puntos XP.
    if (tieneLogro('APRENDIZ_DESTACADO')) {
      return '⭐ ¡Ya tienes 50 XP! Sigue así, estás haciendo un gran trabajo.';
    }

    // Celebrar la primera lección completada.
    if (tieneLogro('PRIMERA_LECCION')) {
      return '📚 ¡Increíble! Terminaste tu primera lección. ¡Vamos por más!';
    }

    // Celebrar el primer acierto.
    if (tieneLogro('PRIMER_ACIERTO')) {
      return '🎯 ¡Muy bien! Has conseguido tu primer acierto.';
    }

    // Mensaje para estudiantes que todavía no tienen logros.
    return '🐱 ¡Bienvenido! Cada gran aventura comienza con un pequeño paso.';
  }

  /**
   * Cuenta las insignias que el estudiante
   * ha desbloqueado según los datos del backend.
   *
   * No modifica los logros ni sus condiciones.
   */
  contarInsigniasDesbloqueadas(): number {
    return this.logros.filter(logro => logro.desbloqueado).length;
  }
  // Regresa al dashboard principal del estudiante.

  volverAlDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}