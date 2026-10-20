package com.eduneko.repository;

import com.eduneko.entity.DiagnosticoOpcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticoOpcionRepository extends JpaRepository<DiagnosticoOpcion, Long> {

    List<DiagnosticoOpcion> findByPreguntaIdOrderByIdAsc(
            Long preguntaId
    );
}
