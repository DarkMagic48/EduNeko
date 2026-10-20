
package com.eduneko.repository;

import com.eduneko.entity.DiagnosticoResultadoTema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticoResultadoTemaRepository
        extends JpaRepository<DiagnosticoResultadoTema, Long> {

    List<DiagnosticoResultadoTema> findByIntentoIdOrderByTemaAsc(
            Long intentoId
    );
}
