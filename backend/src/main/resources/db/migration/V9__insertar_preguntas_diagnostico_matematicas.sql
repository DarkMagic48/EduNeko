-- EduNeko V1: banco inicial de Matemáticas (45 preguntas y 180 opciones).
-- Dificultad estimada pedagógicamente; pendiente de calibración empírica.
-- La migración requiere que exista la materia Matemáticas en V6.
DO $$
DECLARE
    v_materia_id BIGINT;
    v_pregunta_id BIGINT;
BEGIN
    SELECT id INTO STRICT v_materia_id FROM materia WHERE nombre = 'Matemáticas';

    -- Pregunta 1 | Grado 1 | BASICO | Fracciones y decimales
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Fracciones y decimales', 'BASICO', '¿Qué fracción equivale a 0.5?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '1/4', FALSE),
        (v_pregunta_id, '1/2', TRUE),
        (v_pregunta_id, '3/4', FALSE),
        (v_pregunta_id, '1/5', FALSE);

    -- Pregunta 2 | Grado 1 | INTERMEDIO | Fracciones y decimales
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Fracciones y decimales', 'INTERMEDIO', '¿Cuál de los siguientes números es mayor?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '0.65', FALSE),
        (v_pregunta_id, '3/5', FALSE),
        (v_pregunta_id, '0.7', TRUE),
        (v_pregunta_id, '1/2', FALSE);

    -- Pregunta 3 | Grado 1 | AVANZADO | Fracciones y decimales
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Fracciones y decimales', 'AVANZADO', 'Una botella contiene 1.5 litros de agua. Si se utilizan 3/4 de litro, ¿cuánta agua queda?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '0.25 litros', FALSE),
        (v_pregunta_id, '0.5 litros', FALSE),
        (v_pregunta_id, '0.75 litros', TRUE),
        (v_pregunta_id, '1.25 litros', FALSE);

    -- Pregunta 4 | Grado 1 | BASICO | Números enteros y recta numérica
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Números enteros y recta numérica', 'BASICO', '¿Qué número entero está inmediatamente a la izquierda del 0 en la recta numérica?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '1', FALSE),
        (v_pregunta_id, '-1', TRUE),
        (v_pregunta_id, '2', FALSE),
        (v_pregunta_id, '-2', FALSE);

    -- Pregunta 5 | Grado 1 | INTERMEDIO | Números enteros y recta numérica
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Números enteros y recta numérica', 'INTERMEDIO', '¿Cuál es el orden correcto de menor a mayor de -3, 2, -1 y 0?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '-3, -1, 0, 2', TRUE),
        (v_pregunta_id, '0, -1, 2, -3', FALSE),
        (v_pregunta_id, '2, 0, -1, -3', FALSE),
        (v_pregunta_id, '-1, -3, 0, 2', FALSE);

    -- Pregunta 6 | Grado 1 | AVANZADO | Números enteros y recta numérica
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Números enteros y recta numérica', 'AVANZADO', 'La temperatura es de -2 °C y aumenta 7 °C. ¿Cuál es la temperatura final?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '-9 °C', FALSE),
        (v_pregunta_id, '-5 °C', FALSE),
        (v_pregunta_id, '5 °C', TRUE),
        (v_pregunta_id, '9 °C', FALSE);

    -- Pregunta 7 | Grado 1 | BASICO | Operaciones básicas y jerarquía
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Operaciones básicas y jerarquía', 'BASICO', '¿Cuánto es 48 ÷ 6?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '6', FALSE),
        (v_pregunta_id, '7', FALSE),
        (v_pregunta_id, '8', TRUE),
        (v_pregunta_id, '9', FALSE);

    -- Pregunta 8 | Grado 1 | INTERMEDIO | Operaciones básicas y jerarquía
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Operaciones básicas y jerarquía', 'INTERMEDIO', '¿Cuál es el resultado de 6 + 4 × 3?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '18', TRUE),
        (v_pregunta_id, '30', FALSE),
        (v_pregunta_id, '24', FALSE),
        (v_pregunta_id, '15', FALSE);

    -- Pregunta 9 | Grado 1 | AVANZADO | Operaciones básicas y jerarquía
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Operaciones básicas y jerarquía', 'AVANZADO', 'Hay 5 cajas con 12 lápices cada una. Si se reparten 38 lápices, ¿cuántos quedan?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '12', FALSE),
        (v_pregunta_id, '22', TRUE),
        (v_pregunta_id, '32', FALSE),
        (v_pregunta_id, '38', FALSE);

    -- Pregunta 10 | Grado 1 | BASICO | Patrones e introducción al álgebra
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Patrones e introducción al álgebra', 'BASICO', '¿Qué número continúa la secuencia 3, 6, 9, 12, ...?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '13', FALSE),
        (v_pregunta_id, '14', FALSE),
        (v_pregunta_id, '15', TRUE),
        (v_pregunta_id, '18', FALSE);

    -- Pregunta 11 | Grado 1 | INTERMEDIO | Patrones e introducción al álgebra
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Patrones e introducción al álgebra', 'INTERMEDIO', 'Una secuencia empieza en 5 y aumenta 4 en cada término. ¿Cuál es el quinto término?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '17', FALSE),
        (v_pregunta_id, '19', FALSE),
        (v_pregunta_id, '20', FALSE),
        (v_pregunta_id, '21', TRUE);

    -- Pregunta 12 | Grado 1 | AVANZADO | Patrones e introducción al álgebra
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Patrones e introducción al álgebra', 'AVANZADO', 'Si al multiplicar un número por 3 y sumarle 4 se obtiene 25, ¿cuál es ese número?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '6', FALSE),
        (v_pregunta_id, '7', TRUE),
        (v_pregunta_id, '8', FALSE),
        (v_pregunta_id, '9', FALSE);

    -- Pregunta 13 | Grado 1 | BASICO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Geometría y medición', 'BASICO', '¿Cuántos lados tiene un hexágono?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '5', FALSE),
        (v_pregunta_id, '6', TRUE),
        (v_pregunta_id, '7', FALSE),
        (v_pregunta_id, '8', FALSE);

    -- Pregunta 14 | Grado 1 | INTERMEDIO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Geometría y medición', 'INTERMEDIO', '¿Cuál es el perímetro de un rectángulo de 8 cm de largo y 5 cm de ancho?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '13 cm', FALSE),
        (v_pregunta_id, '26 cm', TRUE),
        (v_pregunta_id, '40 cm', FALSE),
        (v_pregunta_id, '80 cm', FALSE);

    -- Pregunta 15 | Grado 1 | AVANZADO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 1, 'Geometría y medición', 'AVANZADO', 'Un jardín rectangular mide 12 m de largo y 7 m de ancho. ¿Cuál es su área?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '19 m²', FALSE),
        (v_pregunta_id, '38 m²', FALSE),
        (v_pregunta_id, '72 m²', FALSE),
        (v_pregunta_id, '84 m²', TRUE);

    -- Pregunta 16 | Grado 2 | BASICO | Números y operaciones
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Números y operaciones', 'BASICO', '¿Cuál es el resultado de -12 + 7 - (-5)?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '-24', FALSE),
        (v_pregunta_id, '-10', FALSE),
        (v_pregunta_id, '0', TRUE),
        (v_pregunta_id, '10', FALSE);

    -- Pregunta 17 | Grado 2 | INTERMEDIO | Números y operaciones
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Números y operaciones', 'INTERMEDIO', '¿Cuánto es 3/4 + 2/3 + 1/6?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '13/12', FALSE),
        (v_pregunta_id, '17/12', FALSE),
        (v_pregunta_id, '19/12', TRUE),
        (v_pregunta_id, '6/13', FALSE);

    -- Pregunta 18 | Grado 2 | AVANZADO | Números y operaciones
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Números y operaciones', 'AVANZADO', '¿Cuál es el resultado de -8 - (-3) × 4 + 18 ÷ (-6)?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '-3', FALSE),
        (v_pregunta_id, '-1', FALSE),
        (v_pregunta_id, '3', FALSE),
        (v_pregunta_id, '1', TRUE);

    -- Pregunta 19 | Grado 2 | BASICO | Lenguaje y expresiones algebraicas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Lenguaje y expresiones algebraicas', 'BASICO', '¿Qué expresión representa el triple de un número, menos 5 unidades?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '3(x - 5)', FALSE),
        (v_pregunta_id, '3x - 5', TRUE),
        (v_pregunta_id, 'x/3 - 5', FALSE),
        (v_pregunta_id, '3x + 5', FALSE);

    -- Pregunta 20 | Grado 2 | INTERMEDIO | Lenguaje y expresiones algebraicas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Lenguaje y expresiones algebraicas', 'INTERMEDIO', 'Si x = -2, ¿cuánto vale 2x² - 3x - 3?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '-1', FALSE),
        (v_pregunta_id, '5', FALSE),
        (v_pregunta_id, '11', TRUE),
        (v_pregunta_id, '19', FALSE);

    -- Pregunta 21 | Grado 2 | AVANZADO | Lenguaje y expresiones algebraicas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Lenguaje y expresiones algebraicas', 'AVANZADO', 'Un triángulo tiene dos lados que miden 2x + 3 cada uno y un tercer lado que mide x + 6. ¿Cuál es su perímetro?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '2x + 9', FALSE),
        (v_pregunta_id, '5x + 9', FALSE),
        (v_pregunta_id, '5x + 3', FALSE),
        (v_pregunta_id, '5x + 12', TRUE);

    -- Pregunta 22 | Grado 2 | BASICO | Ecuaciones y resolución de problemas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Ecuaciones y resolución de problemas', 'BASICO', '¿Cuál es el valor de x en 4x - 7 = 21?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '5', FALSE),
        (v_pregunta_id, '6', FALSE),
        (v_pregunta_id, '7', TRUE),
        (v_pregunta_id, '8', FALSE);

    -- Pregunta 23 | Grado 2 | INTERMEDIO | Ecuaciones y resolución de problemas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Ecuaciones y resolución de problemas', 'INTERMEDIO', '¿Cuál es el valor de x en 3(x + 4) = 5x + 2?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '4', FALSE),
        (v_pregunta_id, '5', TRUE),
        (v_pregunta_id, '6', FALSE),
        (v_pregunta_id, '7', FALSE);

    -- Pregunta 24 | Grado 2 | AVANZADO | Ecuaciones y resolución de problemas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Ecuaciones y resolución de problemas', 'AVANZADO', 'Dos números suman 60 y el mayor es el doble del menor. ¿Cuáles son?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '12 y 24', FALSE),
        (v_pregunta_id, '16 y 32', FALSE),
        (v_pregunta_id, '18 y 36', FALSE),
        (v_pregunta_id, '20 y 40', TRUE);

    -- Pregunta 25 | Grado 2 | BASICO | Proporcionalidad y funciones
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Proporcionalidad y funciones', 'BASICO', 'Si 4 cuadernos cuestan $76, ¿cuánto cuestan 7 cuadernos al mismo precio unitario?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '$114', FALSE),
        (v_pregunta_id, '$126', FALSE),
        (v_pregunta_id, '$133', TRUE),
        (v_pregunta_id, '$152', FALSE);

    -- Pregunta 26 | Grado 2 | INTERMEDIO | Proporcionalidad y funciones
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Proporcionalidad y funciones', 'INTERMEDIO', 'Si 6 trabajadores terminan una obra en 8 días, ¿cuántos días tardarían 8 trabajadores al mismo ritmo?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '4', FALSE),
        (v_pregunta_id, '6', TRUE),
        (v_pregunta_id, '8', FALSE),
        (v_pregunta_id, '12', FALSE);

    -- Pregunta 27 | Grado 2 | AVANZADO | Proporcionalidad y funciones
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Proporcionalidad y funciones', 'AVANZADO', 'La empresa A cobra $25 fijos más $10 por kilómetro; la empresa B cobra $10 fijos más $13 por kilómetro. ¿A qué distancia cuestan lo mismo?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '3 km', FALSE),
        (v_pregunta_id, '4 km', FALSE),
        (v_pregunta_id, '5 km', TRUE),
        (v_pregunta_id, '6 km', FALSE);

    -- Pregunta 28 | Grado 2 | BASICO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Geometría y medición', 'BASICO', '¿Cuál es el área de un triángulo con base de 14 cm y altura de 9 cm?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '23 cm²', FALSE),
        (v_pregunta_id, '63 cm²', TRUE),
        (v_pregunta_id, '126 cm²', FALSE),
        (v_pregunta_id, '46 cm²', FALSE);

    -- Pregunta 29 | Grado 2 | INTERMEDIO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Geometría y medición', 'INTERMEDIO', 'De un rectángulo de 15 cm por 10 cm se recorta un rectángulo de 6 cm por 5 cm. ¿Qué área queda?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '84 cm²', FALSE),
        (v_pregunta_id, '96 cm²', FALSE),
        (v_pregunta_id, '120 cm²', TRUE),
        (v_pregunta_id, '144 cm²', FALSE);

    -- Pregunta 30 | Grado 2 | AVANZADO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 2, 'Geometría y medición', 'AVANZADO', 'Un rectángulo tiene perímetro de 36 m y su largo mide 4 m más que su ancho. ¿Cuánto mide el largo?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '8 m', FALSE),
        (v_pregunta_id, '9 m', FALSE),
        (v_pregunta_id, '10 m', FALSE),
        (v_pregunta_id, '11 m', TRUE);

    -- Pregunta 31 | Grado 3 | BASICO | Operaciones y expresiones algebraicas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Operaciones y expresiones algebraicas', 'BASICO', 'Simplifica 5x - 3(2x - 4) + 7.')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '-x + 5', FALSE),
        (v_pregunta_id, '-x + 19', TRUE),
        (v_pregunta_id, '11x + 19', FALSE),
        (v_pregunta_id, '-x - 5', FALSE);

    -- Pregunta 32 | Grado 3 | INTERMEDIO | Operaciones y expresiones algebraicas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Operaciones y expresiones algebraicas', 'INTERMEDIO', '¿Cuál es el resultado de (3x + 2)(x - 4)?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '3x² + 2x - 8', FALSE),
        (v_pregunta_id, '3x² - 2x - 8', FALSE),
        (v_pregunta_id, '3x² - 10x - 8', TRUE),
        (v_pregunta_id, '3x² - 10x + 8', FALSE);

    -- Pregunta 33 | Grado 3 | AVANZADO | Operaciones y expresiones algebraicas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Operaciones y expresiones algebraicas', 'AVANZADO', 'Simplifica (2x + 3)(2x - 1) - 2(x + 1).')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '4x² + 6x - 5', FALSE),
        (v_pregunta_id, '4x² - 2x - 5', FALSE),
        (v_pregunta_id, '4x² + 4x - 5', FALSE),
        (v_pregunta_id, '4x² + 2x - 5', TRUE);

    -- Pregunta 34 | Grado 3 | BASICO | Ecuaciones y desigualdades
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Ecuaciones y desigualdades', 'BASICO', '¿Cuál es el valor de x en 5x - 8 = 2x + 10?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '6', TRUE),
        (v_pregunta_id, '-6', FALSE),
        (v_pregunta_id, '3', FALSE),
        (v_pregunta_id, '12', FALSE);

    -- Pregunta 35 | Grado 3 | INTERMEDIO | Ecuaciones y desigualdades
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Ecuaciones y desigualdades', 'INTERMEDIO', '¿Cuál es el valor de x en 2(3x - 4) - 5 = 3(x - 1) + 2x + 2?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '6', FALSE),
        (v_pregunta_id, '9', FALSE),
        (v_pregunta_id, '12', TRUE),
        (v_pregunta_id, '15', FALSE);

    -- Pregunta 36 | Grado 3 | AVANZADO | Ecuaciones y desigualdades
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Ecuaciones y desigualdades', 'AVANZADO', 'Un transporte cobra $35 de tarifa fija más $12 por kilómetro. Si una persona dispone de un máximo de $155, ¿cuántos kilómetros como máximo puede recorrer?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '8 km', FALSE),
        (v_pregunta_id, '9 km', FALSE),
        (v_pregunta_id, '10 km', TRUE),
        (v_pregunta_id, '12 km', FALSE);

    -- Pregunta 37 | Grado 3 | BASICO | Funciones y gráficas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Funciones y gráficas', 'BASICO', 'Si y = 2x + 3, ¿cuánto vale y cuando x = -1?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '-3', FALSE),
        (v_pregunta_id, '-1', FALSE),
        (v_pregunta_id, '1', TRUE),
        (v_pregunta_id, '3', FALSE);

    -- Pregunta 38 | Grado 3 | INTERMEDIO | Funciones y gráficas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Funciones y gráficas', 'INTERMEDIO', '¿Cuál es la ecuación de la recta que pasa por los puntos (3, 3) y (5, 7)?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, 'y = 2x + 3', FALSE),
        (v_pregunta_id, 'y = 2x - 3', TRUE),
        (v_pregunta_id, 'y = -2x + 3', FALSE),
        (v_pregunta_id, 'y = -2x - 3', FALSE);

    -- Pregunta 39 | Grado 3 | AVANZADO | Funciones y gráficas
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Funciones y gráficas', 'AVANZADO', 'El servicio A cobra $20 fijos más $6 por viaje y el servicio B cobra $8 fijos más $8 por viaje. ¿A partir de cuántos viajes el servicio A resulta más barato?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '5', FALSE),
        (v_pregunta_id, '6', FALSE),
        (v_pregunta_id, '7', TRUE),
        (v_pregunta_id, '8', FALSE);

    -- Pregunta 40 | Grado 3 | BASICO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Geometría y medición', 'BASICO', 'Dos triángulos son semejantes. Un lado de 6 cm del primero corresponde a uno de 15 cm del segundo. Si otro lado del primero mide 8 cm, ¿cuánto mide el lado correspondiente del segundo?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '16 cm', FALSE),
        (v_pregunta_id, '18 cm', FALSE),
        (v_pregunta_id, '20 cm', TRUE),
        (v_pregunta_id, '24 cm', FALSE);

    -- Pregunta 41 | Grado 3 | INTERMEDIO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Geometría y medición', 'INTERMEDIO', '¿Cuál es el área de un trapecio con bases de 18 cm y 10 cm y altura de 6 cm?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '60 cm²', FALSE),
        (v_pregunta_id, '72 cm²', FALSE),
        (v_pregunta_id, '84 cm²', TRUE),
        (v_pregunta_id, '96 cm²', FALSE);

    -- Pregunta 42 | Grado 3 | AVANZADO | Geometría y medición
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Geometría y medición', 'AVANZADO', 'Un terreno rectangular tiene un área de 96 m² y un ancho de 8 m. ¿Cuántos metros de cerca se necesitan para rodearlo por completo?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '20 m', FALSE),
        (v_pregunta_id, '32 m', FALSE),
        (v_pregunta_id, '36 m', FALSE),
        (v_pregunta_id, '40 m', TRUE);

    -- Pregunta 43 | Grado 3 | BASICO | Estadística y probabilidad
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Estadística y probabilidad', 'BASICO', '¿Cuál es la media aritmética de 6, 8, 8, 10 y 12?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '8.8', TRUE),
        (v_pregunta_id, '8', FALSE),
        (v_pregunta_id, '9', FALSE),
        (v_pregunta_id, '10', FALSE);

    -- Pregunta 44 | Grado 3 | INTERMEDIO | Estadística y probabilidad
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Estadística y probabilidad', 'INTERMEDIO', 'Una bolsa contiene 5 fichas rojas, 3 azules y 2 verdes. Si se extrae una ficha al azar, ¿cuál es la probabilidad de que NO sea roja?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '3/10', FALSE),
        (v_pregunta_id, '1/2', TRUE),
        (v_pregunta_id, '7/10', FALSE),
        (v_pregunta_id, '2/5', FALSE);

    -- Pregunta 45 | Grado 3 | AVANZADO | Estadística y probabilidad
    INSERT INTO diagnostico_pregunta (materia_id, grado_escolar, tema, dificultad, enunciado)
    VALUES (v_materia_id, 3, 'Estadística y probabilidad', 'AVANZADO', 'Una bolsa contiene 3 fichas rojas y 3 azules. Si se extraen dos fichas sin reemplazo, ¿cuál es la probabilidad de que ambas sean rojas?')
    RETURNING id INTO v_pregunta_id;

    INSERT INTO diagnostico_opcion (pregunta_id, texto, es_correcta) VALUES
        (v_pregunta_id, '1/12', FALSE),
        (v_pregunta_id, '1/6', FALSE),
        (v_pregunta_id, '1/5', TRUE),
        (v_pregunta_id, '1/3', FALSE);

END $$;
