package com.uniovi.services;

import com.uniovi.entities.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CategoryService {

    /**
     * Add a new category to the database
     *
     * @param category Question to be added
     */
    void addNewCategory(Category category);

    /**
     * Get all the categories in the database
     *
     * @return A list with all the categories
     */
    List<Category> getAllCategories();

    /**
     * Get a category by its id
     *
     * @param id The id of the category
     * @return The category with the given id
     */
    Optional<Category> getCategory(Long id);

    Category getCategoryByName(String geography);
}
