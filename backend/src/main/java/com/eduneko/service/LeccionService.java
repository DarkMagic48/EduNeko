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
                        leccion
                );

                if (!validacion.aprobada()) {

                String instruccionesCorreccion = """
                        Eres un especialista en Matemáticas de secundaria
                        y didáctica educativa en México.

                        Corrige la lección educativa proporcionada.

                        Debes resolver todos los errores identificados
                        por el revisor, sin introducir nuevos errores.

                        REGLAS:
                        1. Conserva el grado escolar, los PDA, la prioridad
                        y las preferencias de aprendizaje.

                        2. Mantén una explicación clara, gradual y enfocada
                        en el concepto central de la lección.

                        3. Verifica que los cálculos, procedimientos y
                        resultados matemáticos sean correctos.

                        4. El ejemplo resuelto debe corresponder
                        con la explicación.

                        RECURSOS VISUALES:
                        5. Incluye siempre el campo recursoVisual en el JSON.

                        6. Si prefiere_visual es true, prioriza un recurso
                        visual cuando sea adecuado al contenido.

                        7. Los estudiantes sin preferencia visual también
                        pueden recibir recursos visuales útiles.

                        
                        8. Los tipos de recursos visuales permitidos son:
                        - "barra_fraccionaria"
                        - "recta_numerica"

                        9. Utiliza "barra_fraccionaria" para representar
                        fracciones propias, impropias, aparentes
                        y números mixtos positivos convertidos
                        a fracciones impropias equivalentes.

                        10. Para barra_fraccionaria:
                        - numerador y denominador deben ser enteros.
                        - denominador debe estar entre 1 y 12.
                        - numerador debe estar entre 0 y cuatro
                          veces el denominador.
                        - Se permiten hasta cuatro barras completas.

                        11. Utiliza "recta_numerica" para representar
                        la ubicación de números positivos, negativos
                        o decimales sobre una recta numérica.

                        Para recta_numerica:
                        - minimo debe ser menor que maximo.
                        - intervalo debe ser positivo y mayor
                          o igual a 0.00000001.
                        - valorDestacado debe estar comprendido
                          entre minimo y maximo.
                        - Todos los valores deben ser números finitos.
                        - (maximo - minimo) / intervalo
                          no debe superar 20.
                        - Prioriza intervalos sencillos y legibles.
                        - No representes desigualdades, intervalos
                          abiertos o cerrados ni varios puntos.

                        12. Conserva un recurso visual válido de
                        la lección original cuando siga siendo
                        coherente con el contenido corregido.

                        Si el recurso original contiene errores,
                        corrígelo o sustitúyelo por uno adecuado.

                        Si no existe una representación correcta,
                        devuelve recursoVisual como null.

                        13. El recurso visual debe ser matemáticamente
                        correcto y corresponder a la explicación
                        y al ejemplo resuelto.

                        No generes HTML, SVG, JavaScript,
                        enlaces de imágenes ni recursos de
                        tipos diferentes de los permitidos.


                        FORMATO:
                        Devuelve exclusivamente un objeto JSON válido:

                        {
                        "titulo": "Título corregido",
                        "introduccion": "Introducción corregida",
                        "explicacion": "Explicación corregida",
                        "ejemploResuelto": "Ejemplo corregido",
                        "recursoVisual": null
                        }

                        Si corresponde una barra fraccionaria,
                        reemplaza únicamente el valor null del campo
                        recursoVisual por el siguiente objeto:

                        {
                        "tipo": "barra_fraccionaria",
                        "titulo": "Representación de tres cuartos",
                        "datos": {
                        "numerador": 3,
                        "denominador": 4
                        }
                        }
                        
                        Si corresponde una recta numérica,
                        reemplaza únicamente el valor null del campo
                        recursoVisual por el siguiente objeto:

                        {
                          "tipo": "recta_numerica",
                          "titulo": "Ubicación del número -2",
                          "datos": {
                            "minimo": -5,
                            "maximo": 5,
                            "intervalo": 1,
                            "valorDestacado": -2
                          }
                        }

                        Incluye únicamente un recurso visual
                        por lección y conserva los campos
                        principales del JSON de la lección.

                        IMPORTANTE:
                        La respuesta debe ser siempre un único objeto JSON
                        con los cinco campos principales:
                        titulo, introduccion, explicacion,
                        ejemploResuelto y recursoVisual.

                        El campo recursoVisual debe contener:
                        - Un objeto barra_fraccionaria válido.
                        - Un objeto recta_numerica válido.
                        - O null si no corresponde ningún recurso.

                        Nunca devuelvas el recurso visual como un objeto
                        independiente de la lección.


                        No incluyas Markdown ni texto fuera del JSON.
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

                //Valida el recurso visual generado por OpenAI.
                //Si el recurso es una barra fraccionaria valida, devuelve su representacion JSON normalizada.
                String recursoVisualJson = validarRecursoVisual(leccion);

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
                String contenido = objectMapper.writeValueAsString(
                        Map.of("leccion", leccion)
                );

                String respuestaIA = generadorContenidoIAService
                        .generarTexto(instrucciones, contenido);

                // La IA puede devolver un objeto visual o null.
                JsonNode recursoVisual = objectMapper.readTree(respuestaIA);

                // Si no hay una representación adecuada,
                // conserva la lección sin modificaciones.
                if (recursoVisual == null || recursoVisual.isNull()) {

                // La IA no encontró una representación visual adecuada.
                // Registramos el resultado para evitar nuevos intentos
                // automáticos al abrir esta misma lección.
                leccionRepository.actualizarEstadoRecursoVisual(
                        leccionId,
                        "NO_APLICABLE"
                );

                // La IA determinó que no existe un recurso visual
                // compatible con el contenido de esta lección.
                System.out.println(
                        "[EDUNEKO VISUAL] Lección " + leccionId
                        + ": OpenAI devolvió null."
                );

                return prepararRespuestaLeccion(leccion);
                }

                // Rechaza recursos con datos incompatibles
                // con los componentes implementados en Angular.
                if (!validadorContenidoIAService
                        .validarEstructuraRecursoVisual(recursoVisual)) {

                // El recurso generado no cumple la estructura
                // requerida por los componentes de Angular.
                leccionRepository.actualizarEstadoRecursoVisual(
                        leccionId,
                        "RECHAZADO"
                );

                // El recurso no cumple la estructura requerida
                // por los componentes visuales de Angular.
                System.out.println(
                        "[EDUNEKO VISUAL] Lección " + leccionId
                        + ": recurso rechazado por estructura."
                );

                return prepararRespuestaLeccion(leccion);
                }

                // Comprueba que el recurso represente correctamente
                // un concepto explicado en la lección.
                var validacion = validadorContenidoIAService
                        .validarRecursoVisualExistente(
                                leccion,
                                recursoVisual
                        );

                // Si el revisor encuentra errores matemáticos
                // o pedagógicos, no guarda el recurso.
                if (!validacion.aprobada()) {

                // El recurso no superó la revisión matemática
                // o su representación no corresponde a la lección.
                leccionRepository.actualizarEstadoRecursoVisual(
                        leccionId,
                        "RECHAZADO"
                );

                // El revisor encontró un problema matemático
                // o de correspondencia con el contenido.
                System.out.println(
                        "[EDUNEKO VISUAL] Lección " + leccionId
                        + ": recurso rechazado por validación pedagógica."
                );

                return prepararRespuestaLeccion(leccion);
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

                // Una respuesta JSON inválida no debe modificar
                // el contenido educativo existente.
                throw new IllegalStateException(
                        "No fue posible interpretar el recurso visual generado.",
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

        private String validarRecursoVisual(JsonNode leccion) {

        // Recupera el recurso visual generado por la IA.
        JsonNode recurso = leccion.path("recursoVisual");

        // Las lecciones pueden no necesitar un recurso visual.
        if (!recurso.isObject()) {
                return null;
        }

        // Verifica el tipo y el título del recurso.
        JsonNode tipo = recurso.path("tipo");
        JsonNode titulo = recurso.path("titulo");
        JsonNode datos = recurso.path("datos");

        if (!tipo.isTextual()
                || !titulo.isTextual()
                || titulo.asText().isBlank()
                || !datos.isObject()) {
                return null;
        }

        String tipoRecurso = tipo.asText();
        String tituloRecurso = titulo.asText().trim();

        // Construye un recurso normalizado.
        // Solo se almacenarán los campos autorizados.
        var recursoValidado = objectMapper.createObjectNode();
        var datosValidados = objectMapper.createObjectNode();

        switch (tipoRecurso) {

                case "barra_fraccionaria" -> {

                JsonNode numerador = datos.path("numerador");
                JsonNode denominador = datos.path("denominador");

                // Los valores deben ser enteros compatibles con int.
                if (!numerador.isIntegralNumber()
                        || !denominador.isIntegralNumber()
                        || !numerador.canConvertToInt()
                        || !denominador.canConvertToInt()) {
                        return null;
                }

                int valorNumerador = numerador.intValue();
                int valorDenominador = denominador.intValue();

                // Se permiten hasta cuatro barras completas.
                if (valorDenominador < 1
                        || valorDenominador > 12
                        || valorNumerador < 0
                        || valorNumerador > valorDenominador * 4) {
                        return null;
                }

                datosValidados.put("numerador", valorNumerador);
                datosValidados.put("denominador", valorDenominador);
                }

                case "recta_numerica" -> {

                JsonNode minimo = datos.path("minimo");
                JsonNode maximo = datos.path("maximo");
                JsonNode intervalo = datos.path("intervalo");
                JsonNode valorDestacado = datos.path("valorDestacado");

                // Todos los campos deben contener números JSON.
                if (!minimo.isNumber()
                        || !maximo.isNumber()
                        || !intervalo.isNumber()
                        || !valorDestacado.isNumber()) {
                        return null;
                }

                double valorMinimo = minimo.doubleValue();
                double valorMaximo = maximo.doubleValue();
                double valorIntervalo = intervalo.doubleValue();
                double valorPunto = valorDestacado.doubleValue();

                // Rechaza números no finitos o rangos inválidos.
                if (!Double.isFinite(valorMinimo)
                        || !Double.isFinite(valorMaximo)
                        || !Double.isFinite(valorIntervalo)
                        || !Double.isFinite(valorPunto)
                        || valorMinimo >= valorMaximo
                        || valorIntervalo <= 0
                        || valorPunto < valorMinimo
                        || valorPunto > valorMaximo) {
                        return null;
                }

                // Limita la cantidad de marcas del componente.
                double cantidadIntervalos =
                        (valorMaximo - valorMinimo) / valorIntervalo;

                if (!Double.isFinite(cantidadIntervalos)
                        || cantidadIntervalos > 20) {
                        return null;
                }

                // Evita valores que puedan producir posiciones
                // o marcas incompatibles con los cálculos de Angular.
                if (!Double.isFinite(valorMaximo - valorMinimo)) {
                        return null;
                }

                // Angular redondea las marcas a ocho decimales.
                // Restringimos la precisión para mantener coherencia.
                if (valorIntervalo < 0.00000001) {
                        return null;
                }

                datosValidados.put("minimo", valorMinimo);
                datosValidados.put("maximo", valorMaximo);
                datosValidados.put("intervalo", valorIntervalo);
                datosValidados.put("valorDestacado", valorPunto);
                }

                // Los componentes todavía no implementados
                // se omiten sin impedir la generación de la lección.
                default -> {
                return null;
                }
        }

        recursoValidado.put("tipo", tipoRecurso);
        recursoValidado.put("titulo", tituloRecurso);
        recursoValidado.set("datos", datosValidados);

        return recursoValidado.toString();
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