package com.eduneko.service;

import com.eduneko.repository.ActividadRepository;
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

    private static final int PUNTOS_POR_ACIERTO = 10;

    private final ActividadRepository actividadRepository;
    private final LeccionService leccionService;
    private final DiagnosticoService diagnosticoService;
    private final ProgresoRepository progresoRepository;
    private final PlanAprendizajeRepository planAprendizajeRepository;
    private final NamedParameterJdbcTemplate jdbc;

    public EvaluacionActividadService(
            ActividadRepository actividadRepository,
            LeccionService leccionService,
            DiagnosticoService diagnosticoService,
            ProgresoRepository progresoRepository,
            PlanAprendizajeRepository planAprendizajeRepository,
            NamedParameterJdbcTemplate jdbc) {

        this.actividadRepository = actividadRepository;
        this.leccionService = leccionService;
        this.diagnosticoService = diagnosticoService;
        this.progresoRepository = progresoRepository;
        this.planAprendizajeRepository = planAprendizajeRepository;
        this.jdbc = jdbc;
    }

    @Transactional
    public Map<String, Object> evaluar(
            Long unidadId,
            Long actividadId,
            String respuesta,
            String correo) {

        if (respuesta == null || respuesta.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes seleccionar una respuesta."
            );
        }

        // Verificar que la unidad pertenece al estudiante.
        Map<String, Object> leccion =
                leccionService.obtenerLeccion(unidadId, correo);

        Long leccionId =
                ((Number) leccion.get("id")).longValue();

        // Recuperar la actividad para evaluar.
        Map<String, Object> actividad =
                actividadRepository
                        .obtenerActividadParaEvaluar(actividadId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontró la actividad."
                        ));

        Long leccionActividadId =
                ((Number) actividad.get("leccion_id")).longValue();

        if (!leccionId.equals(leccionActividadId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La actividad no pertenece a esta lección."
            );
        }

        if (!"OPCION_MULTIPLE".equals(actividad.get("tipo"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de actividad no admitido."
            );
        }

        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        // Bloqueo transaccional para evitar puntos duplicados
        // cuando llegan respuestas simultáneas.
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

        // Verificar si ya obtuvo puntos por esta actividad.
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

        String respuestaCorrecta =
                (String) actividad.get("respuesta_correcta");

        boolean esCorrecta =
                respuestaCorrecta.equals(respuesta.trim());

        int puntosObtenidos =
                esCorrecta && !Boolean.TRUE.equals(puntosOtorgados)
                        ? PUNTOS_POR_ACIERTO
                        : 0;

        // Registrar el intento.
        actividadRepository.registrarIntento(
                actividadId,
                estudianteId,
                respuesta.trim(),
                esCorrecta,
                puntosObtenidos
        );

        // Calcular el progreso actualizado de la lección.
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

        int progresoPorcentaje = totalActividades == 0
                ? 0
                : (int) Math.round(
                        actividadesCorrectas * 100.0 / totalActividades
                );

        boolean completada =
                totalActividades > 0
                && actividadesCorrectas == totalActividades;

        // Sincronizar el progreso con la unidad del plan activo.
        planAprendizajeRepository.actualizarProgresoUnidad(
                unidadId,
                progresoPorcentaje
        );

        // Preparar respuesta para Angular.
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

        // Nuevos datos de progreso.
        resultado.put("totalActividades", totalActividades);
        resultado.put("actividadesCorrectas", actividadesCorrectas);
        resultado.put("progresoPorcentaje", progresoPorcentaje);
        resultado.put("leccionCompletada", completada);

        return resultado;
    }
}