-- V12: Relaciones curriculares y PDA de Matemáticas
-- Primera parte: contenidos por grado

INSERT INTO contenido_curricular_grado (
    contenido_curricular_id,
    materia_id,
    grado_escolar
)
SELECT
    cc.id,
    cc.materia_id,
    relaciones.grado_escolar
FROM contenido_curricular cc
JOIN materia m
    ON m.id = cc.materia_id
JOIN (
    VALUES
        -- Primero de secundaria: contenidos 1 al 14
        (1, 1), (1, 2), (1, 3), (1, 4),
        (1, 5), (1, 6), (1, 7), (1, 8),
        (1, 9), (1, 10), (1, 11), (1, 12),
        (1, 13), (1, 14),

        -- Segundo de secundaria
        (2, 3), (2, 4), (2, 5), (2, 6),
        (2, 7), (2, 8), (2, 9), (2, 10),
        (2, 11), (2, 12), (2, 13), (2, 14),

        -- Tercero de secundaria
        (3, 5), (3, 6), (3, 7),
        (3, 9), (3, 10), (3, 11),
        (3, 12), (3, 13), (3, 14)
) AS relaciones(grado_escolar, orden_contenido)
    ON cc.orden = relaciones.orden_contenido
WHERE m.nombre = 'Matemáticas';

