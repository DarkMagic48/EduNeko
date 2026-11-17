package com.eduneko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Validación estructural de dos rectas y su punto solución. */
class SistemaLinealVisualTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ValidadorContenidoIAService validador =
            new ValidadorContenidoIAService(null, mapper);

    @Test void sistemaConInterseccionValido() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"sistema_lineal","titulo":"Dos ecuaciones",
            "datos":{"a1":1,"b1":1,"c1":5,"a2":1,"b2":-1,"c2":1,
            "minimoX":-1,"maximoX":6,"minimoY":-1,"maximoY":6}}
            """)));
    }

    @Test void rectasParalelasRechazadas() throws Exception {
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"sistema_lineal","titulo":"Paralelas",
            "datos":{"a1":1,"b1":1,"c1":5,"a2":2,"b2":2,"c2":10,
            "minimoX":-1,"maximoX":6,"minimoY":-1,"maximoY":6}}
            """)));
    }

    @Test void solucionFueraDeRangoRechazada() throws Exception {
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"sistema_lineal","titulo":"Fuera de rango",
            "datos":{"a1":1,"b1":1,"c1":5,"a2":1,"b2":-1,"c2":1,
            "minimoX":-10,"maximoX":1,"minimoY":-10,"maximoY":1}}
            """)));
    }
}
