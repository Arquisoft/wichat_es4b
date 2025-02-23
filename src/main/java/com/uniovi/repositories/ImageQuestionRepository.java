package com.uniovi.repositories;

import com.uniovi.entities.Category;
import com.uniovi.entities.ImageQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ImageQuestionRepository extends CrudRepository<ImageQuestion, Long> {

    // Buscar por enunciado
    ImageQuestion findByStatement(String statement);

    // Obtener todas las preguntas de imagen
    List<ImageQuestion> findAll();

    // Obtener preguntas por idioma
    @Query("SELECT iq FROM ImageQuestion iq WHERE iq.language = ?1")
    Page<ImageQuestion> findByLanguage(Pageable pageable, String language);

    // Obtener preguntas por categoría e idioma
    @Query("SELECT iq FROM ImageQuestion iq WHERE iq.category = ?1 AND iq.language = ?2")
    Page<ImageQuestion> findByCategoryAndLanguage(Pageable pageable, Category category, String lang);

    // Buscar preguntas por enunciado e idioma (con búsqueda parcial)
    @Query("SELECT iq FROM ImageQuestion iq WHERE LOWER(iq.statement) LIKE LOWER(CONCAT('%', ?1, '%')) AND iq.language = ?2")
    Page<ImageQuestion> findByStatementAndLanguage(Pageable pageable, String statement, String language);
}

