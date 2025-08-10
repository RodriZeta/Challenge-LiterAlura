package com.gutendex.repository;

import com.gutendex.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findFirstByNombreIgnoreCase(String nombre);

    @Query("SELECT a FROM Autor a WHERE a.anioNacimiento IS NOT NULL AND a.anioNacimiento <= :anio AND (a.anioFallecimiento IS NULL OR a.anioFallecimiento > :anio)")
    List<Autor> autoresVivosEn(int anio);
}
