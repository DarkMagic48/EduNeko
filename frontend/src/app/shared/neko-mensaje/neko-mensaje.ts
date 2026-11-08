import { Component, Input } from '@angular/core';

/**
 * Tipos de mensajes que Neko puede mostrar.
 *
 * motivacion: anima al estudiante a continuar.
 * acierto: reconoce una respuesta correcta.
 * error: acompaña después de una respuesta incorrecta.
 * orientacion: proporciona una pista pedagógica.
 * logro: celebra una recompensa desbloqueada.
 */
export type TipoMensajeNeko =
  | 'motivacion'
  | 'acierto'
  | 'error'
  | 'orientacion'
  | 'logro';

/**
 * Componente reutilizable para mostrar los mensajes de Neko.
 *
 * Puede utilizarse en el dashboard, las lecciones,
 * las actividades, las evaluaciones y los logros.
 *
 * La lógica pedagógica y la selección del mensaje
 * pertenecen a la pantalla o servicio que lo utiliza.
 *
 * Este componente solamente presenta la información.
 */
@Component({
  selector: 'app-neko-mensaje',
  imports: [],
  templateUrl: './neko-mensaje.html',
  styleUrl: './neko-mensaje.css'
})
export class NekoMensaje {

  /**
   * Mensaje que Neko mostrará al estudiante.
   *
   * Puede ser una frase motivacional o una
   * retroalimentación relacionada con una actividad.
   */
  @Input() mensaje: string = '';

  /**
   * Tipo de mensaje.
   *
   * Permitirá cambiar la apariencia de Neko
   * según el contexto educativo.
   */
  @Input() tipo: TipoMensajeNeko = 'motivacion';

  /**
   * Obtiene un título apropiado para cada tipo
   * de intervención de Neko.
   */
  obtenerTitulo(): string {

    switch (this.tipo) {

      case 'acierto':
        return '¡Excelente trabajo!';

      case 'error':
        return '¡Buen intento!';

      case 'orientacion':
        return 'Una pista de Neko';

      case 'logro':
        return '¡Nuevo logro!';

      default:
        return 'Neko dice:';
    }
  }

  /**
   * Selecciona un icono provisional.
   *
   * Posteriormente se sustituirá por la imagen
   * oficial de Neko y sus diferentes expresiones.
   */
  obtenerIcono(): string {

    switch (this.tipo) {

      case 'acierto':
        return '😸';

      case 'error':
        return '🐱';

      case 'orientacion':
        return '😺';

      case 'logro':
        return '😻';

      default:
        return '🐱';
    }
  }

}