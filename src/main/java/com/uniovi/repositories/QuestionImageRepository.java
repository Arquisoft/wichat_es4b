package com.uniovi.repositories;

import com.uniovi.entities.Category;
import com.uniovi.entities.QuestionImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionImageRepository extends QuestionBaseRepository<QuestionImage> {
}
