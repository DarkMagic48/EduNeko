package com.eduneko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración para los recursos visuales
 * de tipo plano_cartesiano.
 *
 * No modifica planes, unidades ni lecciones existentes.
 */
@SpringBootTest
class PlanoCartesianoIntegracionTest {

    @Autowired
    private GeneradorContenidoIAService generadorIA;

    @Autowired
    private PersonalizacionPedagogicaService personalizacion;

    @Autowired
    private ValidadorContenidoIAService validador;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void verificarServiciosDisponibles() {

        // Comprobar que Spring Boot puede proporcionar
        // los servicios necesarios para la prueba.
        assertNotNull(generadorIA);
        assertNotNull(personalizacion);
        assertNotNull(validador);
        assertNotNull(objectMapper);
    }

    /**
     * Comprueba que OpenAI pueda generar un plano cartesiano
     * a partir de una lección ficticia con coordenadas explícitas.
     *
     * No utiliza repositorios ni modifica PostgreSQL.
     */
    @Test
    void generarPlanoCartesianoConIA() throws Exception {

        // Lección ficticia diseñada para probar el recurso.
        // Todas las coordenadas aparecen explícitamente.
        Map<String, Object> leccion = Map.of(
                "titulo", "Ubicación de puntos en el plano cartesiano",

                "introduccion",
                "Aprenderemos a ubicar puntos mediante pares ordenados (x, y).",

                "explicacion",
                """
                El plano cartesiano tiene dos ejes:
                X es horizontal y Y es vertical.

                Para ubicar un punto, primero identificamos
                su coordenada X y después su coordenada Y.

                Trabajaremos con tres puntos:
                A(2, 3), B(-3, 2) y C(0, -2).
                """,

                "ejemplo_resuelto",
                """
                Ubicamos los puntos:

                A(2, 3): avanzamos 2 unidades a la derecha
                y 3 unidades hacia arriba.

                B(-3, 2): avanzamos 3 unidades a la izquierda
                y 2 unidades hacia arriba.

                C(0, -2): permanecemos sobre el eje Y
                y bajamos 2 unidades.
                """
        );

        // Obtener las mismas instrucciones utilizadas
        // por el enriquecimiento visual de EduNeko.
        String instrucciones = personalizacion
                .construirInstruccionesEnriquecimientoVisual();

        // Preparar el contexto que recibirá OpenAI.
        String contenido = objectMapper.writeValueAsString(
                Map.of("leccion", leccion)
        );

        // Generar el recurso visual mediante OpenAI.
        String respuestaIA = generadorIA.generarTexto(
                instrucciones,
                contenido
        );

        System.out.println("===== PLANO CARTESIANO GENERADO =====");
        System.out.println(respuestaIA);
        System.out.println("====================================");

        // Convertir la respuesta en un objeto JSON.
        JsonNode recurso = objectMapper.readTree(respuestaIA);

        // Comprobar que OpenAI haya generado un recurso.
        assertNotNull(recurso);
        assertTrue(recurso.isObject(),
                "La IA debe devolver un objeto JSON.");

        // Verificar que el tipo sea plano_cartesiano.
        assertEquals(
                "plano_cartesiano",
                recurso.path("tipo").asText(),
                "La IA debe seleccionar el plano cartesiano."
        );

        // Validar que Angular pueda representar
        // la estructura generada.
        assertTrue(
                validador.validarEstructuraRecursoVisual(recurso),
                "El plano cartesiano tiene una estructura inválida."
        );

        // Verificar que el recurso corresponda
        // matemáticamente a la lección ficticia.
        var resultado = validador.validarRecursoVisualExistente(
                leccion,
                recurso
        );

        System.out.println("===== VALIDACIÓN PEDAGÓGICA =====");
        System.out.println("Aprobada: " + resultado.aprobada());
        System.out.println("Observaciones: " + resultado.observaciones());
        System.out.println("================================");

        assertTrue(
                resultado.aprobada(),
                "El recurso fue rechazado: "
                        + resultado.observaciones()
        );
    }
}