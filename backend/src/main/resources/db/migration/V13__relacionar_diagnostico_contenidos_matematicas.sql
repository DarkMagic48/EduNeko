-- V13: Relacionar los temas del diagnóstico de Matemáticas
-- con los contenidos curriculares de la SEP.

-- Primero de secundaria
-- Tema 1: Fracciones y decimales

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    1,
    'Fracciones y decimales',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 1;

  -- Primero de secundaria
-- Tema 2: Geometría y medición

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    1,
    'Geometría y medición',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas'
  AND cc.orden IN (8, 9, 10, 11);

  -- Primero de secundaria
-- Tema 3: Números enteros y recta numérica

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    1,
    'Números enteros y recta numérica',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 2;

  -- Primero de secundaria
-- Tema 4: Operaciones básicas y jerarquía

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    1,
    'Operaciones básicas y jerarquía',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 3;

  -- Primero de secundaria
-- Tema 5: Patrones e introducción al álgebra

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    1,
    'Patrones e introducción al álgebra',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 1
WHERE m.nombre = 'Matemáticas'
  AND cc.orden IN (4, 5);

  -- Segundo de secundaria
-- Tema 1: Ecuaciones y resolución de problemas

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    2,
    'Ecuaciones y resolución de problemas',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 2
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 6;

  -- Segundo de secundaria
-- Tema 2: Geometría y medición

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    2,
    'Geometría y medición',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 2
WHERE m.nombre = 'Matemáticas'
  AND cc.orden IN (8, 9, 10, 11);

  -- Segundo de secundaria
-- Tema 3: Lenguaje y expresiones algebraicas

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    2,
    'Lenguaje y expresiones algebraicas',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 2
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 5;

  -- Segundo de secundaria
-- Tema 4: Números y operaciones

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    2,
    'Números y operaciones',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 2
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 3;

  -- Segundo de secundaria
-- Tema 5: Proporcionalidad y funciones

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    2,
    'Proporcionalidad y funciones',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 2
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 7;

  -- Tercero de secundaria
-- Tema 1: Ecuaciones y desigualdades

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    3,
    'Ecuaciones y desigualdades',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 3
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 6;

  -- Tercero de secundaria
-- Tema 2: Estadística y probabilidad

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    3,
    'Estadística y probabilidad',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 3
WHERE m.nombre = 'Matemáticas'
  AND cc.orden IN (12, 13, 14);

  -- Tercero de secundaria
-- Tema 3: Funciones y gráficas

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    3,
    'Funciones y gráficas',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 3
WHERE m.nombre = 'Matemáticas'
  AND cc.orden IN (7, 12);

  -- Tercero de secundaria
-- Tema 4: Geometría y medición

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    3,
    'Geometría y medición',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 3
WHERE m.nombre = 'Matemáticas'
  AND cc.orden IN (9, 10, 11);

  -- Tercero de secundaria
-- Tema 5: Operaciones y expresiones algebraicas

INSERT INTO diagnostico_tema_contenido (
    materia_id,
    grado_escolar,
    tema_diagnostico,
    contenido_curricular_grado_id
)
SELECT
    m.id,
    3,
    'Operaciones y expresiones algebraicas',
    ccg.id
FROM materia m
JOIN contenido_curricular cc
    ON cc.materia_id = m.id
JOIN contenido_curricular_grado ccg
    ON ccg.contenido_curricular_id = cc.id
   AND ccg.grado_escolar = 3
WHERE m.nombre = 'Matemáticas'
  AND cc.orden = 5;