package com.eduneko.service;

import com.eduneko.repository.LeccionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

@Service
public class LeccionService {

    private final LeccionRepository leccionRepository;
    private final DiagnosticoService diagnosticoService;
    private final NamedParameterJdbcTemplate jdbc;
    private final GeneradorContenidoIAService generadorContenidoIAService;
    private final PersonalizacionPedagogicaService personalizacionPedagogicaService;
    private final ObjectMapper objectMapper;
    private final ValidadorContenidoIAService validadorContenidoIAService;

    public LeccionService(
            LeccionRepository leccionRepository,
            DiagnosticoService diagnosticoService,
            NamedParameterJdbcTemplate jdbc,
            GeneradorContenidoIAService generadorContenidoIAService,
            PersonalizacionPedagogicaService personalizacionPedagogicaService,
            ObjectMapper objectMapper,
            ValidadorContenidoIAService validadorContenidoIAService
    ) {
        this.leccionRepository = leccionRepository;
        this.diagnosticoService = diagnosticoService;
        this.jdbc = jdbc;
        this.generadorContenidoIAService = generadorContenidoIAService;
        this.personalizacionPedagogicaService = personalizacionPedagogicaService;
        this.objectMapper = objectMapper;
        this.validadorContenidoIAService = validadorContenidoIAService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerLeccion(
            Long unidadId,
            String correo
    ) {
        validarAccesoUnidad(unidadId, correo);

        Long leccionId = leccionRepository
                .buscarLeccionPorUnidad(unidadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Esta unidad todavía no tiene una lección"
                ));

        return leccionRepository.obtenerLeccion(leccionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la lección"
                ));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerContextoUnidad(
            Long unidadId,
            String correo
    ) {
        validarAccesoUnidad(unidadId, correo);
  
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        String sqlPreferencias = """
            SELECT
                nivel_explicacion,
                nivel_reto_preferido,
                ritmo_estudio,
                prefiere_lectura_escritura,
                prefiere_visual,
                prefiere_auditivo,
                prefiere_practica
            FROM preferencia_aprendizaje
            WHERE estudiante_id = :estudianteId
            """;

        var parametrosPreferencias = new MapSqlParameterSource()
                .addValue("estudianteId", estudianteId);

        List<Map<String, Object>> resultadosPreferencias =
                jdbc.queryForList(
                        sqlPreferencias,
                        parametrosPreferencias
                );

        if (resultadosPreferencias.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El estudiante no tiene preferencias de aprendizaje registradas"
            );
        }

        Map<String, Object> preferencias =
                resultadosPreferencias.get(0);

        String sql = """
            SELECT
                u.id,
                u.titulo,
                u.objetivo,
                u.prioridad,
                p.grado_escolar,
                pa.descripcion AS pda_descripcion
            FROM plan_unidad u
            JOIN plan_aprendizaje p
              ON p.id = u.plan_aprendizaje_id
            LEFT JOIN plan_unidad_pda up
              ON up.plan_unidad_id = u.id
            LEFT JOIN proceso_aprendizaje pa
              ON pa.id = up.proceso_aprendizaje_id
            WHERE u.id = :unidadId
            ORDER BY pa.orden, pa.id
            """;

        var params = new MapSqlParameterSource()
                .addValue("unidadId", unidadId);

        List<Map<String, Object>> filas =
                jdbc.queryForList(sql, params);

