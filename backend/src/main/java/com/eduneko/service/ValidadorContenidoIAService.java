package com.eduneko.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ValidadorContenidoIAService {

    private final GeneradorContenidoIAService generadorIA;
    private final ObjectMapper objectMapper;

    public ValidadorContenidoIAService(
            GeneradorContenidoIAService generadorIA,
            ObjectMapper objectMapper) {

        this.generadorIA = generadorIA;
        this.objectMapper = objectMapper;
    }

    public ResultadoValidacion validar(
            Map<String, Object> contexto,
            JsonNode leccion) {

        String instrucciones = """
            Eres un revisor especializado en Matemáticas
            de secundaria y didáctica educativa en México.

            Evalúa críticamente la lección proporcionada.

            CRITERIOS:
            1. Verifica cálculos, fórmulas y resultados.
            2. Detecta errores conceptuales matemáticos.
            3. Comprueba que los procedimientos sean completos.
            4. Revisa que no se confundan conjuntos numéricos.
            5. Verifica correspondencia con grado, objetivo y PDA.
            6. Evalúa si la explicación es apropiada para
               la prioridad y preferencias del estudiante.
            7. Detecta contradicciones entre explicación y ejemplo.
            8. No apruebes contenido con errores matemáticos.
            9. No rechaces contenido correcto por diferencias
               de redacción o estilo.
            10. Si existe incertidumbre sobre la corrección
                matemática, indica que requiere revisión.

            Devuelve exclusivamente un JSON válido:
            {
              "aprobada": true,
              "observaciones": "Sin errores detectados"
            }

            Si detectas errores, devuelve aprobada=false
            y describe concretamente los problemas encontrados.

            No incluyas Markdown ni texto fuera del JSON.
            """;

        try {
            String contenido = objectMapper.writeValueAsString(
                    Map.of(
                            "contexto", contexto,
                            "leccion", leccion
                    )
            );

            String respuesta = generadorIA.generarTexto(
                    instrucciones,
                    contenido
            );

            JsonNode resultado = objectMapper.readTree(respuesta);

            JsonNode aprobada = resultado.path("aprobada");
            JsonNode observaciones = resultado.path("observaciones");

            if (!aprobada.isBoolean()
                    || !observaciones.isTextual()
                    || observaciones.asText().isBlank()) {

                throw new IllegalStateException(
                        "La validacion de IA devolvio un formato invalido."
                );
            }

            return new ResultadoValidacion(
                    aprobada.asBoolean(),
                    observaciones.asText().trim()
            );

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(
                    "No fue posible interpretar la validacion de IA.",
                    e
            );
        }
    }

    public record ResultadoValidacion(
            boolean aprobada,
            String observaciones
    ) {}
}