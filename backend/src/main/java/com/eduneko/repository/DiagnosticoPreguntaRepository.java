package com.eduneko.repository;

import com.eduneko.entity.DiagnosticoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticoPreguntaRepository extends JpaRepository<DiagnosticoPregunta, Long> {

    List<DiagnosticoPregunta>
        findByMateriaIdAndGradoEscolarAndActivoTrueOrderByIdAsc(
            Long materiaId,
            Short gradoEscolar
        );
}
