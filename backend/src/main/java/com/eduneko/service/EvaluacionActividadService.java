package com.eduneko.service;

import com.eduneko.repository.ActividadRepository;
import com.eduneko.repository.LeccionRepository;
import com.eduneko.repository.PlanAprendizajeRepository;
import com.eduneko.repository.ProgresoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class EvaluacionActividadService {

    // Puntos que puede obtener un estudiante por cada pregunta correcta.
    private static final int PUNTOS_POR_ACIERTO = 10;

    // Repositorios y servicios utilizados durante la evaluación.
    private final ActividadRepository actividadRepository;
    private final LeccionRepository leccionRepository;
    private final LeccionService leccionService;
    private final DiagnosticoService diagnosticoService;
    private final ProgresoRepository progresoRepository;
    private final PlanAprendizajeRepository planAprendizajeRepository;
    private final NamedParameterJdbcTemplate jdbc;

    public EvaluacionActividadService(
            ActividadRepository actividadRepository,
            LeccionRepository leccionRepository,
            LeccionService leccionService,
            DiagnosticoService diagnosticoService,
            ProgresoRepository progresoRepository,
            PlanAprendizajeRepository planAprendizajeRepository,
            NamedParameterJdbcTemplate jdbc) {

        this.actividadRepository = actividadRepository;
        this.leccionRepository = leccionRepository;
        this.leccionService = leccionService;
        this.diagnosticoService = diagnosticoService;
        this.progresoRepository = progresoRepository;
        this.planAprendizajeRepository = planAprendizajeRepository;
        this.jdbc = jdbc;
    }

    /**
     * Evalúa la respuesta de un estudiante.
     *
     * Todas las operaciones de escritura se realizan dentro de
     * una misma transacción para mantener la consistencia entre:
     * - Intentos registrados.
     * - Puntos obtenidos.
     * - Progreso de la unidad.
     * - Estado de la lección.
     */
    @Transactional
    public Map<String, Object> evaluar(
            Long unidadId,
            Long actividadId,
            String respuesta,
            String correo) {

        // 1. Validar que el estudiante haya enviado una respuesta.
        if (respuesta == null || respuesta.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes seleccionar una respuesta."
            );
        }

        // 2. Verificar que la unidad pertenece al estudiante.
        Map<String, Object> leccion =
                leccionService.obtenerLeccion(unidadId, correo);

        Long leccionId =
                ((Number) leccion.get("id")).longValue();

        // 3. Recuperar la actividad que se va a evaluar.
        Map<String, Object> actividad =
                actividadRepository
                        .obtenerActividadParaEvaluar(actividadId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontró la actividad."
                        ));

        Long leccionActividadId =
                ((Number) actividad.get("leccion_id")).longValue();

        // Evitar que se evalúen actividades de otras lecciones.
        if (!leccionId.equals(leccionActividadId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La actividad no pertenece a esta lección."
            );
        }

        // Actualmente el MVP admite preguntas de opción múltiple.
        if (!"OPCION_MULTIPLE".equals(actividad.get("tipo"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de actividad no admitido."
            );
        }

        // 4. Obtener el identificador del estudiante autenticado.
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        // Bloquear el registro del estudiante durante la evaluación.
        // Esto evita otorgar puntos duplicados ante solicitudes simultáneas.
        jdbc.queryForObject(
                """
                SELECT id
                FROM estudiante
                WHERE id = :estudianteId
                FOR UPDATE
                """,
                Map.of("estudianteId", estudianteId),
                Long.class
        );

        // 5. Comprobar si el estudiante ya obtuvo puntos
        // por responder correctamente esta misma pregunta.
        Boolean puntosOtorgados = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM actividad_intento
                    WHERE actividad_id = :actividadId
                      AND estudiante_id = :estudianteId
                      AND puntos_obtenidos > 0
                )
                """,
                Map.of(
                        "actividadId", actividadId,
                        "estudianteId", estudianteId
                ),
                Boolean.class
        );

        // 6. Comparar la respuesta enviada con la respuesta correcta.
        String respuestaCorrecta =
                (String) actividad.get("respuesta_correcta");

        boolean esCorrecta =
                respuestaCorrecta.equals(respuesta.trim());

        // Otorgar 10 puntos solamente si la respuesta es correcta
        // y todavía no se habían otorgado puntos por esta pregunta.
        int puntosObtenidos =
                esCorrecta && !Boolean.TRUE.equals(puntosOtorgados)
                        ? PUNTOS_POR_ACIERTO
                        : 0;

        // 7. Registrar el intento, sea correcto o incorrecto.
        actividadRepository.registrarIntento(
                actividadId,
                estudianteId,
                respuesta.trim(),
                esCorrecta,
                puntosObtenidos
        );

        // 8. Consultar el progreso acumulado de la lección.
        Map<String, Object> datosProgreso =
                progresoRepository.obtenerProgresoLeccion(
                        leccionId,
                        estudianteId
                );

        int totalActividades =
                ((Number) datosProgreso.get("total_actividades"))
                        .intValue();

        int actividadesCorrectas =
                ((Number) datosProgreso.get("actividades_correctas"))
                        .intValue();

        // Calcular el porcentaje de respuestas correctas.
        // Ejemplo: 3 de 5 preguntas = 60 %.
        int progresoPorcentaje = totalActividades == 0
                ? 0
                : (int) Math.round(
                        actividadesCorrectas * 100.0 / totalActividades
                );

        // Una lección se considera completada cuando todas
        // sus preguntas tienen al menos una respuesta correcta.
        boolean completada =
                totalActividades > 0
                && actividadesCorrectas == totalActividades;

        // 9. Sincronizar el porcentaje y estado de la unidad
        // dentro del plan de aprendizaje activo.
        planAprendizajeRepository.actualizarProgresoUnidad(
                unidadId,
                progresoPorcentaje
        );

        // 10. Sincronizar también el estado de la lección.
        //
        // 0 %: conserva su estado.
        // 1 % a 99 %: EN_PROGRESO.
        // 100 %: COMPLETADA.
        //
        // Ambas actualizaciones forman parte de la misma
        // transacción que registra la respuesta del estudiante.
        leccionRepository.actualizarEstadoPorProgreso(
                leccionId,
                progresoPorcentaje
        );

        // 11. Preparar la respuesta que recibirá Angular.
        Map<String, Object> resultado = new HashMap<>();

        resultado.put("actividadId", actividadId);
        resultado.put("esCorrecta", esCorrecta);
        resultado.put("respuestaCorrecta", respuestaCorrecta);

        resultado.put(
                "explicacion",
                actividad.get("explicacion_respuesta")
        );

        resultado.put("puntosObtenidos", puntosObtenidos);

        resultado.put(
                "puntosYaOtorgados",
                Boolean.TRUE.equals(puntosOtorgados)
        );

        // Información de progreso para actualizar la interfaz.
        resultado.put("totalActividades", totalActividades);
        resultado.put("actividadesCorrectas", actividadesCorrectas);
        resultado.put("progresoPorcentaje", progresoPorcentaje);
        resultado.put("leccionCompletada", completada);

        return resultado;
    }
}
