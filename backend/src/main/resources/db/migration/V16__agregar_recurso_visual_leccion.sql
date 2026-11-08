-- EduNeko V1
-- Migración V16: Recursos visuales para las lecciones.
--
-- Agrega una columna JSONB para almacenar los datos
-- estructurados de los diagramas personalizados.
--
-- La columna permite NULL para mantener la compatibilidad
-- con las lecciones generadas anteriormente.

ALTER TABLE leccion
ADD COLUMN recurso_visual JSONB;