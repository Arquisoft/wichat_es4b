package com.uniovi.repositories;

import com.uniovi.entities.Category;
import com.uniovi.entities.QuestionImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionImageRepository extends CrudRepository<QuestionImage, Long> {
    QuestionImage findByStatement(String statement);
    List<QuestionImage> findAll();

    @Query("SELECT q FROM QuestionImage q WHERE q.language = ?1")
    Page<QuestionImage> findByLanguage(Pageable pageable, String language);

    @Query("SELECT q FROM QuestionImage q WHERE q.category = ?1 AND q.language = ?2")
    Page<QuestionImage> findByCategoryAndLanguage(Pageable pageable, Category category, String lang);

    @Query("SELECT q FROM QuestionImage q WHERE LOWER(q.statement) LIKE LOWER(CONCAT('%', ?1, '%')) AND q.language = ?2")
    Page<QuestionImage> findByStatementAndLanguage(Pageable pageable, String statement, String language);
}
