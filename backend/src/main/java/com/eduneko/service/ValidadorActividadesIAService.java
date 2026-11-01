package com.eduneko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ValidadorActividadesIAService {

    private final GeneradorContenidoIAService generadorIA;
    private final ObjectMapper objectMapper;

    public ValidadorActividadesIAService(
            GeneradorContenidoIAService generadorIA,
            ObjectMapper objectMapper) {

        this.generadorIA = generadorIA;
        this.objectMapper = objectMapper;
    }

    public ResultadoValidacion validar(
            Map<String, Object> leccion,
            JsonNode actividades) {

        String instrucciones = """
            Eres un revisor especializado en Matemáticas
            de secundaria en México.

            Revisa las actividades educativas proporcionadas
            junto con la lección de referencia.

            CRITERIOS OBLIGATORIOS:

            1. Cada pregunta debe ser matemáticamente correcta.
            2. La respuesta marcada como correcta debe ser
               realmente correcta.
            3. Debe existir una única opción correcta.
            4. Las otras tres opciones deben ser incorrectas,
               pero razonables como distractores.
            5. La explicación debe justificar correctamente
               la respuesta.
            6. Las preguntas deben corresponder al contenido
               explicado en la lección.
            7. El lenguaje debe ser adecuado para secundaria.
            8. No debe haber preguntas ambiguas.
            9. Verifica cálculos, signos, operaciones,
               propiedades y procedimientos.
            10. Rechaza cualquier actividad que presente
                errores conceptuales o matemáticos.

            Si una sola actividad tiene errores,
            rechaza el conjunto completo.

            Devuelve exclusivamente JSON válido:

            {
              "aprobada": true,
              "observaciones": "Sin errores detectados"
            }

            Si existen errores:

            {
              "aprobada": false,
              "observaciones": "Describe los errores concretos,
              identifica las preguntas afectadas y explica
              cómo corregirlas."
            }

            No incluyas Markdown ni texto adicional.
            """;

        try {

            String contenido = objectMapper.writeValueAsString(
                    Map.of(
                            "leccion", leccion,
                            "actividades", actividades
                    )
            );

            String respuestaIA = generadorIA.generarTexto(
                    instrucciones,
                    contenido
            );

            JsonNode resultado = objectMapper.readTree(respuestaIA);

            JsonNode aprobada = resultado.path("aprobada");
            JsonNode observaciones = resultado.path("observaciones");

            if (!aprobada.isBoolean()
                    || !observaciones.isTextual()
                    || observaciones.asText().isBlank()) {

                throw new IllegalStateException(
                        "La validación de actividades devolvió "
                                + "un formato incorrecto."
                );
            }

            return new ResultadoValidacion(
                    aprobada.asBoolean(),
                    observaciones.asText().trim()
            );

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "No fue posible interpretar la validación "
                            + "de actividades.",
                    e
            );
        }
    }

    public record ResultadoValidacion(
            boolean aprobada,
            String observaciones
    ) {
    }
}
