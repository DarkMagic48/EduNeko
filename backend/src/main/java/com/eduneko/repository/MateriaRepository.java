package com.eduneko.repository;

import com.eduneko.entity.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

    @Query(value = """
            SELECT m.*
            FROM materia m
            INNER JOIN materia_grado mg ON mg.materia_id = m.id
            WHERE mg.grado_escolar = :grado
              AND m.activo = true
            ORDER BY m.id
            """, nativeQuery = true)
    List<Materia> findActivasByGrado(@Param("grado") Short grado); 
    
}
