
package com.eduneko.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorRectanguloAlgebraicoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // No se llama a OpenAI: solo probamos el validador estructural.
    private final ValidadorContenidoIAService validador =
            new ValidadorContenidoIAService(null, objectMapper);

    /**
     * Construye un recurso de prueba con dimensiones
     * y expresiones algebraicas configurables.
     */
    private JsonNode crearRectangulo(
            double base,
            double altura,
            double area,
            String expresionBase,
            String expresionAltura) throws Exception {

        String json = """
            {
              "tipo": "figura_geometrica_2d",
              "titulo": "Rectángulo algebraico de prueba",
              "datos": {
                "figura": "rectangulo",
                "base": %s,
                "altura": %s,
                "unidad": "cm",
                "algebra": {
                  "incognita": "x",
                  "expresionBase": "%s",
                  "expresionAltura": "%s",
                  "areaConocida": %s
                }
              }
            }
            """.formatted(
                base, altura,
                expresionBase, expresionAltura,
                area
            );

        return objectMapper.readTree(json);
    }

    @Test
    void debeAceptarRectanguloAlgebraicoCorrecto() throws Exception {
        // x = 4, base = x + 2 = 6, altura = x = 4.
        JsonNode recurso = crearRectangulo(
                6, 4, 24, "x + 2", "x");

        assertTrue(
                validador.validarEstructuraRecursoVisual(recurso)
        );
    }

    @Test
    void debeRechazarAreaIncorrecta() throws Exception {
        // Las dimensiones producen 24 cm², no 30 cm².
        JsonNode recurso = crearRectangulo(
                6, 4, 30, "x + 2", "x");

        assertFalse(
                validador.validarEstructuraRecursoVisual(recurso)
        );
    }

    @Test
    void debeRechazarExpresionesInconsistentes() throws Exception {
        // Si x = 4, x + 3 = 7, no 6.
        JsonNode recurso = crearRectangulo(
                6, 4, 24, "x + 3", "x");

        assertFalse(
                validador.validarEstructuraRecursoVisual(recurso)
        );
    }
}
