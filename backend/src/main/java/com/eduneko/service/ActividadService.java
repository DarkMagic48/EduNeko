package com.eduneko.service;

import com.eduneko.repository.ActividadRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final GeneradorContenidoIAService generadorIA;
    private final ObjectMapper objectMapper;
    private final LeccionService leccionService;
    private final ValidadorActividadesIAService validadorActividades;
    private final DiagnosticoService diagnosticoService;

    public ActividadService(
            ActividadRepository actividadRepository,
            GeneradorContenidoIAService generadorIA,
            ObjectMapper objectMapper,
            LeccionService leccionService,
            ValidadorActividadesIAService validadorActividadesIAService,
            DiagnosticoService diagnosticoService) {

        this.actividadRepository = actividadRepository;
        this.generadorIA = generadorIA;
        this.objectMapper = objectMapper;
        this.leccionService = leccionService;
        this.validadorActividades = validadorActividadesIAService;
        this.diagnosticoService = diagnosticoService;
    }
        /**
         * Recupera las preguntas de una unidad junto con las respuestas
         * que el estudiante haya registrado anteriormente.
         *
         * No devuelve las respuestas correctas oficiales de preguntas pendientes.
         */
        @Transactional(readOnly = true)
        public List<Map<String, Object>> obtenerActividades(
                Long unidadId,
                String correo) {

        // Validar que la unidad pertenece al estudiante autenticado.
        Map<String, Object> leccion =
                leccionService.obtenerLeccion(unidadId, correo);

        Long leccionId =
                ((Number) leccion.get("id")).longValue();

        // Identificar al estudiante para consultar exclusivamente sus intentos.
        Long estudianteId =
                diagnosticoService.obtenerEstudianteId(correo);

        // Recuperar las cinco preguntas existentes.
        List<Map<String, Object>> actividades =
                actividadRepository.obtenerActividades(leccionId);

        // Consultar las respuestas previamente guardadas en PostgreSQL.
        Map<Long, Map<String, Object>> respuestasGuardadas =
                actividadRepository.obtenerRespuestasGuardadas(
                        leccionId,
                        estudianteId
                );

        // Incorporar el historial de cada pregunta a la respuesta de la API.
        for (Map<String, Object> actividad : actividades) {

                Long actividadId =
                        ((Number) actividad.get("id")).longValue();

                Map<String, Object> respuestaGuardada =
                        respuestasGuardadas.get(actividadId);

                if (respuestaGuardada != null) {
                actividad.putAll(respuestaGuardada);
                }
        }

        return actividades;
        }

        @Transactional 
        public List<Map<String, Object>> generarActividadesParaUnidad(
            Long unidadId,
            String correo) {

        Map<String, Object> leccion =
                leccionService.obtenerLeccion(unidadId, correo);

        Long leccionId =
                ((Number) leccion.get("id")).longValue();

        return generarActividades(leccionId, leccion);
    }

    @Transactional
    private List<Map<String, Object>> generarActividades(
            Long leccionId,
            Map<String, Object> leccion) {

        if (actividadRepository.existenActividades(leccionId)) {
            return actividadRepository.obtenerActividades(leccionId);
        }

        String instrucciones = """
            Eres un profesor especialista en Matemáticas
            de secundaria en México.

            Genera exactamente 5 actividades educativas
            basadas en la lección proporcionada.

            REGLAS:
            1. Las actividades deben evaluar conceptos
               explicados en la lección.
            2. Usa dificultad gradual.
            3. Evita ambigüedades y errores matemáticos.
            4. Cada actividad debe tener una única respuesta.
            5. Genera únicamente preguntas de opción múltiple.
            6. Cada pregunta tendrá exactamente 4 opciones.
            7. Las opciones deben ser distintas.
            8. Usa respuestas correctas distribuidas entre
               las opciones, no siempre la primera.
            9. Incluye una explicación breve y clara de la
               respuesta correcta, mostrando el razonamiento.
           10. Genera una pistaPedagogica específica para
                cada pregunta, relacionada con el concepto
                matemático y el procedimiento necesario.
            11. La pista debe orientar al estudiante cuando
                responda incorrectamente, sin revelar la
                respuesta correcta ni el resultado numérico.
            12. La pista debe ser comprensible para estudiantes
                de secundaria y formular una orientación,
                pregunta guía o recordatorio conceptual.
            13. No utilices contenido fuera de la lección.

            Devuelve exclusivamente un JSON válido:

            {
              "actividades": [
                {
                  "pregunta": "...",
                  "tipo": "OPCION_MULTIPLE",
                  "opciones": [
                    "Opción A",
                    "Opción B",
                    "Opción C",
                    "Opción D"
                  ],
                  "respuestaCorrecta": "Opción correcta exacta",
                  "explicacionRespuesta": "Explicación del procedimiento y resultado correcto",
                  "pistaPedagogica": "Orientación específica sobre el concepto o procedimiento, sin revelar la respuesta correcta"
                }
              ]
            }

            No incluyas Markdown ni texto adicional.
            """;

        try {
            String contenido = objectMapper.writeValueAsString(leccion);

            String respuestaIA = generadorIA.generarTexto(
                    instrucciones,
                    contenido
            );

        JsonNode raiz = objectMapper.readTree(respuestaIA);
        JsonNode actividades = raiz.path("actividades");

        // Verificar la estructura básica antes de llamar al validador.
        if (!actividades.isArray() || actividades.size() != 5) {
        throw new IllegalStateException(
                "La IA no devolvio exactamente 5 actividades."
        );
        }

        // Primera validación matemática.
        var validacion = validadorActividades.validar(
                leccion,
                actividades
        );

        if (!validacion.aprobada()) {

        String instruccionesCorreccion = """
                Eres un profesor especialista en Matemáticas
                de secundaria en México.

                Corrige las cinco actividades proporcionadas,
                resolviendo todos los errores identificados
                por el revisor.

                REGLAS:
                - Conserva exactamente cinco actividades.
                - Usa únicamente OPCION_MULTIPLE.
                - Cada actividad debe tener cuatro opciones
                  de texto distintas.
                - Cada pregunta debe tener una única respuesta correcta.
                - La respuestaCorrecta debe coincidir exactamente
                  con una de las cuatro opciones.
                - Las explicaciones deben ser matemáticamente correctas.
                - Conserva o corrige una pistaPedagogica para cada
                  actividad, relacionada con el concepto evaluado.
                - La pista no debe revelar la respuesta correcta,
                  el resultado numérico ni una opción equivalente.
                - La pista debe orientar el razonamiento del
                  estudiante sin resolver el ejercicio.
                - Mantén las preguntas relacionadas con la lección.
                - No introduzcas errores nuevos.

                Devuelve exclusivamente JSON válido con esta estructura:

                {
                "actividades": [
                {
                "pregunta": "...",
                "tipo": "OPCION_MULTIPLE",
                "opciones": [
                        "Opción A",
                        "Opción B",
                        "Opción C",
                        "Opción D"
                ],
                "respuestaCorrecta": "Opción correcta exacta",
                "explicacionRespuesta": "Explicación del procedimiento y resultado correcto",
                "pistaPedagogica": "Orientación específica sobre el concepto o procedimiento, sin revelar la respuesta correcta"
                }
                ]
                }

                No incluyas Markdown ni texto adicional.
                """;

        String contenidoCorreccion = objectMapper.writeValueAsString(
                Map.of(
                        "leccion", leccion,
                        "actividadesOriginales", actividades,
                        "erroresDetectados", validacion.observaciones()
                )
        );

        String respuestaCorregida = generadorIA.generarTexto(
                instruccionesCorreccion,
                contenidoCorreccion
        );

        JsonNode raizCorregida =
                objectMapper.readTree(respuestaCorregida);

        actividades = raizCorregida.path("actividades");

        if (!actividades.isArray() || actividades.size() != 5) {
                throw new IllegalStateException(
                        "La correccion no devolvio exactamente 5 actividades."
                );
        }

        // Segunda y última validación matemática.
        var validacionFinal = validadorActividades.validar(
                leccion,
                actividades
        );

        if (!validacionFinal.aprobada()) {
                throw new IllegalStateException(
                        "Las actividades no superaron la validacion matematica: "
                                + validacionFinal.observaciones()
                );
            }
        }

            if (!actividades.isArray() || actividades.size() != 5) {
                throw new IllegalStateException(
                        "La IA no devolvio exactamente 5 actividades."
                );
            }

            int orden = 1;

            for (JsonNode actividad : actividades) {

                String pregunta =
                        textoObligatorio(actividad, "pregunta");

                String tipo =
                        textoObligatorio(actividad, "tipo");

                String respuestaCorrecta =
                        textoObligatorio(actividad, "respuestaCorrecta");

                String explicacion =
                        textoObligatorio(
                                actividad,
                                "explicacionRespuesta"
                        );
                
                String pistaPedagogica = textoObligatorio(
                        actividad, "pistaPedagogica"
                );

                JsonNode opciones = actividad.path("opciones");

                if (!"OPCION_MULTIPLE".equals(tipo)) {
                    throw new IllegalStateException(
                            "Tipo de actividad no permitido."
                    );
                }

                if (!opciones.isArray() || opciones.size() != 4) {
                    throw new IllegalStateException(
                            "Cada actividad debe tener 4 opciones."
                    );
                }

                Set<String> opcionesUnicas = new HashSet<>();

                for (JsonNode opcion : opciones) {
                    if (!opcion.isTextual()
                            || opcion.asText().isBlank()) {
                        throw new IllegalStateException(
                                "Se encontro una opcion invalida."
                        );
                    }

                    opcionesUnicas.add(opcion.asText().trim());
                }

                if (opcionesUnicas.size() != 4
                        || !opcionesUnicas.contains(respuestaCorrecta)) {
                    throw new IllegalStateException(
                            "Las opciones o la respuesta son invalidas."
                    );
                }

                actividadRepository.guardarActividad(
                        leccionId,
                        pregunta,
                        tipo,
                        opciones,
                        respuestaCorrecta,
                        explicacion,
                        pistaPedagogica,
                        orden++
                );
            }

            return actividadRepository.obtenerActividades(leccionId);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(
                    "No fue posible interpretar las actividades generadas.",
                    e
            );
        }
    }

    private String textoObligatorio(
            JsonNode nodo,
            String campo) {

        JsonNode valor = nodo.path(campo);

        if (!valor.isTextual() || valor.asText().isBlank()) {
            throw new IllegalStateException(
                    "Campo invalido en actividad: " + campo
            );
        }

        return valor.asText().trim();
    }
}
