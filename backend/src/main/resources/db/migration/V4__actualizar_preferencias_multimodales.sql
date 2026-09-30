-- Renombrar longitud_explicacion a nivel_explicacion
ALTER TABLE preferencia_aprendizaje
    RENAME COLUMN longitud_explicacion TO nivel_explicacion;


-- Actualizar los valores existentes del nivel de explicación
UPDATE preferencia_aprendizaje
SET nivel_explicacion =
    CASE nivel_explicacion
        WHEN 'CORTA' THEN 'BREVE'
        WHEN 'MEDIA' THEN 'EQUILIBRADA'
        WHEN 'DETALLADA' THEN 'DETALLADA'
        ELSE nivel_explicacion
    END;


-- Renombrar dificultad_preferida a nivel_reto_preferido
ALTER TABLE preferencia_aprendizaje
    RENAME COLUMN dificultad_preferida TO nivel_reto_preferido;


-- Actualizar los valores existentes del nivel de reto
UPDATE preferencia_aprendizaje
SET nivel_reto_preferido =
    CASE nivel_reto_preferido
        WHEN 'BASICA' THEN 'BAJO'
        WHEN 'INTERMEDIA' THEN 'EQUILIBRADO'
        WHEN 'RETADORA' THEN 'ALTO'
        ELSE nivel_reto_preferido
    END;


-- Agregar las modalidades de aprendizaje preferidas
ALTER TABLE preferencia_aprendizaje
    ADD COLUMN prefiere_lectura_escritura BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN prefiere_visual BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN prefiere_auditivo BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN prefiere_practica BOOLEAN NOT NULL DEFAULT FALSE;


-- Eliminar el formato de explicación anterior
ALTER TABLE preferencia_aprendizaje
    DROP COLUMN formato_preferido;