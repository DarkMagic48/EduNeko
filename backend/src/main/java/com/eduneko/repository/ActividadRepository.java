package com.eduneko.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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

        // Consultar las actividades asociadas a la lección.
        // Convertimos el JSONB a texto para obtener su
        // representación JSON original desde PostgreSQL.
        String sql = """
            SELECT
                id,
                leccion_id,
                pregunta,
                tipo,
                opciones::text AS opciones,
                orden
            FROM actividad
            WHERE leccion_id = :leccionId
            ORDER BY orden
            """;

        List<Map<String, Object>> actividades = jdbc.queryForList(
                sql,
                Map.of("leccionId", leccionId)
        );

        // Convertir las opciones de JSON a un arreglo de Java
        // antes de enviar la respuesta al frontend.
        for (Map<String, Object> actividad : actividades) {

            Object opcionesJson = actividad.get("opciones");

            // Las actividades sin opciones conservan el valor null.
            if (opcionesJson == null) {
                continue;
            }

            try {

                // Leer el JSON almacenado en PostgreSQL.
                JsonNode opcionesNode = objectMapper.readTree(
                        opcionesJson.toString()
                );

                // Verificar que las opciones sean un arreglo.
                if (!opcionesNode.isArray()) {
                    throw new IllegalStateException(
                            "Las opciones de la actividad "
                            + actividad.get("id")
                            + " no tienen formato de arreglo."
                    );
                }

                // Crear una lista de respuestas para Angular.
                List<String> opciones = new ArrayList<>();

                for (JsonNode opcion : opcionesNode) {

                    // Cada opción debe ser una cadena de texto.
                    if (!opcion.isTextual()) {
                        throw new IllegalStateException(
                                "La actividad "
                                + actividad.get("id")
                                + " contiene una opción inválida."
                        );
                    }

                    opciones.add(opcion.asText());
                }

                // Sustituir el texto JSON por una lista real.
                // Jackson serializará esta lista como un arreglo JSON.
                actividad.put("opciones", opciones);

            } catch (JsonProcessingException e) {

                // Evitar devolver datos incorrectos al frontend.
                throw new IllegalStateException(
                        "No fue posible interpretar las opciones "
                        + "de la actividad " + actividad.get("id"),
                        e
                );
            }
        }

        return actividades;
    }

    public void guardarActividad(
            Long leccionId,
            String pregunta,
            String tipo,
            JsonNode opciones,
            String respuestaCorrecta,
            String explicacionRespuesta,
            String pistaPedagogica,
            int orden) {
        
    String sql = """
        INSERT INTO actividad (
            leccion_id,
            pregunta,
            tipo,
            opciones,
            respuesta_correcta,
            explicacion_respuesta,
            pista_pedagogica,
            orden
        )
        VALUES (
            :leccionId,
            :pregunta,
            :tipo,
            CAST(:opciones AS jsonb),
            :respuestaCorrecta,
            :explicacionRespuesta,
            :pistaPedagogica,
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
                        .addValue("pistaPedagogica", pistaPedagogica)
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
            explicacion_respuesta,
            pista_pedagogica
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
    
    /**
     * Recupera los intentos anteriores de un estudiante
     * para todas las preguntas de una lección.
     *
     * Prioriza una respuesta correcta ya registrada.
     * Si no existe, recupera el intento más reciente.
     *
     * No consulta ni expone la respuesta correcta oficial
     * almacenada en la tabla actividad.
     */
    public Map<Long, Map<String, Object>> obtenerRespuestasGuardadas(
            Long leccionId,
            Long estudianteId) {

        String sql = """
            SELECT
                a.id AS actividad_id,
                intento.respuesta_estudiante,
                COALESCE(intento.es_correcta, FALSE)
                    AS respondida_correctamente,
                COALESCE(puntos.puntos_obtenidos, 0)
                    AS puntos_obtenidos
            FROM actividad a

            -- Recuperar una respuesta correcta si existe;
            -- de lo contrario, el último intento realizado.
            LEFT JOIN LATERAL (
                SELECT
                    ai.respuesta_estudiante,
                    ai.es_correcta
                FROM actividad_intento ai
                WHERE ai.actividad_id = a.id
                AND ai.estudiante_id = :estudianteId
                ORDER BY
                    ai.es_correcta DESC,
                    ai.fecha_respuesta DESC,
                    ai.id DESC
                LIMIT 1
            ) intento ON TRUE

            -- Sumar los puntos previamente otorgados
            -- por cada pregunta, sin generar puntos nuevos.
            LEFT JOIN LATERAL (
                SELECT
                    COALESCE(SUM(ai.puntos_obtenidos), 0)
                        AS puntos_obtenidos
                FROM actividad_intento ai
                WHERE ai.actividad_id = a.id
                AND ai.estudiante_id = :estudianteId
            ) puntos ON TRUE

            WHERE a.leccion_id = :leccionId
            ORDER BY a.orden
            """;

        var parametros = new MapSqlParameterSource()
                .addValue("leccionId", leccionId)
                .addValue("estudianteId", estudianteId);

        List<Map<String, Object>> filas =
                jdbc.queryForList(sql, parametros);

        // Organizar los resultados por identificador de pregunta
        // para facilitar su incorporación en la respuesta de la API.
        Map<Long, Map<String, Object>> respuestas =
                new java.util.HashMap<>();

        for (Map<String, Object> fila : filas) {

            Long actividadId =
                    ((Number) fila.get("actividad_id")).longValue();

            Map<String, Object> datos = new java.util.HashMap<>();

            datos.put(
                    "respuestaEstudiante",
                    fila.get("respuesta_estudiante")
            );

            datos.put(
                    "respondidaCorrectamente",
                    Boolean.TRUE.equals(
                            fila.get("respondida_correctamente")
                    )
            );

            datos.put(
                    "puntosObtenidos",
                    ((Number) fila.get("puntos_obtenidos")).intValue()
            );

            respuestas.put(actividadId, datos);
        }

        return respuestas;
    }

        public Map<String, Object> obtenerDesempenoAnterior(
            Long unidadId,
            Long estudianteId) {

        String sql = """
            WITH unidad_actual AS (
                SELECT
                    pu.plan_aprendizaje_id
                FROM plan_unidad pu
                INNER JOIN plan_aprendizaje p
                    ON p.id = pu.plan_aprendizaje_id
                WHERE pu.id = :unidadId
                AND p.estudiante_id = :estudianteId
                AND p.estado = 'ACTIVO'
            ),
            primeros_intentos AS (
                SELECT DISTINCT ON (ai.actividad_id)
                    ai.actividad_id,
                    ai.es_correcta
                FROM actividad_intento ai
                INNER JOIN actividad a
                    ON a.id = ai.actividad_id
                INNER JOIN leccion l
                    ON l.id = a.leccion_id
                INNER JOIN plan_unidad pu
                    ON pu.id = l.plan_unidad_id
                INNER JOIN unidad_actual ua
                    ON ua.plan_aprendizaje_id =
                        pu.plan_aprendizaje_id
                WHERE ai.estudiante_id = :estudianteId
                AND pu.id <> :unidadId
                ORDER BY
                    ai.actividad_id,
                    ai.fecha_respuesta ASC,
                    ai.id ASC
            )
            SELECT
                COUNT(*) AS total_respondidas,
                COUNT(*) FILTER (
                    WHERE es_correcta = TRUE
                ) AS aciertos_iniciales
            FROM primeros_intentos
            """;

        var parametros = new MapSqlParameterSource()
                .addValue("unidadId", unidadId)
                .addValue("estudianteId", estudianteId);

        // Recupera los datos agregados del desempeño anterior.
        Map<String, Object> resultado =
                jdbc.queryForMap(sql, parametros);

        long total = ((Number) resultado.get(
                "total_respondidas")).longValue();

        long aciertos = ((Number) resultado.get(
                "aciertos_iniciales")).longValue();

        // Si todavía no hay respuestas, no se calcula
        // un porcentaje que pudiera interpretarse
        // erróneamente como bajo desempeño.
        Double porcentaje = total == 0
                ? null
                : Math.round(aciertos * 10000.0 / total) / 100.0;

        return Map.of(
                "totalRespondidas", total,
                "aciertosIniciales", aciertos,
                "porcentajeAciertosIniciales",
                porcentaje == null ? -1.0 : porcentaje
        );
    }
}