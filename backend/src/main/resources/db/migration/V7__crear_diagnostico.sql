
CREATE TABLE diagnostico_pregunta (
    id BIGSERIAL PRIMARY KEY,
    materia_id BIGINT NOT NULL,
    grado_escolar SMALLINT NOT NULL,
    tema VARCHAR(100) NOT NULL,
    dificultad VARCHAR(20) NOT NULL,
    enunciado TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_diagnostico_pregunta_materia
        FOREIGN KEY (materia_id)
        REFERENCES materia(id),

    CONSTRAINT chk_diagnostico_pregunta_grado
        CHECK (grado_escolar BETWEEN 1 AND 3),

    CONSTRAINT chk_diagnostico_pregunta_dificultad
        CHECK (dificultad IN ('BASICO', 'INTERMEDIO', 'AVANZADO'))
);

CREATE TABLE diagnostico_opcion (
    id BIGSERIAL PRIMARY KEY,
    pregunta_id BIGINT NOT NULL,
    texto TEXT NOT NULL,
    es_correcta BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_diagnostico_opcion_pregunta
        FOREIGN KEY (pregunta_id)
        REFERENCES diagnostico_pregunta(id)
);

CREATE TABLE diagnostico_intento (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL,
    materia_id BIGINT NOT NULL,
    grado_escolar SMALLINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'EN_PROGRESO',
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP,
    total_preguntas INTEGER,
    total_aciertos INTEGER,

    CONSTRAINT fk_diagnostico_intento_estudiante
        FOREIGN KEY (estudiante_id)
        REFERENCES estudiante(id),

    CONSTRAINT fk_diagnostico_intento_materia
        FOREIGN KEY (materia_id)
        REFERENCES materia(id),

    CONSTRAINT chk_diagnostico_intento_grado
        CHECK (grado_escolar BETWEEN 1 AND 3),

    CONSTRAINT chk_diagnostico_intento_estado
        CHECK (estado IN ('EN_PROGRESO', 'FINALIZADO'))
);

CREATE TABLE diagnostico_respuesta (
    id BIGSERIAL PRIMARY KEY,
    intento_id BIGINT NOT NULL,
    pregunta_id BIGINT NOT NULL,
    opcion_id BIGINT NOT NULL,

    CONSTRAINT fk_diagnostico_respuesta_intento
        FOREIGN KEY (intento_id)
        REFERENCES diagnostico_intento(id),

    CONSTRAINT fk_diagnostico_respuesta_pregunta
        FOREIGN KEY (pregunta_id)
        REFERENCES diagnostico_pregunta(id),

    CONSTRAINT fk_diagnostico_respuesta_opcion
        FOREIGN KEY (opcion_id)
        REFERENCES diagnostico_opcion(id),

    CONSTRAINT uq_diagnostico_respuesta_intento_pregunta
        UNIQUE (intento_id, pregunta_id)
);

CREATE TABLE diagnostico_resultado_tema (
    id BIGSERIAL PRIMARY KEY,
    intento_id BIGINT NOT NULL,
    tema VARCHAR(100) NOT NULL,
    total_preguntas INTEGER NOT NULL,
    total_aciertos INTEGER NOT NULL,

    CONSTRAINT fk_diagnostico_resultado_intento
        FOREIGN KEY (intento_id)
        REFERENCES diagnostico_intento(id),

    CONSTRAINT uq_diagnostico_resultado_intento_tema
        UNIQUE (intento_id, tema),

    CONSTRAINT chk_diagnostico_resultado_aciertos
        CHECK (
            total_preguntas > 0
            AND total_aciertos BETWEEN 0 AND total_preguntas
        )
);
