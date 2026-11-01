package com.eduneko.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ActividadRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ActividadRepository(
            NamedParameterJdbcTemplate jdbc,
            ObjectMapper objectMapper) {

        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public boolean existenActividades(Long leccionId) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM actividad
                WHERE leccion_id = :leccionId
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                Map.of("leccionId", leccionId),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    public List<Map<String, Object>> obtenerActividades(Long leccionId) {

        String sql = """
            SELECT
                id,
                leccion_id,
                pregunta,
                tipo,
                opciones,
                orden
            FROM actividad
            WHERE leccion_id = :leccionId
            ORDER BY orden
            """;

        return jdbc.queryForList(
                sql,
                Map.of("leccionId", leccionId)
        );
    }

    public void guardarActividad(
            Long leccionId,
            String pregunta,
            String tipo,
            JsonNode opciones,
            String respuestaCorrecta,
            String explicacionRespuesta,
            int orden) {

        String sql = """
            INSERT INTO actividad (
                leccion_id,
                pregunta,
                tipo,
                opciones,
                respuesta_correcta,
                explicacion_respuesta,
                orden
            )
            VALUES (
                :leccionId,
                :pregunta,
                :tipo,
                CAST(:opciones AS jsonb),
                :respuestaCorrecta,
                :explicacionRespuesta,
                :orden
            )
            """;

        String opcionesJson = null;

        if (opciones != null && !opciones.isNull()) {
            opcionesJson = opciones.toString();
        }

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("leccionId", leccionId)
                        .addValue("pregunta", pregunta)
                        .addValue("tipo", tipo)
                        .addValue("opciones", opcionesJson)
                        .addValue("respuestaCorrecta", respuestaCorrecta)
                        .addValue("explicacionRespuesta", explicacionRespuesta)
                        .addValue("orden", orden);

        jdbc.update(sql, parametros);
    }

    public java.util.Optional<Map<String, Object>> obtenerActividadParaEvaluar(
        Long actividadId) {

    String sql = """
        SELECT
            id,
            leccion_id,
            pregunta,
            tipo,
            opciones,
            respuesta_correcta,
            explicacion_respuesta
        FROM actividad
        WHERE id = :actividadId
        """;

    List<Map<String, Object>> resultados = jdbc.queryForList(
            sql,
            Map.of("actividadId", actividadId)
    );

        return resultados.stream().findFirst();
    }
    
    public void registrarIntento(
        Long actividadId,
        Long estudianteId,
        String respuestaEstudiante,
        boolean esCorrecta,
        int puntosObtenidos) {

    String sql = """
        INSERT INTO actividad_intento (
            actividad_id,
            estudiante_id,
            respuesta_estudiante,
            es_correcta,
            puntos_obtenidos
        )
        VALUES (
            :actividadId,
            :estudianteId,
            :respuestaEstudiante,
            :esCorrecta,
            :puntosObtenidos
        )
        """;

    MapSqlParameterSource parametros =
            new MapSqlParameterSource()
                    .addValue("actividadId", actividadId)
                    .addValue("estudianteId", estudianteId)
                    .addValue("respuestaEstudiante", respuestaEstudiante)
                    .addValue("esCorrecta", esCorrecta)
                    .addValue("puntosObtenidos", puntosObtenidos);

        jdbc.update(sql, parametros);
    }
}
