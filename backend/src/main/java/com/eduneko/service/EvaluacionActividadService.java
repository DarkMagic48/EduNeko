package com.eduneko.service;

import com.eduneko.repository.ActividadRepository;
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
    private final NamedParameterJdbcTemplate jdbc;

    public EvaluacionActividadService(
            ActividadRepository actividadRepository,
            LeccionService leccionService,
            DiagnosticoService diagnosticoService,
            NamedParameterJdbcTemplate jdbc) {

        this.actividadRepository = actividadRepository;
        this.leccionService = leccionService;
        this.diagnosticoService = diagnosticoService;
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

        // Bloqueo transaccional por estudiante para evitar
        // que dos respuestas simultáneas dupliquen puntos.
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

        // Comprobar si ya recibió puntos por esta actividad.
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

        actividadRepository.registrarIntento(
                actividadId,
                estudianteId,
                respuesta.trim(),
                esCorrecta,
                puntosObtenidos
        );

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

        return resultado;
    }
}
