
package com.eduneko.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración para el recurso visual
 * de tipo grafica_estadistica.
 *
 * Utiliza OpenAI, pero no modifica planes,
 * unidades ni lecciones en PostgreSQL.
 */
@SpringBootTest
class GraficaEstadisticaIntegracionTest {

    @Autowired
    private GeneradorContenidoIAService generadorIA;

    @Autowired
    private PersonalizacionPedagogicaService personalizacion;

    @Autowired
    private ValidadorContenidoIAService validador;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Comprueba que OpenAI pueda generar una gráfica
     * de barras a partir de datos explícitos.
     */
    @Test
    void generarGraficaEstadisticaConIA() throws Exception {

        // Lección ficticia para probar el componente.
        // Las categorías y cantidades están definidas
        // explícitamente para evitar datos inventados.
        Map<String, Object> leccion = Map.of(
                "titulo", "Representación de datos en gráficas de barras",

                "introduccion",
                """
                Aprenderemos a organizar e interpretar
                datos estadísticos mediante gráficas
                de barras.
                """,

                "explicacion",
                """
                Una gráfica de barras permite comparar
                cantidades entre diferentes categorías.

                En una encuesta escolar se preguntó
                cuántos libros habían leído tres grupos.

                Los resultados fueron:

                Grupo A: 12 libros.
                Grupo B: 18 libros.
                Grupo C: 15 libros.

                Cada barra representa un grupo y su altura
                corresponde a la cantidad de libros.
                """,

                "ejemplo_resuelto",
                """
                Representamos los resultados de la encuesta
                en una gráfica de barras:

                A = 12 libros.
                B = 18 libros.
                C = 15 libros.

                El grupo B tiene el valor más alto, con
                18 libros.

                El grupo A tiene el valor más bajo, con
                12 libros.
                """
        );

        // Obtener las instrucciones reales que utiliza
        // EduNeko para generar recursos visuales.
        String instrucciones = personalizacion
                .construirInstruccionesEnriquecimientoVisual();

        // Convertir la lección en el contexto JSON
        // que recibirá OpenAI.
        String contenido = objectMapper.writeValueAsString(
                Map.of("leccion", leccion)
        );

        // Primera llamada a OpenAI:
        // generar el recurso visual.
        String respuestaIA = generadorIA.generarTexto(
                instrucciones,
                contenido
        );

        System.out.println(
                "===== GRÁFICA ESTADÍSTICA GENERADA ====="
        );
        System.out.println(respuestaIA);
        System.out.println(
                "======================================="
        );

        // Interpretar la respuesta de OpenAI.
        JsonNode recurso = objectMapper.readTree(respuestaIA);

        assertNotNull(recurso);
        assertTrue(
                recurso.isObject(),
                "La IA debe devolver un objeto JSON."
        );

        // Verificar el tipo de recurso seleccionado.
        assertEquals(
                "grafica_estadistica",
                recurso.path("tipo").asText(),
                "La IA debe seleccionar una gráfica estadística."
        );

        // La lección requiere comparar categorías,
        // por lo que esperamos una gráfica de barras.
        JsonNode datos = recurso.path("datos");

        assertEquals(
                "barras",
                datos.path("modalidad").asText(),
                "La modalidad esperada es barras."
        );

        // Comprobar que la estructura sea compatible
        // con las reglas del backend y Angular.
        assertTrue(
                validador.validarEstructuraRecursoVisual(recurso),
                "La gráfica tiene una estructura inválida."
        );

        // Verificar que se hayan conservado exactamente
        // las tres categorías de la lección.
        JsonNode elementos = datos.path("elementos");

        assertEquals(
                3,
                elementos.size(),
                "La gráfica debe contener tres grupos."
        );

        // Recuperar los valores por etiqueta para que
        // la comprobación no dependa del orden del JSON.
        Map<String, Integer> valoresEsperados = Map.of(
                "A", 12,
                "B", 18,
                "C", 15
        );

        for (JsonNode elemento : elementos) {

            String etiqueta = elemento.path("etiqueta").asText();
            JsonNode valor = elemento.path("valor");

            assertTrue(
                    valoresEsperados.containsKey(etiqueta),
                    "La IA generó una categoría no esperada: "
                            + etiqueta
            );

            assertTrue(
                    valor.isNumber(),
                    "Cada categoría debe tener un valor numérico."
            );

            assertEquals(
                    valoresEsperados.get(etiqueta).doubleValue(),
                    valor.doubleValue(),
                    0.000001,
                    "La cantidad no coincide con la lección: "
                            + etiqueta
            );
        }

        // Segunda llamada a OpenAI:
        // revisar la correspondencia pedagógica.
        var resultado = validador.validarRecursoVisualExistente(
                leccion,
                recurso
        );

        System.out.println(
                "===== VALIDACIÓN PEDAGÓGICA ====="
        );
        System.out.println(
                "Aprobada: " + resultado.aprobada()
        );
        System.out.println(
                "Observaciones: " + resultado.observaciones()
        );
        System.out.println(
                "================================"
        );

        assertTrue(
                resultado.aprobada(),
                "La gráfica fue rechazada: "
                        + resultado.observaciones()
        );
    }

    
/**
 * Comprueba que OpenAI genere una gráfica de líneas
 * a partir de una secuencia temporal explícita.
 *
 * No modifica la base de datos.
 */
@Test
void generarGraficaLineasConIA() throws Exception {

    // Lección ficticia con datos ordenados por día.
    Map<String, Object> leccion = Map.of(
            "titulo", "Interpretación de gráficas de líneas",

            "introduccion",
            """
            Aprenderemos a representar cambios de una
            cantidad a través del tiempo.
            """,

            "explicacion",
            """
            Una gráfica de líneas permite visualizar
            cómo cambia una cantidad en observaciones
            consecutivas.

            Durante cuatro días se registró la cantidad
            de visitantes de una biblioteca escolar:

            Lunes: 20 visitantes.
            Martes: 25 visitantes.
            Miércoles: 15 visitantes.
            Jueves: 30 visitantes.

            Los puntos se colocan en orden cronológico
            y se conectan para observar los cambios.
            """,

            "ejemplo_resuelto",
            """
            Representamos los datos en una gráfica de líneas:

            Lun = 20 visitantes.
            Mar = 25 visitantes.
            Mie = 15 visitantes.
            Jue = 30 visitantes.

            De lunes a martes aumenta la cantidad.
            De martes a miércoles disminuye.
            De miércoles a jueves vuelve a aumentar.
            """
    );

    // Recuperar las instrucciones reales de EduNeko.
    String instrucciones = personalizacion
            .construirInstruccionesEnriquecimientoVisual();

    String contenido = objectMapper.writeValueAsString(
            Map.of("leccion", leccion)
    );

    // Generar el recurso mediante OpenAI.
    String respuestaIA = generadorIA.generarTexto(
            instrucciones,
            contenido
    );

    System.out.println(
            "===== GRÁFICA DE LÍNEAS GENERADA ====="
    );
    System.out.println(respuestaIA);
    System.out.println(
            "====================================="
    );

    JsonNode recurso = objectMapper.readTree(respuestaIA);

    // Comprobar el tipo y la modalidad.
    assertTrue(recurso.isObject());

    assertEquals(
            "grafica_estadistica",
            recurso.path("tipo").asText()
    );

    JsonNode datos = recurso.path("datos");

    assertEquals(
            "lineas",
            datos.path("modalidad").asText()
    );

    // Validar la estructura antes de revisar los valores.
    assertTrue(
            validador.validarEstructuraRecursoVisual(recurso),
            "La gráfica de líneas tiene una estructura inválida."
    );

    JsonNode elementos = datos.path("elementos");

    String[] etiquetasEsperadas = {
            "Lun", "Mar", "Mie", "Jue"
    };

    int[] valoresEsperados = {
            20, 25, 15, 30
    };

    assertEquals(
            4,
            elementos.size(),
            "La gráfica debe contener cuatro observaciones."
    );

    // Verificar valores y orden cronológico.
    for (int i = 0; i < elementos.size(); i++) {

        JsonNode elemento = elementos.get(i);

        assertEquals(
                etiquetasEsperadas[i],
                elemento.path("etiqueta").asText(),
                "La etiqueta no coincide en la posición " + i
        );

        assertEquals(
                valoresEsperados[i],
                elemento.path("valor").asInt(),
                "El valor no coincide en la posición " + i
        );
    }

    // Comprobar correspondencia pedagógica con OpenAI.
    var resultado = validador.validarRecursoVisualExistente(
            leccion,
            recurso
    );

    System.out.println(
            "===== VALIDACIÓN PEDAGÓGICA DE LÍNEAS ====="
    );
    System.out.println("Aprobada: " + resultado.aprobada());
    System.out.println(
            "Observaciones: " + resultado.observaciones()
    );
    System.out.println(
            "=========================================="
    );

    assertTrue(
            resultado.aprobada(),
            "La gráfica fue rechazada: "
                    + resultado.observaciones()
    );
}

}
