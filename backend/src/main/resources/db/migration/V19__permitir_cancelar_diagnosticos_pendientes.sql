-- Conservar intentos de diagnósticos anteriores cuando el estudiante
-- modifica su grado escolar. No se elimina ningún registro histórico.
ALTER TABLE diagnostico_intento
    DROP CONSTRAINT chk_diagnostico_intento_estado;

ALTER TABLE diagnostico_intento
    ADD CONSTRAINT chk_diagnostico_intento_estado
    CHECK (estado IN ('EN_PROGRESO', 'FINALIZADO', 'CANCELADO'));
