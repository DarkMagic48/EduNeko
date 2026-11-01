-- V15: Lecciones y actividades de aprendizaje

CREATE TABLE leccion (
    id BIGSERIAL PRIMARY KEY,
    plan_unidad_id BIGINT NOT NULL
        REFERENCES plan_unidad(id) ON DELETE CASCADE,
    titulo VARCHAR(200) NOT NULL,
    introduccion TEXT NOT NULL,
    explicacion TEXT NOT NULL,
    ejemplo_resuelto TEXT NOT NULL,
    orden INTEGER NOT NULL CHECK (orden > 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE'
        CHECK (estado IN (
            'BLOQUEADA',
            'DISPONIBLE',
            'EN_PROGRESO',
            'COMPLETADA'
        )),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (plan_unidad_id, orden)
);

CREATE TABLE actividad (
    id BIGSERIAL PRIMARY KEY,
    leccion_id BIGINT NOT NULL
        REFERENCES leccion(id) ON DELETE CASCADE,
    pregunta TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'OPCION_MULTIPLE'
        CHECK (tipo IN (
            'OPCION_MULTIPLE',
            'RESPUESTA_NUMERICA'
        )),
    opciones JSONB,
    respuesta_correcta TEXT NOT NULL,
    explicacion_respuesta TEXT,
    orden INTEGER NOT NULL CHECK (orden > 0),
    UNIQUE (leccion_id, orden)
);

CREATE TABLE actividad_intento (
    id BIGSERIAL PRIMARY KEY,
    actividad_id BIGINT NOT NULL
        REFERENCES actividad(id) ON DELETE CASCADE,
    estudiante_id BIGINT NOT NULL
        REFERENCES estudiante(id),
    respuesta_estudiante TEXT NOT NULL,
    es_correcta BOOLEAN NOT NULL,
    puntos_obtenidos INTEGER NOT NULL DEFAULT 0
        CHECK (puntos_obtenidos >= 0),
    fecha_respuesta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leccion_unidad
    ON leccion(plan_unidad_id);

CREATE INDEX idx_actividad_leccion
    ON actividad(leccion_id);

CREATE INDEX idx_actividad_intento_estudiante
    ON actividad_intento(estudiante_id);
