package com.eduneko.repository;

import com.eduneko.entity.DiagnosticoIntento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiagnosticoIntentoRepository
        extends JpaRepository<DiagnosticoIntento, Long> {

    List<DiagnosticoIntento>
        findByEstudianteIdOrderByFechaInicioDesc(
            Long estudianteId
        );

    Optional<DiagnosticoIntento>
        findFirstByEstudianteIdAndMateriaIdAndEstadoOrderByFechaInicioDesc(
            Long estudianteId,
            Long materiaId,
            String estado
        );
}
