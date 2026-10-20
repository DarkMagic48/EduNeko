
package com.eduneko.repository;

import com.eduneko.entity.DiagnosticoRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticoRespuestaRepository
        extends JpaRepository<DiagnosticoRespuesta, Long> {

    List<DiagnosticoRespuesta> findByIntentoIdOrderByPreguntaIdAsc(
            Long intentoId
    );

    boolean existsByIntentoIdAndPreguntaId(
            Long intentoId,
            Long preguntaId
    );
}
