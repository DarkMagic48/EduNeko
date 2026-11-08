package com.eduneko.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class LeccionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public LeccionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> buscarLeccionPorUnidad(Long unidadId) {

        String sql = """
            SELECT id
            FROM leccion
            WHERE plan_unidad_id = :unidadId
            ORDER BY orden
            LIMIT 1
            """;

        var params = new MapSqlParameterSource()
                .addValue("unidadId", unidadId);

        List<Long> resultados = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> rs.getLong("id")
        );

        return resultados.stream().findFirst();
    }

    public Long crearLeccion(
            Long unidadId,
            String titulo,
            String introduccion,
            String explicacion,
            String ejemploResuelto,
            String recursoVisualJson
    ) {

        String sql = """
            INSERT INTO leccion (
                plan_unidad_id,
                titulo,
                introduccion,
                explicacion,
                ejemplo_resuelto,
                recurso_visual,
                estado_recurso_visual,
                orden
            )
            VALUES (
                :unidadId,
                :titulo,
                :introduccion,
                :explicacion,
                :ejemploResuelto,
                CAST(:recursoVisualJson AS jsonb),

                -- Si la lección incluye un recurso visual,
                -- se registra como GENERADO.
                -- Si no incluye recurso, queda PENDIENTE
                -- para permitir enriquecimiento posterior.
                CASE
                    WHEN CAST(:recursoVisualJson AS jsonb) IS NOT NULL
                        THEN 'GENERADO'
                    ELSE 'PENDIENTE'
                END,

                1
            )
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
                .addValue("unidadId", unidadId)
                .addValue("titulo", titulo)
                .addValue("introduccion", introduccion)
                .addValue("explicacion", explicacion)
                .addValue("ejemploResuelto", ejemploResuelto)
                .addValue("recursoVisualJson", recursoVisualJson);

        return jdbc.queryForObject(sql, params, Long.class);
    }

    /**
     * Recupera una lección existente junto con el contexto
     * curricular disponible en su unidad de aprendizaje.
     *
     * Este método no modifica la base de datos.
     */
    public Optional<Map<String, Object>> obtenerLeccion(Long leccionId) {

        String sql = """
        SELECT
            l.id,
            l.plan_unidad_id,
            l.titulo,
            l.introduccion,
            l.explicacion,
            l.ejemplo_resuelto,
            l.orden,
            l.estado,
            l.estado_recurso_visual,
            l.recurso_visual::text AS recurso_visual,

            -- Contexto de la unidad.
            pu.titulo AS titulo_unidad,
            pu.descripcion AS descripcion_unidad,
            pu.objetivo AS objetivo_unidad,
            pu.tema_diagnostico AS tema_diagnostico,

            -- Grado escolar del plan al que pertenece la lección.
            p.grado_escolar AS grado_escolar,

            --Agrupa los PDA asociados a la unidad en una sola cadena.
            --Evita duplicar la fila de la leccion.
            (
                SELECT STRING_AGG(
                    pa.descripcion,
                ' | ' ORDER BY pa.orden, pa.id
                )
                FROM plan_unidad_pda up
                INNER JOIN proceso_aprendizaje pa
                    ON pa.id = up.proceso_aprendizaje_id
                WHERE up.plan_unidad_id = pu.id
                ) AS pda_descripciones

        FROM leccion l

        INNER JOIN plan_unidad pu
            ON pu.id = l.plan_unidad_id

        INNER JOIN plan_aprendizaje p
            ON p.id = pu.plan_aprendizaje_id

        WHERE l.id = :leccionId
        """;

        var params = new MapSqlParameterSource()
                .addValue("leccionId", leccionId);

        List<Map<String, Object>> resultados =
                jdbc.queryForList(sql, params);

        return resultados.stream().findFirst();
    }

    /**
     * Actualiza el estado de una lección según el porcentaje
     * de actividades respondidas correctamente.
     *
     * Reglas:
     * - 0 %: conserva el estado actual.
     * - Entre 1 % y 99 %: EN_PROGRESO.
     * - 100 %: COMPLETADA.
     *
     * Solo actualiza lecciones pertenecientes a un plan activo.
     * No modifica el contenido educativo generado por IA.
     */
    public void actualizarEstadoPorProgreso(
            Long leccionId,
            int progresoPorcentaje
    ) {

        String sql = """
            UPDATE leccion
            SET estado = CASE
                WHEN :progresoPorcentaje = 100 THEN 'COMPLETADA'
                WHEN :progresoPorcentaje > 0 THEN 'EN_PROGRESO'
                ELSE estado
            END
            WHERE id = :leccionId
            AND EXISTS (
                SELECT 1
                FROM plan_unidad u
                JOIN plan_aprendizaje p
                    ON p.id = u.plan_aprendizaje_id
                WHERE u.id = leccion.plan_unidad_id
                    AND p.estado = 'ACTIVO'
            )
            """;

        // Parámetros utilizados para identificar la lección
        // y determinar su nuevo estado.
        var params = new MapSqlParameterSource()
                .addValue("leccionId", leccionId)
                .addValue("progresoPorcentaje", progresoPorcentaje);

        // Ejecuta la actualización en PostgreSQL.
        jdbc.update(sql, params);
    }

    /**
     * Guarda un recurso visual en una lección existente.
     *
     * Solo actualiza la columna recurso_visual.
     * No modifica el contenido educativo, las actividades,
     * el estado ni el progreso de la lección.
     *
     * La condición recurso_visual IS NULL evita sobrescribir
     * un diagrama que ya haya sido almacenado.
     *
     * @param leccionId Identificador de la lección.
     * @param recursoVisualJson Recurso visual validado en formato JSON.
     * @return Número de filas actualizadas (0 o 1).
     */
    public int guardarRecursoVisual(
            Long leccionId,
            String recursoVisualJson
    ) {

        String sql = """
            UPDATE leccion
            SET
                -- Guarda el recurso visual validado.
                recurso_visual = CAST(:recursoVisual AS jsonb),

                -- Actualiza el estado en la misma operación.
                estado_recurso_visual = 'GENERADO'

            WHERE id = :leccionId

            -- No sobrescribe un recurso existente.
            AND recurso_visual IS NULL

            -- Solo guarda recursos pendientes.
            AND estado_recurso_visual = 'PENDIENTE'
            """;

        var params = new MapSqlParameterSource()
                .addValue("leccionId", leccionId)
                .addValue("recursoVisual", recursoVisualJson);

        return jdbc.update(sql, params);
    }

    /**
     * Actualiza el estado del enriquecimiento visual
     * de una lección existente.
     *
     * Estados permitidos:
     * - PENDIENTE
     * - GENERADO
     * - NO_APLICABLE
     * - RECHAZADO
     *
     * No modifica el contenido educativo, las actividades,
     * el progreso ni el recurso visual almacenado.
     *
     * @param leccionId Identificador de la lección.
     * @param estado Nuevo estado del enriquecimiento.
     * @return Número de filas actualizadas.
     */
    public int actualizarEstadoRecursoVisual(
            Long leccionId,
            String estado
    ) {

        String sql = """
            UPDATE leccion
            SET estado_recurso_visual = :estado
            WHERE id = :leccionId

            -- Solo cambia estados pendientes.
            AND estado_recurso_visual = 'PENDIENTE'

            -- No modifica lecciones que ya tienen
            -- un recurso visual almacenado.
            AND recurso_visual IS NULL
            """;

        var params = new MapSqlParameterSource()
                .addValue("leccionId", leccionId)
                .addValue("estado", estado);

        return jdbc.update(sql, params);
    }
    
}
