
-- =====================================================
-- EDUNEKO V1 - MIGRACIÓN V18
--
-- Agrega una columna para almacenar las pistas
-- pedagógicas generadas por IA para cada actividad.
--
-- Se permite NULL para conservar la compatibilidad
-- con las actividades creadas anteriormente.
-- =====================================================

ALTER TABLE actividad
ADD COLUMN pista_pedagogica TEXT;
