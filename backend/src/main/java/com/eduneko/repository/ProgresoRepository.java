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

        /**
     * Calcula los puntos totales acumulados por un estudiante.
     *
     * Los puntos se obtienen de los intentos registrados
     * en la base de datos, por lo que no es necesario
     * mantener un contador adicional.
     *
     * COALESCE permite devolver cero cuando el estudiante
     * todavía no ha obtenido puntos.
     */
    public int obtenerPuntosTotales(Long estudianteId) {

        String sql = """
            SELECT COALESCE(
                SUM(ai.puntos_obtenidos), 0
            )
            FROM actividad_intento ai
            WHERE ai.estudiante_id = :estudianteId
            """;

        return jdbc.queryForObject(
                sql,
                Map.of("estudianteId", estudianteId),
                Integer.class
        );
    }

    /**
     * Calcula la racha actual y la racha máxima del estudiante.
     *
     * Cada fecha con al menos un intento registrado cuenta
     * como un día de aprendizaje, independientemente del
     * número de actividades o de si fueron correctas.
     *
     * La racha actual se conserva si la última actividad
     * ocurrió hoy o ayer.
     *
     * No se crean registros adicionales: los datos se
     * calculan desde actividad_intento.
     */
    public Map<String, Object> obtenerRachas(Long estudianteId) {

        String sql = """
            WITH dias_actividad AS (
                SELECT DISTINCT
                    fecha_respuesta::date AS fecha
                FROM actividad_intento
                WHERE estudiante_id = :estudianteId
            ),

            dias_agrupados AS (
                SELECT
                    fecha,
                    fecha - (
                        ROW_NUMBER() OVER (ORDER BY fecha)
                    )::integer AS grupo
                FROM dias_actividad
            ),

            rachas AS (
                SELECT
                    MIN(fecha) AS inicio,
                    MAX(fecha) AS fin,
                    COUNT(*)::integer AS dias
                FROM dias_agrupados
                GROUP BY grupo
            )

            SELECT
                COALESCE(
                    MAX(dias) FILTER (
                        WHERE fin >= CURRENT_DATE - 1
                    ), 0
                ) AS racha_actual,

                COALESCE(
                    MAX(dias), 0
                ) AS racha_maxima
            FROM rachas
            """;

        // El identificador del estudiante se envía
        // como parámetro para evitar concatenarlo en SQL.
        return jdbc.queryForMap(
                sql,
                Map.of("estudianteId", estudianteId)
        );
    }

    public boolean tieneLeccionCompletada(Long estudianteId) {

    String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM leccion l
            INNER JOIN plan_unidad pu
                ON l.plan_unidad_id = pu.id
            INNER JOIN plan_aprendizaje pa
                ON pu.plan_aprendizaje_id = pa.id
            WHERE pa.estudiante_id = :estudianteId
              AND l.estado = 'COMPLETADA'
        )
        """;

    // PostgreSQL devuelve true cuando existe
    // al menos una lección completada.
    Boolean resultado = jdbc.queryForObject(
        sql,
        Map.of("estudianteId", estudianteId),
        Boolean.class
    );

    // Devolver false si no se obtiene un resultado.
        return Boolean.TRUE.equals(resultado);
    }

    /**
     * Este metodo permite desbloquear el logro "Primer Acierto".
     */
    public boolean tienePrimerAcierto(Long estudianteId) {

    String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM actividad_intento ai
            WHERE ai.estudiante_id = :estudianteId
              AND ai.puntos_obtenidos > 0
        )
        """;

    // Consultar si existe al menos un intento
    // que haya generado puntos por una respuesta correcta.
    Boolean resultado = jdbc.queryForObject(
        sql,
        Map.of("estudianteId", estudianteId),
        Boolean.class
    );

    // Si no se obtiene un resultado, devolver false.
        return Boolean.TRUE.equals(resultado);
    }
}
