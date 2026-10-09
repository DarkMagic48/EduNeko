-- 1. Crear catálogo de materias
CREATE TABLE materia (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);


-- 2. Relacionar las materias con los grados escolares
CREATE TABLE materia_grado (
    materia_id BIGINT NOT NULL,
    grado_escolar SMALLINT NOT NULL,

    PRIMARY KEY (materia_id, grado_escolar),

    CONSTRAINT fk_materia_grado_materia
        FOREIGN KEY (materia_id)
        REFERENCES materia(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_materia_grado
        CHECK (grado_escolar BETWEEN 1 AND 3)
);


-- 3. Registrar las materias seleccionadas por cada estudiante
CREATE TABLE estudiante_materia (
    estudiante_id BIGINT NOT NULL,
    materia_id BIGINT NOT NULL,

    PRIMARY KEY (estudiante_id, materia_id),

    CONSTRAINT fk_estudiante_materia_estudiante
        FOREIGN KEY (estudiante_id)
        REFERENCES estudiante(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_estudiante_materia_materia
        FOREIGN KEY (materia_id)
        REFERENCES materia(id)
        ON DELETE CASCADE
);


-- 4. Insertar catálogo inicial de EduNeko V1
INSERT INTO materia (nombre) VALUES
    ('Matemáticas'),
    ('Español'),
    ('Biología'),
    ('Física'),
    ('Química');


-- 5. Definir las materias disponibles por grado
INSERT INTO materia_grado (materia_id, grado_escolar)
SELECT id, 1 FROM materia
WHERE nombre IN ('Matemáticas', 'Español', 'Biología');

INSERT INTO materia_grado (materia_id, grado_escolar)
SELECT id, 2 FROM materia
WHERE nombre IN ('Matemáticas', 'Español', 'Física');

INSERT INTO materia_grado (materia_id, grado_escolar)
SELECT id, 3 FROM materia
WHERE nombre IN ('Matemáticas', 'Español', 'Química');