-- Segunda parte: PDA de primero de secundaria
-- Contenidos 1, 2 y 3

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        (1, 1, 'Usa diversas estrategias al convertir números fraccionarios a decimales y viceversa.'),

        (2, 1, 'Reconoce la necesidad de los números negativos a partir de usar cantidades que tienen al cero como referencia.'),
        (2, 2, 'Compara y ordena números con signo (enteros, fracciones y decimales) en la recta numérica y analiza en qué casos se cumple la propiedad de densidad.'),

        (3, 1, 'Reconoce el significado de las cuatro operaciones básicas y sus relaciones inversas al resolver problemas que impliquen el uso de números con signo.'),
        (3, 2, 'Comprueba y argumenta si cada una de estas operaciones cumple las propiedades: conmutativa, asociativa y distributiva.'),
        (3, 3, 'Identifica y aplica la jerarquía de operaciones y símbolos de agrupación al realizar cálculos.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas';

-- PDA de primero de secundaria
-- Contenidos 4, 5, 6 y 7

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        (4, 1, 'Representa algebraicamente una sucesión con progresión aritmética de figuras y números.'),

        (5, 1, 'Interpreta y plantea diversas situaciones del lenguaje común al lenguaje algebraico y viceversa.'),
        (5, 2, 'Representa algebraicamente perímetros de figuras.'),

        (6, 1, 'Resuelve ecuaciones de la forma Ax=B, Ax+B=C, Ax+B=Cx+D con el uso de las propiedades de la igualdad.'),
        (6, 2, 'Modela y resuelve problemas cuyo planteamiento es una ecuación lineal.'),
        (6, 3, 'Resuelve problemas de porcentajes en diversas situaciones.'),

        (7, 1, 'Relaciona e interpreta relaciones proporcional y no proporcional a partir de su representación tabular, gráfica y con diagramas.'),
        (7, 2, 'Modela y resuelve diversas situaciones a través de ecuaciones proporcionales con constante positiva y negativa.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas';

-- PDA de primero de secundaria
-- Contenidos 8, 9 y 10

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        -- Contenido 8: Rectas y ángulos
        (8, 1, 'Explora las figuras básicas como rectas y ángulos y su notación.'),
        (8, 2, 'Encuentra y calcula los ángulos que se forman al intersecar dos segmentos.'),

        -- Contenido 9: Construcción y propiedades de las figuras planas y cuerpos
        (9, 1, 'Utiliza la regla y el compás para trazar: punto medio, mediatriz de un segmento, segmentos y ángulos congruentes, bisectriz de un ángulo, rectas perpendiculares y rectas paralelas.'),
        (9, 2, 'Identifica y traza las rectas notables en triángulos y cuadriláteros.'),
        (9, 3, 'Construye y clasifica triángulos y cuadriláteros a partir del análisis de distinta información.'),

        -- Contenido 10: Circunferencia, círculo y esfera
        (10, 1, 'Identifica y traza las rectas notables en la circunferencia y las relaciones entre ellas.'),
        (10, 2, 'Investiga figuras relacionadas con círculos y propiedades de los círculos.'),
        (10, 3, 'Construye circunferencias a partir de distinta información.'),
        (10, 4, 'Verifica los criterios de existencia y unicidad de estas figuras.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas';

-- PDA de primero de secundaria
-- Contenidos 11, 12, 13 y 14

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        -- Contenido 11: Medición y cálculo
        (11, 1, 'Introduce la idea de distancia entre dos puntos como la longitud del segmento que los une.'),
        (11, 2, 'Encuentra la distancia de un punto a una recta y la distancia entre dos rectas paralelas.'),
        (11, 3, 'Explora la desigualdad del triángulo.'),
        (11, 4, 'Obtiene y aplica fórmulas o usa otras estrategias para calcular el perímetro y el área de polígonos regulares e irregulares y del círculo.'),

        -- Contenido 12: Obtención y representación de información
        (12, 1, 'Usa tablas, gráficas de barras y circulares para el análisis de información.'),

        -- Contenido 13: Medidas de tendencia central y dispersión
        (13, 1, 'Determina e interpreta la frecuencia absoluta, la frecuencia relativa, la media, la mediana y la moda en un conjunto de datos.'),
        (13, 2, 'Usa e interpreta las medidas de tendencia central (moda, media aritmética y mediana) y el rango de un conjunto de datos, y justifica con base en ellas sus decisiones.'),

        -- Contenido 14: Azar y probabilidad
        (14, 1, 'Compara cualitativamente dos o más eventos a partir de sus resultados posibles, usa relaciones como: “es más probable que…”, “es menos probable que…”.'),
        (14, 2, 'Identifica eventos en los que interviene el azar, determina el espacio muestral y experimenta.'),
        (14, 3, 'Identifica diversos procedimientos de conteo y resuelve problemas.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas';

-- PDA de segundo de secundaria
-- Contenidos 3, 4, 5, 6 y 7

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        -- Contenido 3: Operaciones y relaciones inversas
        (3, 1, 'Usa criterios de divisibilidad y números primos al resolver problemas que implican calcular el máximo común divisor y el mínimo común múltiplo.'),
        (3, 2, 'Calcula potencias con exponente entero y la raíz cuadrada. Usa la notación científica.'),
        (3, 3, 'Usa la notación científica al realizar cálculos con cantidades muy grandes o muy pequeñas.'),

        -- Contenido 4: Regularidades y patrones
        (4, 1, 'Representa algebraicamente una sucesión con progresión cuadrática de figuras y números.'),

        -- Contenido 5: Introducción al álgebra
        (5, 1, 'Representa algebraicamente áreas que generan una expresión cuadrática.'),
        (5, 2, 'Identifica y usa las propiedades de los exponentes al resolver distintas operaciones algebraicas.'),

        -- Contenido 6: Ecuaciones lineales y cuadráticas
        (6, 1, 'Resuelve desigualdades con expresiones algebraicas.'),
        (6, 2, 'Modela y soluciona sistemas de dos ecuaciones lineales con dos incógnitas por algún método para dar respuesta a un problema.'),

        -- Contenido 7: Funciones
        (7, 1, 'Relaciona e interpreta la proporcionalidad inversa de dos magnitudes o cantidades, además usa una tabla, gráfica o representación algebraica en diversos contextos.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 2
WHERE m.nombre = 'Matemáticas';

-- PDA de segundo de secundaria
-- Contenidos 8, 9 y 10

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        -- Contenido 8: Rectas y ángulos
        (8, 1, 'Identifica y usa las relaciones entre los ángulos, lados y diagonales para construir a escala triángulos, cuadriláteros y polígonos regulares o irregulares.'),

        -- Contenido 9: Construcción y propiedades de las figuras planas y cuerpos
        (9, 1, 'Construye con regla y compás polígonos regulares con distinta información.'),
        (9, 2, 'Identifica y usa las relaciones entre figuras en la construcción de teselados.'),

        -- Contenido 10: Circunferencia, círculo y esfera
        (10, 1, 'Determina la medida de ángulos inscritos y centrales, así como de arcos de circunferencia.'),
        (10, 2, 'Explora las intersecciones entre círculos y figuras al calcular perímetros y áreas.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 2
WHERE m.nombre = 'Matemáticas';

-- PDA de segundo de secundaria
-- Contenidos 11, 12, 13 y 14

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        -- Contenido 11: Medición y cálculo
        (11, 1, 'Resuelve problemas que implican conversiones en múltiplos y submúltiplos del metro, litro, kilogramo y de unidades del sistema inglés (yarda, pulgada, galón, onza y libra).'),
        (11, 2, 'Utiliza estrategias diversas para determinar el perímetro y el área de figuras compuestas.'),

        -- Contenido 12: Obtención y representación de información
        (12, 1, 'Recolecta, registra, lee y comunica información mediante histogramas, gráficas poligonales y de línea.'),

        -- Contenido 13: Medidas de tendencia central y dispersión
        (13, 1, 'Usa e interpreta las medidas de tendencia central (moda, media aritmética y mediana) y de dispersión (rango y la desviación media) de un conjunto de datos, y justifica con base en ellas sus decisiones.'),
        (13, 2, 'Identifica tendencias en los datos centrándose en sus valores representativos y sus variaciones.'),

        -- Contenido 14: Azar y probabilidad
        (14, 1, 'Realiza experimentos aleatorios y registra los resultados en una tabla de frecuencia como la transición de la probabilidad frecuencial a la teórica.'),
        (14, 2, 'Analiza las características de la medida de la probabilidad y su equivalencia y representación en números decimales, fraccionarios y porcentajes.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 2
WHERE m.nombre = 'Matemáticas';

-- PDA de tercero de secundaria
-- Contenidos 5, 6 y 7

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        -- Contenido 5: Introducción al álgebra
        (5, 1, 'Representa algebraicamente áreas y volúmenes de cuerpos geométricos y calcula el valor de una variable en función de las otras.'),

        -- Contenido 6: Ecuaciones lineales y cuadráticas
        (6, 1, 'Resuelve ecuaciones de la forma Ax²+Bx+C=0 por factorización y fórmula general.'),
        (6, 2, 'Resuelve problemas cuyo planteamiento es una ecuación cuadrática.'),

        -- Contenido 7: Funciones
        (7, 1, 'Relaciona e interpreta la variación de dos cantidades a partir de su representación tabular, gráfica y algebraica.'),
        (7, 2, 'Explora diversos procedimientos para resolver problemas de reparto proporcional.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 3
WHERE m.nombre = 'Matemáticas';

-- PDA de tercero de secundaria
-- Contenidos 9 y 10

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        -- Contenido 9: Construcción y propiedades de las figuras planas y cuerpos
        (9, 1, 'Aplica las propiedades de la congruencia y semejanza de triángulos al construir y resolver problemas.'),
        (9, 2, 'Reconoce las propiedades de los sólidos.'),
        (9, 3, 'Explora la generación de sólidos de revolución a partir de figuras planas.'),
        (9, 4, 'Explora y construye desarrollos planos de diferentes figuras tridimensionales, cilindros, pirámides y conos.'),

        -- Contenido 10: Circunferencia, círculo y esfera
        (10, 1, 'Explora y construye desarrollos planos de esferas.'),
        (10, 2, 'Indaga la generación de esferas a partir de figuras planas.'),
        (10, 3, 'Encuentra relaciones de volumen de la esfera, el cono y el cilindro.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 3
WHERE m.nombre = 'Matemáticas';

-- PDA de tercero de secundaria
-- Contenidos 11, 12, 13 y 14

INSERT INTO proceso_aprendizaje (
    contenido_curricular_grado_id,
    descripcion,
    orden
)
SELECT
    ccg.id,
    pda.descripcion,
    pda.orden_pda
FROM (
    VALUES
        -- Contenido 11: Medición y cálculo
        (11, 1, 'Usa diferentes estrategias para calcular el volumen de prismas, pirámides y cilindros.'),
        (11, 2, 'Formula, justifica y usa el teorema de Pitágoras al resolver problemas.'),
        (11, 3, 'Resuelve problemas utilizando las razones trigonométricas seno, coseno y tangente.'),

        -- Contenido 12: Obtención y representación de información
        (12, 1, 'Lee, interpreta y comunica información de cualquier tipo de gráficas.'),

        -- Contenido 13: Medidas de tendencia central y dispersión
        (13, 1, 'Determina y compara las medidas de tendencia central (media, mediana y moda) y de dispersión (rango y desviación media) de dos conjuntos de datos para tomar decisiones.'),

        -- Contenido 14: Azar y probabilidad
        (14, 1, 'Resuelve problemas donde se analicen las características de eventos complementarios y eventos mutuamente excluyentes e independientes.'),
        (14, 2, 'Resuelve problemas donde se calcule la probabilidad de ocurrencia de dos eventos mutuamente excluyentes y de eventos complementarios (regla de la suma).'),
        (14, 3, 'Resuelve problemas donde se calcule la probabilidad de ocurrencia de dos eventos independientes (regla del producto).'),
        (14, 4, 'Indaga las condiciones necesarias para que un juego de azar sea justo, con base en la noción de resultados equiprobables y no equiprobables.')
) AS pda(orden_contenido, orden_pda, descripcion)
JOIN contenido_curricular cc
    ON cc.orden = pda.orden_contenido
JOIN materia m
    ON m.id = cc.materia_id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 3
WHERE m.nombre = 'Matemáticas';