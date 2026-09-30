CREATE TABLE preferencia_aprendizaje (
    id BIGSERIAL PRIMARY KEY,

    estudiante_id BIGINT NOT NULL UNIQUE,

    formato_preferido VARCHAR(30) NOT NULL,

    longitud_explicacion VARCHAR(20) NOT NULL,

    dificultad_preferida VARCHAR(20) NOT NULL,

    ritmo_estudio VARCHAR(20) NOT NULL,

    CONSTRAINT fk_preferencia_estudiante
        FOREIGN KEY (estudiante_id)
        REFERENCES estudiante(id)
        ON DELETE CASCADE
);