        if (filas.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró la unidad"
            );
        }

        Map<String, Object> primeraFila = filas.get(0);

        List<String> pdas = filas.stream()
                .map(fila -> (String) fila.get("pda_descripcion"))
                .filter(descripcion -> descripcion != null)
                .toList();

        return Map.of(
                "unidadId", unidadId,
                "titulo", primeraFila.get("titulo"),
                "objetivo", primeraFila.get("objetivo"),
                "prioridad", primeraFila.get("prioridad"),
                "gradoEscolar", primeraFila.get("grado_escolar"),
                "procesosAprendizaje", pdas,
                "preferenciasAprendizaje", preferencias
        );
    }

    private void validarAccesoUnidad(
            Long unidadId,
            String correo
    ) {
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM plan_unidad u
                JOIN plan_aprendizaje p
                  ON p.id = u.plan_aprendizaje_id
                WHERE u.id = :unidadId
                  AND p.estudiante_id = :estudianteId
                  AND p.estado = 'ACTIVO'
            )
            """;

        var params = new MapSqlParameterSource()
                .addValue("unidadId", unidadId)
                .addValue("estudianteId", estudianteId);

        Boolean permitido =
                jdbc.queryForObject(sql, params, Boolean.class);

        if (!Boolean.TRUE.equals(permitido)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró una unidad activa para este estudiante"
            );
        }
    }
        @Transactional
        public Map<String, Object> generarLeccion(
                Long unidadId,
                String correo) {

        validarAccesoUnidad(unidadId, correo);

        var leccionExistente =
                leccionRepository.buscarLeccionPorUnidad(unidadId);

        if (leccionExistente.isPresent()) {
                return leccionRepository
                        .obtenerLeccion(leccionExistente.get())
                        .orElseThrow();
        }

        Map<String, Object> contexto =
                obtenerContextoUnidad(unidadId, correo);

        String instrucciones =
                personalizacionPedagogicaService
                        .construirInstrucciones();

        String contenido =
                personalizacionPedagogicaService
                        .construirContexto(contexto);

        String respuestaIA =
                generadorContenidoIAService
                        .generarTexto(instrucciones, contenido);

        try {
                JsonNode leccion = objectMapper.readTree(respuestaIA);

                var validacion = validadorContenidoIAService.validar(
                        contexto,
                        leccion
                );

                if (!validacion.aprobada()) {

                String instruccionesCorreccion = """
                        Corrige la leccion educativa proporcionada.

                        Debes resolver todos los errores identificados
                        por el revisor, sin introducir nuevos errores.

                        Conserva el grado escolar, los PDA, la prioridad
                        y las preferencias de aprendizaje.

                        Mantén una explicacion clara, gradual y enfocada.

                        Devuelve exclusivamente un JSON valido con:
                        {
                        "titulo": "...",
                        "introduccion": "...",
                        "explicacion": "...",
                        "ejemploResuelto": "..."
                        }
                        """;

                String solicitudCorreccion = objectMapper.writeValueAsString(
                        Map.of(
                                "contexto", contexto,
                                "leccionOriginal", leccion,
                                "erroresDetectados", validacion.observaciones()
                        )
                );

                String respuestaCorregida =
                        generadorContenidoIAService.generarTexto(
                                instruccionesCorreccion,
                                solicitudCorreccion
                        );

                leccion = objectMapper.readTree(respuestaCorregida);

                var validacionFinal = validadorContenidoIAService.validar(
                        contexto,
                        leccion
                );

                if (!validacionFinal.aprobada()) {
                        throw new IllegalStateException(
                                "La leccion no supero la validacion matematica: "
                                        + validacionFinal.observaciones()
                        );
                    }
                }

                String titulo = obtenerCampo(leccion, "titulo");
                String introduccion =
                        obtenerCampo(leccion, "introduccion");
                String explicacion =
                        obtenerCampo(leccion, "explicacion");
                String ejemploResuelto =
                        obtenerCampo(leccion, "ejemploResuelto");

                Long leccionId = leccionRepository.crearLeccion(
                        unidadId,
                        titulo,
                        introduccion,
                        explicacion,
                        ejemploResuelto
                );

                return leccionRepository
                        .obtenerLeccion(leccionId)
                        .orElseThrow();

        } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "OpenAI devolvio una leccion con formato JSON invalido.",
                        e
                );
        }
        }

        private String obtenerCampo(JsonNode leccion, String campo) {

        JsonNode valor = leccion.path(campo);

        if (!valor.isTextual() || valor.asText().isBlank()) {
                throw new IllegalStateException(
                        "La leccion generada no contiene un campo valido: "
                                + campo
                );
        }

        return valor.asText().trim();
    }
}
