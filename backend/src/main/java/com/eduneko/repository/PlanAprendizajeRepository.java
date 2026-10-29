package com.eduneko.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@Repository
public class PlanAprendizajeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PlanAprendizajeRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public Optional<Long> buscarPlanActivo(
            Long estudianteId,
            Long materiaId,
            Short gradoEscolar
    ) {
        String sql = """
            SELECT id
            FROM plan_aprendizaje
            WHERE estudiante_id = :estudianteId
              AND materia_id = :materiaId
              AND grado_escolar = :gradoEscolar
              AND estado = 'ACTIVO'
            """;

        var params = new MapSqlParameterSource()
                .addValue("estudianteId", estudianteId)
                .addValue("materiaId", materiaId)
                .addValue("gradoEscolar", gradoEscolar);

        List<Long> resultados = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> rs.getLong("id")
        );

        return resultados.stream().findFirst();
    }

    public Long crearPlan(
            Long estudianteId,
            Long materiaId,
            Short gradoEscolar,
            Long diagnosticoIntentoId,
            Integer version
    ) {
        String sql = """
            INSERT INTO plan_aprendizaje (
                estudiante_id,
                materia_id,
                grado_escolar,
                diagnostico_intento_id,
                version,
                estado
            )
            VALUES (
                :estudianteId,
                :materiaId,
                :gradoEscolar,
                :diagnosticoIntentoId,
                :version,
                'ACTIVO'
            )
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
                .addValue("estudianteId", estudianteId)
                .addValue("materiaId", materiaId)
                .addValue("gradoEscolar", gradoEscolar)
                .addValue("diagnosticoIntentoId", diagnosticoIntentoId)
                .addValue("version", version);

        return jdbc.queryForObject(sql, params, Long.class);
    }

    public Long crearUnidad(
            Long planId,
            String titulo,
            String descripcion,
            String objetivo,
            String temaDiagnostico,
            Integer orden,
            Integer prioridad
    ) {
        String sql = """
            INSERT INTO plan_unidad (
                plan_aprendizaje_id,
                titulo,
                descripcion,
                objetivo,
                tema_diagnostico,
                orden,
                prioridad,
                estado
            )
            VALUES (
                :planId,
                :titulo,
                :descripcion,
                :objetivo,
                :temaDiagnostico,
                :orden,
                :prioridad,
                'DISPONIBLE'
            )
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
                .addValue("planId", planId)
                .addValue("titulo", titulo)
                .addValue("descripcion", descripcion)
                .addValue("objetivo", objetivo)
                .addValue("temaDiagnostico", temaDiagnostico)
                .addValue("orden", orden)
                .addValue("prioridad", prioridad);

        return jdbc.queryForObject(sql, params, Long.class);
    }

    public void vincularPda(
            Long unidadId,
            Long procesoAprendizajeId
    ) {
        String sql = """
            INSERT INTO plan_unidad_pda (
                plan_unidad_id,
                proceso_aprendizaje_id
            )
            VALUES (
                :unidadId,
                :procesoAprendizajeId
            )
            ON CONFLICT DO NOTHING
            """;

        var params = new MapSqlParameterSource()
                .addValue("unidadId", unidadId)
                .addValue("procesoAprendizajeId", procesoAprendizajeId);

        jdbc.update(sql, params);
    }

    public List<Map<String, Object>> obtenerUnidadesConPda(Long planId) {

        String sql = """
            SELECT
                u.id AS unidad_id,
                u.titulo,
                u.descripcion,
                u.objetivo,
                u.tema_diagnostico,
                u.orden,
                u.prioridad,
                u.estado,
                u.progreso_porcentaje,
                pa.id AS pda_id,
                pa.descripcion AS pda_descripcion,
                pa.orden AS pda_orden
            FROM plan_unidad u
            LEFT JOIN plan_unidad_pda up
                ON up.plan_unidad_id = u.id
            LEFT JOIN proceso_aprendizaje pa
                ON pa.id = up.proceso_aprendizaje_id
            WHERE u.plan_aprendizaje_id = :planId
            ORDER BY u.orden, pa.orden, pa.id
            """;

        var params = new MapSqlParameterSource()
                .addValue("planId", planId);

        return jdbc.queryForList(sql, params);
    }

    public Integer obtenerSiguienteVersion(
            Long estudianteId,
            Long materiaId,
            Short gradoEscolar
    ) {
        String sql = """
            SELECT COALESCE(MAX(version), 0) + 1
            FROM plan_aprendizaje
            WHERE estudiante_id = :estudianteId
            AND materia_id = :materiaId
            AND grado_escolar = :gradoEscolar
            """;

        var params = new MapSqlParameterSource()
                .addValue("estudianteId", estudianteId)
                .addValue("materiaId", materiaId)
                .addValue("gradoEscolar", gradoEscolar);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    public void sustituirPlanActivo(
            Long estudianteId,
            Long materiaId,
            Short gradoEscolar
    ) {
        String sql = """
            UPDATE plan_aprendizaje
            SET estado = 'SUSTITUIDO',
                fecha_actualizacion = CURRENT_TIMESTAMP
            WHERE estudiante_id = :estudianteId
            AND materia_id = :materiaId
            AND grado_escolar = :gradoEscolar
            AND estado = 'ACTIVO'
            """;

        var params = new MapSqlParameterSource()
                .addValue("estudianteId", estudianteId)
                .addValue("materiaId", materiaId)
                .addValue("gradoEscolar", gradoEscolar);

        jdbc.update(sql, params);
    }

    public Map<String, Object> obtenerDatosPlan(Long planId) {
        String sql = """
            SELECT
                id,
                estado,
                version
            FROM plan_aprendizaje
            WHERE id = :planId
            """;

        var params = new MapSqlParameterSource()
                .addValue("planId", planId);

        return jdbc.queryForMap(sql, params);
    }
    
    public boolean tieneProgresoRegistrado(Long planId) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM plan_unidad
                WHERE plan_aprendizaje_id = :planId
                AND (
                    progreso_porcentaje > 0
                    OR estado IN ('EN_PROGRESO', 'COMPLETADA')
                )
            )
            """;

        var params = new MapSqlParameterSource()
                .addValue("planId", planId);

        return Boolean.TRUE.equals(
                jdbc.queryForObject(sql, params, Boolean.class)
        );
    }
    
    public void bloquearEstudiante(Long estudianteId) {

        String sql = """
            SELECT id
            FROM estudiante
            WHERE id = :estudianteId
            FOR UPDATE
            """;

        var params = new MapSqlParameterSource()
                .addValue("estudianteId", estudianteId);

        jdbc.queryForObject(sql, params, Long.class);
    }

}