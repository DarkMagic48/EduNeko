package com.eduneko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** Casos del fallo reportado: raíces positivas frente a factores negativos. */
class CoherenciaCuadraticaVisualTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ValidadorContenidoIAService validador =
            new ValidadorContenidoIAService(null, mapper);
    private final Map<String, Object> ejemplo =
            Map.of("ejemplo_resuelto", "Paso 1: Resuelve x² - 5x + 6 = 0. "
                    + "Paso 2: Factoriza (x-2)(x-3)=0. Raíces x=2 y x=3.");

    @Test
    void rechazaRectaDeLosFactoresNegativos() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"recta_numerica","titulo":"Factorización -3 y -2",
             "datos":{"minimo":-5,"maximo":1,"intervalo":1,"valorDestacado":-3}}
            """);
        assertNotNull(validador.verificarCorrespondenciaCuadratica(ejemplo, recurso));
    }

    @Test
    void graficaDeRaicesCorrecta() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"grafica_funcion","titulo":"Cortes en x=2 y x=3",
             "datos":{"a":1,"b":-5,"c":6,"minimoX":-1,"maximoX":5,"valorX":2}}
            """);
        assertNull(validador.verificarCorrespondenciaCuadratica(ejemplo, recurso));
    }

    @Test
    void rechazaGraficasQueOmitenRaices() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"grafica_funcion","titulo":"Recorte incorrecto",
             "datos":{"a":1,"b":-5,"c":6,"minimoX":-5,"maximoX":1,"valorX":0}}
            """);
        assertNotNull(validador.verificarCorrespondenciaCuadratica(ejemplo, recurso));
    }

    @Test
    void rechazaCoeficientesIncompatibles() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"grafica_funcion","titulo":"Parábola equivocada",
             "datos":{"a":1,"b":5,"c":6,"minimoX":-5,"maximoX":5,"valorX":2}}
            """);
        assertNotNull(validador.verificarCorrespondenciaCuadratica(ejemplo, recurso));
    }
}
