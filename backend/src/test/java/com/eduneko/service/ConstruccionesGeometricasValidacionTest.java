package com.eduneko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de regresión sin PostgreSQL ni llamadas a OpenAI. */
class ConstruccionesGeometricasValidacionTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ValidadorContenidoIAService validador =
            new ValidadorContenidoIAService(null, mapper);

    @Test
    void mediatrizConMedidasCorrectas() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"construccion_geometrica", "titulo":"Mediatriz de AB",
             "datos":{"construccion":"mediatriz","longitud":6,"unidad":"cm"}}
            """)));
    }

    @Test
    void bisectrizSinMedidaNoInventaGrados() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"construccion_geometrica", "titulo":"Bisectriz esquemática",
             "datos":{"construccion":"bisectriz"}}
            """)));
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"construccion_geometrica", "titulo":"Bisectriz inválida",
             "datos":{"construccion":"bisectriz","grados":190}}
            """)));
    }

    @Test
    void ambasConstruccionesSinMedidas() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"construccion_geometrica", "titulo":"Dos construcciones",
             "datos":{"construccion":"mediatriz_y_bisectriz"}}
            """)));
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
            {"tipo":"construccion_geometrica", "titulo":"Unidad sin longitud",
             "datos":{"construccion":"mediatriz","unidad":"cm"}}
            """)));
    }

    @Test
    void noAceptarUnSegmentoCuandoEjemploPideMediatriz() throws Exception {
        var segmento = mapper.readTree("""
            {"tipo":"segmento_geometrico", "titulo":"Solo paso 1",
             "datos":{"longitud":6,"unidad":"cm"}}
            """);
        var resultado = validador.validarRecursoVisualExistente(
                Map.of("ejemplo_resuelto",
                        "Paso 1: Traza AB. Paso 2: Construye su mediatriz."),
                segmento);
        assertFalse(resultado.aprobada());
    }

    @Test
    void limpiarLeyendasAntiguasSinEliminarProcedimiento() throws Exception {
        // La limpieza opera al devolver la lección, sin UPDATE en PostgreSQL.
        var service = new LeccionService(null, null, null, null,
                null, mapper, validador, null);
        Method metodo = LeccionService.class.getDeclaredMethod(
                "limpiarInstruccionesVisuales", String.class);
        metodo.setAccessible(true);
        String original = """
            Paso 1: Traza un segmento AB de 6 cm.
            **Visualización sugerida:** Agrega el dibujo del segmento.
            Paso 2: Construye los dos arcos con el compás.
            Paso 3: Visualizacion sugerida: inserta una figura.
            Paso 4: Une las intersecciones y comprueba 90°.
            """;
        String limpio = (String) metodo.invoke(service, original);
        assertFalse(limpio.toLowerCase().contains("sugerida"));
        assertTrue(limpio.contains("Paso 1: Traza un segmento AB de 6 cm."));
        assertTrue(limpio.contains("Paso 2: Construye los dos arcos"));
        assertTrue(limpio.contains("Paso 4: Une las intersecciones"));
        assertFalse(limpio.contains("Paso 3:"));
    }
}
