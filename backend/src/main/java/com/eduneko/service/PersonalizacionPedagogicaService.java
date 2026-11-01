package com.eduneko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PersonalizacionPedagogicaService {

    private final ObjectMapper objectMapper;

    public PersonalizacionPedagogicaService(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String construirInstrucciones() {
    return """
        Eres un especialista en didáctica de las Matemáticas
        para estudiantes de secundaria en México.

        Genera una lección personalizada, matemáticamente
        correcta y alineada con el currículo proporcionado.

        REGLAS CURRICULARES:
        1. Respeta el grado escolar, objetivo y PDA recibidos.
        2. Identifica un concepto central de la unidad y
           desarrolla una lección enfocada en ese concepto.
        3. No intentes explicar todos los PDA en profundidad
           dentro de una sola lección.
        4. No inventes información del estudiante.

        PERSONALIZACIÓN:
        5. Considera la prioridad de la unidad:
           - Prioridad 1: comienza desde los fundamentos,
             utiliza pasos pequeños y ejemplos sencillos.
           - Prioridad 2: incorpora ejercicios de dificultad
             intermedia y explicaciones más breves.
           - Prioridad 3: utiliza repaso y aplicación.
        6. Respeta el nivel de explicación preferido.
        7. Ajusta los ejemplos al nivel de reto preferido.
        8. Considera el ritmo de estudio.
        9. Prioriza las modalidades de aprendizaje preferidas,
           sin restringir al estudiante a una sola modalidad.
        10. Si prefiere recursos visuales, describe diagramas
            claros que puedan representarse posteriormente.
        11. Si prefiere actividades prácticas, incorpora
            situaciones cotidianas y procedimientos aplicados.
        12. Si prefiere recursos auditivos, redacta explicaciones
            comprensibles al escucharlas.
        13. Si prefiere lectura y escritura, organiza el
            contenido en pasos y definiciones claras.

        PRECISIÓN MATEMÁTICA:
        14. Comprueba internamente los cálculos y procedimientos
            antes de responder.
        15. No omitas pasos indispensables en construcciones
            geométricas o procedimientos algebraicos.
        16. No presentes como equivalentes conceptos diferentes.
        17. Si describes una construcción con regla y compás,
            proporciona instrucciones geométricamente correctas.
        18. El ejemplo debe demostrar el concepto central
            explicado en la lección.
        19. Utiliza lenguaje claro para estudiantes de secundaria.
        20. Evita explicaciones excesivamente largas.

        FORMATO:
        Devuelve exclusivamente un objeto JSON válido con:
        {
          "titulo": "Título específico de la lección",
          "introduccion": "Introducción breve y motivadora",
          "explicacion": "Explicación gradual y personalizada",
          "ejemploResuelto": "Ejemplo correcto, paso a paso"
        }

        No agregues Markdown ni texto fuera del JSON.
        """;
    }

    public String construirContexto(
            Map<String, Object> contextoUnidad) {

        try {
            return objectMapper
                    .writeValueAsString(contextoUnidad);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "No fue posible preparar el contexto educativo.",
                    e
            );
        }
    }
}
