package com.eduneko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Validaciones estructurales independientes de OpenAI y PostgreSQL. */
class SolidosRevolucionValidacionTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ValidadorContenidoIAService validador =
            new ValidadorContenidoIAService(null, mapper);

    @Test
    void cilindroConMedidasValidas() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"solido_revolucion","titulo":"Cilindro",
             "datos":{"forma":"cilindro","radio":2,"altura":5,"unidad":"cm"}}
            """)));
    }

    @Test
    void esferaNoRequiereAltura() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"solido_revolucion","titulo":"Esfera",
             "datos":{"forma":"esfera","radio":3,"unidad":"m"}}
            """)));
    }

    @Test
    void conoSinAlturaSeRechaza() throws Exception {
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"solido_revolucion","titulo":"Cono",
             "datos":{"forma":"cono","radio":3,"unidad":"cm"}}
            """)));
    }

    @Test
    void esferaConAlturaInventadaSeRechaza() throws Exception {
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"solido_revolucion","titulo":"Esfera",
             "datos":{"forma":"esfera","radio":2,"altura":7,"unidad":"cm"}}
            """)));
    }
}
