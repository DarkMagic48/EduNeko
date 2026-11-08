-- =====================================================
-- V17: Control del estado de enriquecimiento visual
-- =====================================================
-- Permite distinguir entre lecciones pendientes,
-- recursos generados y recursos no aplicables o rechazados.
--
-- No modifica el contenido educativo ni el progreso.
-- =====================================================

-- 1. Agregar el estado del recurso visual.
ALTER TABLE leccion
ADD COLUMN estado_recurso_visual VARCHAR(20)
NOT NULL DEFAULT 'PENDIENTE';

-- 2. Reconocer los recursos visuales ya almacenados.
UPDATE leccion
SET estado_recurso_visual = 'GENERADO'
WHERE recurso_visual IS NOT NULL;

-- 3. Restringir los estados permitidos.
ALTER TABLE leccion
ADD CONSTRAINT chk_leccion_estado_recurso_visual
CHECK (
    estado_recurso_visual IN (
        'PENDIENTE',
        'GENERADO',
        'NO_APLICABLE',
        'RECHAZADO'
    )
);

-- 4. Mantener pendiente cualquier lección
-- que todavía no tenga un recurso visual.
-- No se hacen llamadas a OpenAI durante esta migración.