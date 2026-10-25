package com.eduneko.repository;

import com.eduneko.dto.ContenidoCurricularResponse;
import com.eduneko.dto.ProcesoAprendizajeResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DiagnosticoCurricularRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DiagnosticoCurricularRepository(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ContenidoCurricularResponse> obtenerContenidosPorTema(
            Integer gradoEscolar,
            String temaDiagnostico
    ) {

        String sql = """
                SELECT
                    cc.id,
                    cc.nombre,
                    cc.orden
                FROM diagnostico_tema_contenido dtc
                JOIN materia m
                    ON m.id = dtc.materia_id
                JOIN contenido_curricular_grado ccg
                    ON ccg.id = dtc.contenido_curricular_grado_id
                JOIN contenido_curricular cc
                    ON cc.id = ccg.contenido_curricular_id
                WHERE m.nombre = 'Matemáticas'
                  AND dtc.grado_escolar = :gradoEscolar
                  AND dtc.tema_diagnostico = :temaDiagnostico
                ORDER BY cc.orden
                """;

        var parametros = new MapSqlParameterSource()
                .addValue("gradoEscolar", gradoEscolar)
                .addValue("temaDiagnostico", temaDiagnostico);

        List<ContenidoCurricularResponse> contenidos = jdbcTemplate.query(
        sql,
        parametros,
        (rs, rowNum) -> new ContenidoCurricularResponse(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getInt("orden"),
                List.of()
        )
    );

    return contenidos.stream()
        .map(contenido -> new ContenidoCurricularResponse(
                contenido.id(),
                contenido.nombre(),
                contenido.orden(),
                obtenerPdaPorContenido(
                        contenido.id(),
                        gradoEscolar
                )
        ))
        .toList();
    }

    public List<ProcesoAprendizajeResponse> obtenerPdaPorContenido(
        Long contenidoCurricularId,
        Integer gradoEscolar
    ) {

        String sql = """
                SELECT
                    pa.id,
                    pa.descripcion,
                    pa.orden
                FROM proceso_aprendizaje pa
                JOIN contenido_curricular_grado ccg
                    ON ccg.id = pa.contenido_curricular_grado_id
                WHERE ccg.contenido_curricular_id = :contenidoCurricularId
                AND ccg.grado_escolar = :gradoEscolar
                ORDER BY pa.orden
                """;

        var parametros = new MapSqlParameterSource()
                .addValue("contenidoCurricularId", contenidoCurricularId)
                .addValue("gradoEscolar", gradoEscolar);

        return jdbcTemplate.query(
                sql,
                parametros,
                (rs, rowNum) -> new ProcesoAprendizajeResponse(
                        rs.getLong("id"),
                        rs.getString("descripcion"),
                        rs.getInt("orden")
                )
        );
    }
}