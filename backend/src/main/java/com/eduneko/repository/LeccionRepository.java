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
            String ejemploResuelto
    ) {

        String sql = """
            INSERT INTO leccion (
                plan_unidad_id,
                titulo,
                introduccion,
                explicacion,
                ejemplo_resuelto,
                orden
            )
            VALUES (
                :unidadId,
                :titulo,
                :introduccion,
                :explicacion,
                :ejemploResuelto,
                1
            )
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
                .addValue("unidadId", unidadId)
                .addValue("titulo", titulo)
                .addValue("introduccion", introduccion)
                .addValue("explicacion", explicacion)
                .addValue("ejemploResuelto", ejemploResuelto);

        return jdbc.queryForObject(sql, params, Long.class);
    }

    public Optional<Map<String, Object>> obtenerLeccion(Long leccionId) {

        String sql = """
            SELECT
                id,
                plan_unidad_id,
                titulo,
                introduccion,
                explicacion,
                ejemplo_resuelto,
                orden,
                estado
            FROM leccion
            WHERE id = :leccionId
            """;

        var params = new MapSqlParameterSource()
                .addValue("leccionId", leccionId);

        List<Map<String, Object>> resultados =
                jdbc.queryForList(sql, params);

        return resultados.stream().findFirst();
    }
}
