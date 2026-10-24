-- 1. Catálogo de contenidos curriculares por materia
CREATE TABLE contenido_curricular (
    id BIGSERIAL PRIMARY KEY,
    materia_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    orden INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_contenido_curricular_materia
        FOREIGN KEY (materia_id)
        REFERENCES materia(id),

    CONSTRAINT uq_contenido_curricular
        UNIQUE (materia_id, nombre),

    CONSTRAINT uq_contenido_curricular_id_materia
        UNIQUE (id, materia_id),

    CONSTRAINT chk_contenido_curricular_orden
        CHECK (orden > 0)
);


-- 2. Relación de contenidos curriculares con grados escolares
CREATE TABLE contenido_curricular_grado (
    id BIGSERIAL PRIMARY KEY,
    contenido_curricular_id BIGINT NOT NULL,
    materia_id BIGINT NOT NULL,
    grado_escolar SMALLINT NOT NULL,

    CONSTRAINT fk_curricular_grado_contenido
        FOREIGN KEY (contenido_curricular_id, materia_id)
        REFERENCES contenido_curricular(id, materia_id),

    CONSTRAINT fk_curricular_grado_materia
        FOREIGN KEY (materia_id, grado_escolar)
        REFERENCES materia_grado(materia_id, grado_escolar),

    CONSTRAINT uq_contenido_curricular_grado
        UNIQUE (contenido_curricular_id, grado_escolar),

    CONSTRAINT uq_curricular_grado_identidad
        UNIQUE (id, materia_id, grado_escolar)
);


-- 3. Procesos de Desarrollo de Aprendizaje (PDA)
CREATE TABLE proceso_aprendizaje (
    id BIGSERIAL PRIMARY KEY,
    contenido_curricular_grado_id BIGINT NOT NULL,
    descripcion TEXT NOT NULL,
    orden INTEGER NOT NULL,

    CONSTRAINT fk_proceso_aprendizaje_contenido_grado
        FOREIGN KEY (contenido_curricular_grado_id)
        REFERENCES contenido_curricular_grado(id),

    CONSTRAINT uq_proceso_aprendizaje_orden
        UNIQUE (contenido_curricular_grado_id, orden),

    CONSTRAINT chk_proceso_aprendizaje_orden
        CHECK (orden > 0)
);


-- 4. Relación entre temas diagnósticos y contenidos curriculares
CREATE TABLE diagnostico_tema_contenido (
    id BIGSERIAL PRIMARY KEY,
    materia_id BIGINT NOT NULL,
    grado_escolar SMALLINT NOT NULL,
    tema_diagnostico VARCHAR(100) NOT NULL,
    contenido_curricular_grado_id BIGINT NOT NULL,

    CONSTRAINT fk_diagnostico_tema_materia_grado
        FOREIGN KEY (materia_id, grado_escolar)
        REFERENCES materia_grado(materia_id, grado_escolar),

    CONSTRAINT fk_diagnostico_tema_contenido_grado
        FOREIGN KEY (
            contenido_curricular_grado_id,
            materia_id,
            grado_escolar
        )
        REFERENCES contenido_curricular_grado(
            id,
            materia_id,
            grado_escolar
        ),

    CONSTRAINT uq_diagnostico_tema_contenido
        UNIQUE (
            materia_id,
            grado_escolar,
            tema_diagnostico,
            contenido_curricular_grado_id
        )
);