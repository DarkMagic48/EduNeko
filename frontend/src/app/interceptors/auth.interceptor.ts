
import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';

import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.obtenerToken();

  // =====================================================
  // 1. ADAPTACIÓN TEMPORAL PARA PRUEBAS EN RED LOCAL
  // =====================================================

  // Dirección que utilizan actualmente los servicios.
  const prefijoLocal = 'http://localhost:8080/api/';

  // Detectar automáticamente desde dónde se abrió Angular.
  // Computadora: localhost
  // Celular: 192.168.1.70
  
/* =====================================================
   EDUNEKO V1 - SELECCIÓN AUTOMÁTICA DE ENTORNO
   ===================================================== */

  // Angular ejecutándose en el puerto de desarrollo.
  const esEntornoLocal = window.location.port === '4200';

  // LOCAL:
  // Utiliza Spring Boot en la misma computadora.
  // Funciona desde localhost y desde el celular en la red Wi-Fi.
  //
  // PRODUCCIÓN:
  // Utiliza /api/ en el dominio público.
  // El hosting redirigirá estas peticiones al backend.
  const prefijoBackend = esEntornoLocal
    ? `http://${window.location.hostname}:8080/api/`
    : '/api/';

  // Modificar únicamente las solicitudes de EduNeko.
  const esApiEduNeko = req.url.startsWith(prefijoLocal);

  const urlAdaptada = esApiEduNeko
    ? prefijoBackend + req.url.slice(prefijoLocal.length)
    : req.url;

  const solicitudAdaptada = esApiEduNeko
    ? req.clone({ url: urlAdaptada })
    : req;

  // =====================================================
  // 2. IDENTIFICAR ENDPOINTS PÚBLICOS
  // =====================================================

  const rutaApi = esApiEduNeko
    ? req.url.slice(prefijoLocal.length)
    : '';

  const esAccesoPublico =
    /^auth\/(login|register)(\?|$)/.test(rutaApi);

  // =====================================================
  // 3. AGREGAR JWT A SOLICITUDES PROTEGIDAS
  // =====================================================

  // Nunca enviar el JWT a servidores externos
  // ni a los endpoints públicos de autenticación.
  const request =
    esApiEduNeko && !esAccesoPublico && token
      ? solicitudAdaptada.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        })
      : solicitudAdaptada;

  // =====================================================
  // 4. MANEJAR SESIONES VENCIDAS
  // =====================================================

  return next(request).pipe(
    catchError((error: unknown) => {

      // No confundir credenciales incorrectas
      // con una sesión previamente vencida.
      if (
        esApiEduNeko &&
        !esAccesoPublico &&
        token &&
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        authService.obtenerToken() === token
      ) {
        authService.logout();
        void router.navigate(['/login']);
      }

      return throwError(() => error);
    })
  );
};
