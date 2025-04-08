package com.uniovi.repositories;

import com.uniovi.entities.Category;
import com.uniovi.entities.abstracts.AbstractAnswer;
import com.uniovi.entities.abstracts.AbstractQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
@NoRepositoryBean
public interface QuestionBaseRepository<T extends AbstractQuestion<?>> extends CrudRepository<T, Long> {
    T findByStatement(String statement);

    List<T> findAll();

    @Query("SELECT q FROM #{#entityName} q WHERE q.language = ?1")
    Page<T> findByLanguage(Pageable pageable, String language);

    @Query("SELECT q FROM #{#entityName} q WHERE q.category = ?1 AND q.language = ?2")
    Page<T> findByCategoryAndLanguage(Pageable pageable, Category category, String lang);

    @Query("SELECT q FROM #{#entityName} q WHERE LOWER(q.statement) LIKE LOWER(CONCAT('%', ?1, '%')) AND q.language = ?2")
    Page<T> findByStatementAndLanguage(Pageable pageable, String statement, String language);
}
