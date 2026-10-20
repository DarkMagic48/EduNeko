CREATE UNIQUE INDEX uq_diagnostico_intento_en_progreso
ON diagnostico_intento (estudiante_id, materia_id)
WHERE estado = 'EN_PROGRESO';
