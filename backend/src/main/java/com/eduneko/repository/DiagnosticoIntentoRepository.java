package com.eduneko.repository;

import com.eduneko.entity.DiagnosticoIntento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM DiagnosticoIntento i WHERE i.id = :intentoId")
    Optional<DiagnosticoIntento> findByIdParaFinalizar(
            @Param("intentoId") Long intentoId
        );
}
