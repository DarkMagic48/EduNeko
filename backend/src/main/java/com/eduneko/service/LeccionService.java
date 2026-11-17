package com.eduneko.service;

import com.eduneko.repository.LeccionRepository;
import com.eduneko.repository.ActividadRepository;
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

    private final ActividadRepository actividadRepository;
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
            ValidadorContenidoIAService validadorContenidoIAService,
            ActividadRepository actividadRepository
    ) {
        this.leccionRepository = leccionRepository;
        this.diagnosticoService = diagnosticoService;
        this.jdbc = jdbc;
        this.generadorContenidoIAService = generadorContenidoIAService;
        this.personalizacionPedagogicaService = personalizacionPedagogicaService;
        this.objectMapper = objectMapper;
        this.validadorContenidoIAService = validadorContenidoIAService;
        this.actividadRepository = actividadRepository;
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

        Map<String, Object> leccion = leccionRepository
        .obtenerLeccion(leccionId)
        .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se encontró la lección"
        ));

        return prepararRespuestaLeccion(leccion);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerContextoUnidad(
            Long unidadId,
            String correo
    ) {
        validarAccesoUnidad(unidadId, correo);
  
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        Map<String, Object> desempenoAnterior =
        actividadRepository.obtenerDesempenoAnterior(
                unidadId,
                estudianteId
        );

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
                "preferenciasAprendizaje", preferencias,
                "desempenoAnterior", desempenoAnterior
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
                JOIN estudiante e ON e.id = p.estudiante_id
                WHERE u.id = :unidadId
                  AND p.estudiante_id = :estudianteId
                  AND p.estado = 'ACTIVO'
                  AND p.grado_escolar = e.grado_escolar
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
        /**
         * La calidad matemática del texto se valida independientemente
         * del recurso visual. La imagen se genera y revisa después,
         * cuando la lección ya está aprobada y almacenada.
         *
         * No modifica el JSON original devuelto por OpenAI.
         */
        private JsonNode leccionSinRecursoVisual(JsonNode leccion) {
                if (leccion == null || !leccion.isObject()) {
                        return leccion;
                }
                var copia = ((com.fasterxml.jackson.databind.node.ObjectNode) leccion)
                        .deepCopy();
                copia.putNull("recursoVisual");
                return copia;
        }

        @Transactional
        public Map<String, Object> generarLeccion(
                Long unidadId,
                String correo) {

        validarAccesoUnidad(unidadId, correo);

        var leccionExistente =
                leccionRepository.buscarLeccionPorUnidad(unidadId);

        if (leccionExistente.isPresent()) {
                //Recupera la leccion previamente generada.
                //No vuelve a llamar a OpenAI ni modifica su contenido.
                Map<String, Object> leccionGuardada = leccionRepository
                        .obtenerLeccion(leccionExistente.get())
                        .orElseThrow();
                
                //Convierte el recurso visual almacenado en JSONB
                return prepararRespuestaLeccion(leccionGuardada);
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
                        leccionSinRecursoVisual(leccion)
                );

                if (!validacion.aprobada()) {

                String instruccionesCorreccion = """
                        Eres especialista en Matemáticas de secundaria
                        y didáctica educativa de México.

                        Corrige ÚNICAMENTE el contenido académico de la
                        lección según los errores señalados por el revisor.

                        REGLAS:
                        1. Respeta el grado, PDA, objetivos, prioridad
                           y preferencias de aprendizaje recibidos.
                        2. Revisa los cálculos, las fórmulas y cada paso.
                        3. Distingue factores, raíces y resultados:
                           en (x-2)(x-3)=0 las raíces son 2 y 3,
                           no -2 y -3.
                        4. Conserva un ejemplo resuelto completo,
                           correcto y coherente con la explicación.
                        5. No añadas dibujos ni instrucciones para crearlos.
                        6. Los recursos visuales se generan por separado.
                           Devuelve SIEMPRE recursoVisual como null.
                        7. Devuelve solo JSON, sin Markdown.

                        FORMATO:
                        {
                          "titulo": "Título corregido",
                          "introduccion": "Introducción corregida",
                          "explicacion": "Explicación corregida",
                          "ejemploResuelto": "Ejemplo corregido",
                          "recursoVisual": null
                        }
                        """;

                String solicitudCorreccion = objectMapper.writeValueAsString(
                        Map.of(
                                "contexto", contexto,
                                "leccionOriginal", leccionSinRecursoVisual(leccion),
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
                        leccionSinRecursoVisual(leccion)
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
                        limpiarInstruccionesVisuales(
                                obtenerCampo(leccion, "ejemploResuelto"));

                // No guardar aquí el recurso sugerido por la IA.
                // El enriquecimiento posterior valida el diagrama contra
                // la lección ya aprobada, sin bloquear su generación.
                String recursoVisualJson = null;

                Long leccionId = leccionRepository.crearLeccion(
                        unidadId,
                        titulo,
                        introduccion,
                        explicacion,
                        ejemploResuelto,
                        recursoVisualJson
                );
                // Recupera la leccion que acaba de guardarse en PostgreSQL, incluyendo su recurso visual.
                Map<String, Object> leccionGenerada = leccionRepository
                        .obtenerLeccion(leccionId)
                        .orElseThrow();
                // Convierte el recurso visual de texto JSON a un objeto estructurado para el frontend.
                return prepararRespuestaLeccion(leccionGenerada);

        } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "OpenAI devolvio una leccion con formato JSON invalido.",
                        e
                );
        }
        }
        
        /**
         * Agrega un recurso visual a una lección existente.
         *
         * Flujo:
         * 1. Verifica que la unidad pertenezca al estudiante.
         * 2. Recupera la lección sin modificarla.
         * 3. Evita generar otro recurso si ya existe uno.
         * 4. Solicita a OpenAI un recurso visual estructurado.
         * 5. Valida su estructura y coherencia pedagógica.
         * 6. Actualiza únicamente recurso_visual.
         *
         * No modifica actividades, intentos ni progreso.
         */
        /**
         * Reintento manual de un recurso visual previamente descartado.
         * No modifica el contenido académico y exige que la unidad sea del estudiante.
         * Puede consumir llamadas adicionales a la IA, por eso nunca es automático.
         */
        public Map<String, Object> reintentarRecursoVisual(
                Long unidadId, String correo) {
            validarAccesoUnidad(unidadId, correo);
            Long leccionId = leccionRepository.buscarLeccionPorUnidad(unidadId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "La unidad no tiene una lección"));
            leccionRepository.reiniciarRecursoVisualDescartado(leccionId);
            return enriquecerLeccionVisualmente(unidadId, correo);
        }

        /**
         * Identifica lecciones que requieren una representación
         * geométrica específica y no solamente tarjetas de texto.
         *
         * No modifica la lección ni sus recursos almacenados.
         */
        private boolean requiereDiagramaGeometricoEspecifico(
                Map<String, Object> leccion) {

        String titulo = String.valueOf(
                leccion.getOrDefault("titulo", ""));

        String ejemplo = String.valueOf(
                leccion.getOrDefault("ejemplo_resuelto", ""));

        String contenido = (titulo + " " + ejemplo)
                .toLowerCase(java.util.Locale.ROOT);

        return contenido.contains("congruen")
                || contenido.contains("semejan")
                || contenido.contains("mediatr")
                || contenido.contains("bisectr");
        }

        /**
         * Devuelve el motivo del rechazo o null si el recurso es válido.
         * Primero valida estructura sin llamar a IA y después la coherencia
         * con el ejemplo real. Jamás modifica datos ni lecciones existentes.
         */
        private String diagnosticarRecursoVisual(
                Map<String, Object> leccion, JsonNode recurso) {
            if (recurso == null || recurso.isNull()) {
                return "No existe un recurso visual específico para el ejemplo.";
            }
            if (requiereDiagramaGeometricoEspecifico(leccion)
                        && "mapa_conceptual".equals(
                                recurso.path("tipo").asText())) {

                return "El tema requiere una representación geométrica "
                        + "específica relacionada con el ejemplo resuelto. "
                        + "No se aceptan tarjetas de texto como sustituto.";
            }
            if (!validadorContenidoIAService.validarEstructuraRecursoVisual(recurso)) {
                return "El recurso visual no cumple el formato ni los límites admitidos.";
            }
            var validacion = validadorContenidoIAService
                    .validarRecursoVisualExistente(leccion, recurso);
            return validacion.aprobada() ? null : validacion.observaciones();
        }

        public Map<String, Object> enriquecerLeccionVisualmente(
                Long unidadId,
                String correo) {

        // Verifica que la unidad pertenezca a un plan activo
        // del estudiante autenticado.
        validarAccesoUnidad(unidadId, correo);

        // La lección debe existir previamente.
        Long leccionId = leccionRepository
                .buscarLeccionPorUnidad(unidadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La unidad todavía no tiene una lección"
                ));

        // Recupera el contenido educativo existente.
        Map<String, Object> leccion = leccionRepository
                .obtenerLeccion(leccionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la lección"
                ));

        // Recupera el estado del enriquecimiento visual
        // almacenado en PostgreSQL.
        String estadoVisual = (String) leccion.get(
                "estado_recurso_visual"
        );

        // Si ya tiene un recurso visual, no vuelve a llamar
        // a OpenAI ni sobrescribe el recurso almacenado.
        if (leccion.get("recurso_visual") != null) {
                return prepararRespuestaLeccion(leccion);
        }

        // Si anteriormente se determinó que el recurso
        // no es aplicable o fue rechazado, no se vuelve
        // a solicitar automáticamente a OpenAI.
        //
        // Esto evita consumir tokens repetidamente
        // al abrir la misma lección.
        if ("NO_APLICABLE".equals(estadoVisual)
                || "RECHAZADO".equals(estadoVisual)) {

        System.out.println(
                "[EDUNEKO VISUAL] Lección " + leccionId
                + ": enriquecimiento omitido. Estado: "
                + estadoVisual
        );

        return prepararRespuestaLeccion(leccion);
        }

        // Obtiene las instrucciones específicas para generar
        // únicamente un recurso visual, sin reescribir la lección.
        String instrucciones = personalizacionPedagogicaService
                .construirInstruccionesEnriquecimientoVisual();

        try {

                // Envía el contenido existente como contexto.
                // No solicita una nueva lección ni actividades.
                // El enriquecimiento considera las preferencias reales del estudiante.
                // Los recursos siguen disponibles para todos, pero se priorizan los
                // visuales y los explorables cuando las preferencias lo indican.
                String contenido = objectMapper.writeValueAsString(
                        Map.of(
                                "leccion", leccion,
                                "contexto", obtenerContextoUnidad(unidadId, correo)
                        )
                );

                String respuestaIA = generadorContenidoIAService
                        .generarTexto(instrucciones, contenido);

                // La generación del texto ya fue aprobada. Un recurso visual
                // defectuoso no debe invalidarla ni modificar sus resultados.
                JsonNode recursoVisual = objectMapper.readTree(respuestaIA);
                String motivoRechazo = diagnosticarRecursoVisual(leccion, recursoVisual);

                // Un ÚNICO reintento guiado por el error, solamente si la IA
                // propuso un recurso incorrecto. No repetir lecciones, planes
                // ni actividades y evitar ciclos indefinidos de llamadas.
                if (motivoRechazo != null
                        && recursoVisual != null && !recursoVisual.isNull()) {
                    String instruccionesCorreccion = instrucciones + """

                        CORRECCIÓN DEL RECURSO VISUAL:
                        El primer recurso NO superó la validación.
                        Utiliza las observaciones recibidas para devolver
                        solamente un recurso visual corregido, compatible
                        con el ejemplo resuelto y los tipos permitidos.
                        Mantén los valores de la lección, no inventes datos.
                        En ecuaciones cuadráticas distingue los factores de
                        las soluciones: los puntos señalados deben ser raíces.
                        Si no hay una representación matemática fiel,
                        devuelve exclusivamente el JSON null.
                        """;
                    try {
                        String solicitudCorreccion = objectMapper.writeValueAsString(
                                Map.of(
                                        "leccion", leccion,
                                        "contexto", obtenerContextoUnidad(unidadId, correo),
                                        "recursoPropuesto", recursoVisual,
                                        "problemaDetectado", motivoRechazo
                                )
                        );
                        JsonNode corregido = objectMapper.readTree(
                                generadorContenidoIAService.generarTexto(
                                        instruccionesCorreccion, solicitudCorreccion));
                        String errorCorreccion = diagnosticarRecursoVisual(leccion, corregido);
                        if (errorCorreccion == null) {
                            recursoVisual = corregido;
                            motivoRechazo = null;
                            System.out.println("[EDUNEKO VISUAL] Lección " + leccionId
                                    + ": recurso corregido y validado.");
                        } else {
                            motivoRechazo = errorCorreccion;
                        }
                    } catch (JsonProcessingException | RuntimeException errorJson) {
                        // La corrección falló: nunca guardar
                        // ese resultado ni arriesgar la lección ya aprobada.
                        motivoRechazo = "La corrección del recurso visual no es JSON válido.";
                    }
                }

                if (motivoRechazo != null) {
                    // Conservar el texto y las actividades. Si existe una
                    // alternativa segura, usar únicamente conceptos reales
                    // de la lección en lugar de un diagrama falso.
                    Map<String, Object> respaldo =
                            guardarMapaDeEstudioSiEsPosible(leccionId, leccion);
                    if (respaldo != null) return respaldo;

                    String estado = (recursoVisual == null || recursoVisual.isNull())
                            ? "NO_APLICABLE" : "RECHAZADO";
                    leccionRepository.actualizarEstadoRecursoVisual(leccionId, estado);
                    System.out.println("[EDUNEKO VISUAL] Lección " + leccionId
                            + ": recurso no guardado. " + motivoRechazo);
                    return prepararRespuestaLeccion(
                            leccionRepository.obtenerLeccion(leccionId).orElseThrow());
                }

                // Guarda exclusivamente el JSON del recurso visual.
                // El repositorio evita sobrescribir uno existente.
                int filasActualizadas = leccionRepository
                        .guardarRecursoVisual(
                                leccionId,
                                recursoVisual.toString()
                        );

                // Si no se actualizó ninguna fila, puede significar que
                // otra solicitud ya guardó un recurso visual.
                // No intentamos sobrescribirlo ni repetir la generación.
                if (filasActualizadas == 0) {
                // Recuperamos el estado actual directamente desde PostgreSQL.
                Map<String, Object> leccionActual =
                        leccionRepository.obtenerLeccion(leccionId)
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No se encontró la lección"
                                ));

                return prepararRespuestaLeccion(leccionActual);
                }

                // Recupera la versión almacenada en PostgreSQL.
                // Esto también cubre el caso en que otra solicitud
                // haya guardado un recurso antes que esta.
                Map<String, Object> leccionActualizada =
                        leccionRepository.obtenerLeccion(leccionId)
                                .orElseThrow();

                return prepararRespuestaLeccion(leccionActualizada);

                } catch (JsonProcessingException e) {

                // El recurso de IA es inválido; la lección académica permanece
                // almacenada. Evitamos convertir un fallo visual en fallo de lección.
                Map<String, Object> respaldo =
                        guardarMapaDeEstudioSiEsPosible(leccionId, leccion);
                if (respaldo != null) return respaldo;
                leccionRepository.actualizarEstadoRecursoVisual(leccionId, "RECHAZADO");
                return prepararRespuestaLeccion(
                        leccionRepository.obtenerLeccion(leccionId).orElseThrow());
            }
        }

        /**
         * Alternativa verificable cuando la IA no consigue un diagrama matemático.
         * Resume fragmentos LITERALES de la lección ya validada; no inventa
         * medidas, ecuaciones ni resultados, y no modifica el contenido original.
         * Se almacena solo si no hay otro recurso y el estado sigue PENDIENTE.
         */
        private Map<String, Object> guardarMapaDeEstudioSiEsPosible(
                Long leccionId, Map<String, Object> leccion) {

                // Para mediatrices y bisectrices, un mapa genérico NO
                // sustituye el trazo geométrico final del ejemplo.
                // Se conserva el estado de reintento en lugar de guardar
                // una ilustración incompleta como si fuera correcta.
                if (requiereDiagramaGeometricoEspecifico(leccion)) {
                        return null;
                }

                var recurso = objectMapper.createObjectNode();
                recurso.put("tipo", "mapa_conceptual");
                recurso.put("titulo", "Ideas clave de esta lección");
                var datos = recurso.putObject("datos");
                var conceptos = datos.putArray("conceptos");

                agregarFragmentoVerificado(conceptos, "Situación inicial",
                        leccion.get("introduccion"));
                agregarFragmentoVerificado(conceptos, "Explicación",
                        leccion.get("explicacion"));
                agregarFragmentoVerificado(conceptos, "Ejemplo resuelto",
                        leccion.get("ejemplo_resuelto"));

                // El validador conserva la misma estructura exigida a la IA.
                if (!validadorContenidoIAService.validarEstructuraRecursoVisual(recurso)) {
                        return null;
                }

                leccionRepository.guardarRecursoVisual(leccionId, recurso.toString());
                return prepararRespuestaLeccion(
                        leccionRepository.obtenerLeccion(leccionId).orElseThrow());
        }

        /** Conserva texto exacto sin añadir afirmaciones matemáticas nuevas. */
        private void agregarFragmentoVerificado(
                com.fasterxml.jackson.databind.node.ArrayNode conceptos,
                String etiqueta, Object contenido) {
                if (!(contenido instanceof String texto)) return;
                String limpio = ("Ejemplo resuelto".equals(etiqueta)
                        ? limpiarInstruccionesVisuales(texto) : texto)
                        .replaceAll("\\s+", " ").trim();
                if (limpio.isBlank()) return;
                if (limpio.length() > 220) {
                        int espacio = limpio.lastIndexOf(' ', 215);
                        limpio = limpio.substring(0, espacio > 65 ? espacio : 215).trim() + "…";
                }
                var concepto = conceptos.addObject();
                concepto.put("etiqueta", etiqueta);
                concepto.put("detalle", limpio);
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

        private String limpiarInstruccionesVisuales(
                String ejemploResuelto) {

        if (ejemploResuelto == null
                || ejemploResuelto.isBlank()) {
                return ejemploResuelto;
        }

        // Identifica etiquetas de instrucciones visuales
        // situadas al comienzo de una línea.
        //
        // No elimina menciones de diagramas dentro
        // de explicaciones matemáticas normales.
        // También elimina "Visualización sugerida" y sus variantes,
        // incluso si la IA las escribió al final de un paso numerado.
        // Conserva el procedimiento anterior a la etiqueta en esa línea.
        String patron = "(?im)[ \\t]*"
                + "(?:\\*\\*|\\*)?"
                + "(?:Visualizaci[oó]n sugerida|"
                + "Representaci[oó]n visual sugerida|"
                + "Diagrama sugerido|Imagen sugerida)"
                + "(?:\\*\\*|\\*)?"
                + "[ \\t]*:[ \\t]*[^\\r\\n]*";

        // Elimina únicamente las líneas identificadas.
        String resultado = ejemploResuelto.replaceAll(
                patron,
                ""
        );

        // Quitar también encabezados aislados que quedaron al retirar
        // una indicación gráfica (sin borrar pasos con contenido).
        resultado = resultado.replaceAll(
                "(?im)^[ \\t]*Paso[ \\t]+\\d+[ \\t]*[:.)-][ \\t]*(?:\\r?\\n|$)",
                ""
        );
        // Algunos textos históricos almacenan solo la etiqueta, sin dos puntos.
        resultado = resultado.replaceAll(
                "(?im)^[ \\t]*(?:\\*\\*|\\*)?Visualizaci[oó]n sugerida(?:\\*\\*|\\*)?[ \\t]*$",
                ""
        );

        // Reduce los espacios verticales excesivos
        // sin modificar el contenido matemático.
        resultado = resultado.replaceAll(
                "(\\r?\\n){3,}",
                "\n\n"
        );

        return resultado.trim();
        }

        /**
         * Convierte el recurso visual almacenado en PostgreSQL
         * de texto JSON a un objeto que Angular pueda utilizar.
         *
         * Mantiene el valor null para lecciones antiguas
         * o lecciones que no necesitan un diagrama.
         */
        private Map<String, Object> prepararRespuestaLeccion(
                Map<String, Object> leccion) {

        // Crea una copia modificable para no alterar
        // directamente el resultado del repositorio.
        Map<String, Object> respuesta =
                new java.util.HashMap<>(leccion);

        Object ejemploOriginal = respuesta.get("ejemplo_resuelto");

        if (ejemploOriginal instanceof String ejemploTexto) {

        String ejemploLimpio =
                limpiarInstruccionesVisuales(ejemploTexto);

        respuesta.put("ejemplo_resuelto", ejemploLimpio);
        }

        Object recursoVisual = respuesta.get("recurso_visual");

        // Si la lección no tiene recurso visual,
        // conserva null y devuelve los demás datos.
        if (recursoVisual == null) {
                return respuesta;
        }

        // Convierte el texto JSON en un objeto estructurado.
        if (recursoVisual instanceof String recursoJson) {
                try {
                // Convierte el JSON almacenado en un Map de Java.
                // Spring Boot puede serializarlo directamente como
                // un objeto JSON estándar para Angular.
                Map<String, Object> recurso =
                        objectMapper.readValue(
                                recursoJson,
                                new com.fasterxml.jackson.core.type.TypeReference<
                                        Map<String, Object>>() {}
                        );

                respuesta.put("recurso_visual", recurso);

                } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "No fue posible interpretar el recurso visual almacenado.",
                        e
                );
                }
        }

        return respuesta;
        }
}