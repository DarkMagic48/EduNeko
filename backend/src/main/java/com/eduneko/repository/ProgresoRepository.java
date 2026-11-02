package com.eduneko.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class ProgresoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ProgresoRepository(
            NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> obtenerProgresoLeccion(
            Long leccionId,
            Long estudianteId) {

        String sql = """
            SELECT
                COUNT(DISTINCT a.id) AS total_actividades,

                COUNT(DISTINCT CASE
                    WHEN ai.es_correcta = TRUE
                    THEN a.id
                END) AS actividades_correctas,

                COALESCE(
                    SUM(ai.puntos_obtenidos), 0
                ) AS puntos_obtenidos

            FROM actividad a

            LEFT JOIN actividad_intento ai
                ON ai.actividad_id = a.id
                AND ai.estudiante_id = :estudianteId

            WHERE a.leccion_id = :leccionId
            """;

        return jdbc.queryForMap(
                sql,
                Map.of(
                        "leccionId", leccionId,
                        "estudianteId", estudianteId
                )
        );
    }
}
