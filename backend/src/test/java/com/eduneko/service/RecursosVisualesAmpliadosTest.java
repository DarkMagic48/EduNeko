package com.eduneko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Pruebas estructurales sin acceso a IA ni a PostgreSQL. */
class RecursosVisualesAmpliadosTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ValidadorContenidoIAService validador =
            new ValidadorContenidoIAService(null, mapper);

    @Test
    void funcionLinealValida() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"grafica_funcion", "titulo":"y = 2x + 3",
             "datos":{"a":0,"b":2,"c":3,"minimoX":-5,"maximoX":5,"valorX":1}}
            """);
        assertTrue(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void funcionRechazaDominioInvalido() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"grafica_funcion", "titulo":"Dominio inválido",
             "datos":{"a":1,"b":0,"c":0,"minimoX":5,"maximoX":-5,"valorX":1}}
            """);
        assertFalse(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void proporcionalidadValida() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"modelo_proporcionalidad", "titulo":"Costo proporcional",
             "datos":{"cantidadBase":2,"cantidadCorrespondiente":30,
             "unidadBase":"cuadernos","unidadCorrespondiente":"pesos"}}
            """);
        assertTrue(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void proporcionalidadRechazaCero() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"modelo_proporcionalidad", "titulo":"Sin unidades",
             "datos":{"cantidadBase":0,"cantidadCorrespondiente":30,
             "unidadBase":"kg","unidadCorrespondiente":"pesos"}}
            """);
        assertFalse(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void trianguloValido() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"triangulo_rectangulo", "titulo":"Triángulo 3-4-5",
             "datos":{"catetoA":3,"catetoB":4,"unidad":"cm"}}
            """);
        assertTrue(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void trianguloRechazaCatetoNegativo() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"triangulo_rectangulo", "titulo":"Triángulo inválido",
             "datos":{"catetoA":-3,"catetoB":4,"unidad":"cm"}}
            """);
        assertFalse(validador.validarEstructuraRecursoVisual(recurso));
    }
    @Test
    void probabilidadValida() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"modelo_probabilidad", "titulo":"Dado",
             "datos":{"favorables":2,"total":6,"evento":"Obtener un número par menor que cinco"}}
            """);
        assertTrue(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void probabilidadRechazaFavorablesExcesivos() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"modelo_probabilidad", "titulo":"Dado",
             "datos":{"favorables":7,"total":6,"evento":"Obtener seis"}}
            """);
        assertFalse(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void prismaRectangularValido() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"prisma_rectangular", "titulo":"Caja",
             "datos":{"largo":5,"ancho":3,"alto":2,"unidad":"cm"}}
            """);
        assertTrue(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void prismaRechazaAlturaNegativa() throws Exception {
        var recurso = mapper.readTree("""
            {"tipo":"prisma_rectangular", "titulo":"Caja",
             "datos":{"largo":5,"ancho":3,"alto":-2,"unidad":"cm"}}
            """);
        assertFalse(validador.validarEstructuraRecursoVisual(recurso));
    }

    @Test
    void segmentoUnidimensionalCorrecto() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"segmento_geometrico","titulo":"AB","datos":{"longitud":5,"unidad":"cm"}}
          """)));
        // Caso real reportado: un segmento NO es un rectángulo 5 × 0.1 cm.
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"figura_geometrica_2d","titulo":"segmento falso",
           "datos":{"figura":"rectangulo","base":5,"altura":0.1,"unidad":"cm"}}
          """)));
    }

    @Test
    void ecuacionYTablaValidadas() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"modelo_ecuacion","titulo":"2x+3=7","datos":{"a":2,"b":3,"c":7}}
          """)));
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"modelo_ecuacion","titulo":"sin solución única","datos":{"a":0,"b":3,"c":7}}
          """)));
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"tabla_valores","titulo":"y=2x","datos":{"encabezadoX":"x",
            "encabezadoY":"y","pares":[{"x":1,"y":2},{"x":2,"y":4}]}}
          """)));
    }

    @Test
    void anguloYMapaConceptualValidos() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"angulo_geometrico","titulo":"Ángulo","datos":{"grados":60}}
          """)));
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"mapa_conceptual","titulo":"Operaciones", "datos":{"conceptos":[
            {"etiqueta":"Suma","detalle":"Reúne cantidades"},
            {"etiqueta":"Resta","detalle":"Calcula diferencias"}]}}
          """)));
    }

    @Test
    void graficaCircularRechazaPorcentajesIncoherentes() throws Exception {
        assertTrue(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"grafica_circular","titulo":"Encuesta","datos":{"segmentos":[
            {"etiqueta":"A","porcentaje":40},{"etiqueta":"B","porcentaje":60}]}}
          """)));
        assertFalse(validador.validarEstructuraRecursoVisual(mapper.readTree("""
          {"tipo":"grafica_circular","titulo":"Encuesta errónea","datos":{"segmentos":[
            {"etiqueta":"A","porcentaje":30},{"etiqueta":"B","porcentaje":60}]}}
          """)));
    }
}
