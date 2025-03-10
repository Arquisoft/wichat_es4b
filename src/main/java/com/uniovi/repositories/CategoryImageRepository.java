package com.uniovi.repositories;

import com.uniovi.entities.Category;
import com.uniovi.entities.CategoryImage;
import org.springframework.data.repository.CrudRepository;

public interface CategoryImageRepository extends CrudRepository<CategoryImage, Long> {

    CategoryImage findByName(String name);

}
