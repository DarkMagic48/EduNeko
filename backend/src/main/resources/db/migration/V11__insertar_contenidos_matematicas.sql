-- V11: Insertar los contenidos curriculares de Matemáticas
-- Referencia: SEP, Programa Sintético de la Fase 6

INSERT INTO contenido_curricular (
    materia_id,
    nombre,
    orden,
    activo
)
SELECT
    m.id,
    contenidos.nombre,
    contenidos.orden,
    TRUE
FROM materia m
CROSS JOIN (
    VALUES
        (1, 'Expresión de fracciones como decimales y de decimales como fracciones'),
        (2, 'Extensión de los números a positivos y negativos y su orden'),
        (3, 'Extensión del significado de las operaciones y sus relaciones inversas'),
        (4, 'Regularidades y patrones'),
        (5, 'Introducción al álgebra'),
        (6, 'Ecuaciones lineales y cuadráticas'),
        (7, 'Funciones'),
        (8, 'Rectas y ángulos'),
        (9, 'Construcción y propiedades de las figuras planas y cuerpos'),
        (10, 'Circunferencia, círculo y esfera'),
        (11, 'Medición y cálculo en diferentes contextos'),
        (12, 'Obtención y representación de información'),
        (13, 'Interpretación de la información a través de medidas de tendencia central y de dispersión'),
        (14, 'Azar y probabilidad')
) AS contenidos(orden, nombre)
WHERE m.nombre = 'Matemáticas';