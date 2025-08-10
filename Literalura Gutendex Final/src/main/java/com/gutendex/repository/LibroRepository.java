package com.gutendex.repository;

import com.gutendex.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Optional<Libro> findFirstByTituloIgnoreCase(String titulo);
    List<Libro> findByIdiomaIgnoreCase(String idioma);

    @Query("SELECT l FROM Libro l ORDER BY l.descargas DESC LIMIT 10")
    List<Libro> top10PorDescargas();
}
