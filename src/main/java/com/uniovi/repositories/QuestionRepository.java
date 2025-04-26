package com.uniovi.repositories;

import com.uniovi.entities.Category;
import com.uniovi.entities.Question;
import com.uniovi.entities.QuestionImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface QuestionRepository extends CrudRepository<Question, Long> {
    Question findByStatement(String statement);

    List<Question> findAll();

    @Query("SELECT q FROM QuestionImage q WHERE q.language = ?1")
    Page<Question> findByLanguage(Pageable pageable, String language);

    @Query("SELECT q FROM QuestionImage q WHERE q.category = ?1 AND q.language = ?2")
    Page<Question> findByCategoryAndLanguage(Pageable pageable, Category category, String lang);

    @Query("SELECT q FROM Question q WHERE LOWER(q.statement) LIKE LOWER(CONCAT('%', ?1, '%')) AND q.language = ?2")
    Page<Question> findByStatementAndLanguage(Pageable pageable, String statement, String language);
}
