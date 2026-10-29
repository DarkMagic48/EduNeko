-- V14: Estructura inicial de planes de aprendizaje personalizados

CREATE TABLE plan_aprendizaje (
    id BIGSERIAL PRIMARY KEY,

    estudiante_id BIGINT NOT NULL
        REFERENCES estudiante(id),

    materia_id BIGINT NOT NULL
        REFERENCES materia(id),

    grado_escolar SMALLINT NOT NULL
        CHECK (grado_escolar BETWEEN 1 AND 3),

    diagnostico_intento_id BIGINT
        REFERENCES diagnostico_intento(id),

    version INTEGER NOT NULL DEFAULT 1
        CHECK (version > 0),

    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
        CHECK (estado IN (
            'BORRADOR',
            'ACTIVO',
            'COMPLETADO',
            'SUSTITUIDO'
        )),

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (estudiante_id, materia_id, grado_escolar, version)
);

-- Evita múltiples planes activos para la misma materia y grado.
CREATE UNIQUE INDEX uq_plan_aprendizaje_activo
ON plan_aprendizaje (
    estudiante_id,
    materia_id,
    grado_escolar
)
WHERE estado = 'ACTIVO';


CREATE TABLE plan_unidad (
    id BIGSERIAL PRIMARY KEY,

    plan_aprendizaje_id BIGINT NOT NULL
        REFERENCES plan_aprendizaje(id) ON DELETE CASCADE,

    titulo VARCHAR(200) NOT NULL,

    descripcion TEXT,

    objetivo TEXT NOT NULL,

    tema_diagnostico VARCHAR(200),

    orden INTEGER NOT NULL
        CHECK (orden > 0),

    prioridad INTEGER NOT NULL
        CHECK (prioridad BETWEEN 1 AND 3),

    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE'
        CHECK (estado IN (
            'BLOQUEADA',
            'DISPONIBLE',
            'EN_PROGRESO',
            'COMPLETADA'
        )),

    progreso_porcentaje NUMERIC(5,2) NOT NULL DEFAULT 0
        CHECK (progreso_porcentaje BETWEEN 0 AND 100),

    UNIQUE (plan_aprendizaje_id, orden)
);


CREATE TABLE plan_unidad_pda (
    plan_unidad_id BIGINT NOT NULL
        REFERENCES plan_unidad(id) ON DELETE CASCADE,

    proceso_aprendizaje_id BIGINT NOT NULL
        REFERENCES proceso_aprendizaje(id),

    PRIMARY KEY (
        plan_unidad_id,
        proceso_aprendizaje_id
    )
);

CREATE INDEX idx_plan_unidad_plan
ON plan_unidad(plan_aprendizaje_id);