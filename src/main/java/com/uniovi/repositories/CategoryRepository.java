package com.uniovi.repositories;

import com.uniovi.entities.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    Category findByName(String name);

}
