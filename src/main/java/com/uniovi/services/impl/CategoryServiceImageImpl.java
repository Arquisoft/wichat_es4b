package com.uniovi.services.impl;

import com.uniovi.entities.Category;
import com.uniovi.entities.CategoryImage;
import com.uniovi.repositories.CategoryImageRepository;
import com.uniovi.repositories.CategoryRepository;
import com.uniovi.services.CategoryService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryServiceImageImpl {

    private final CategoryImageRepository categoryRepository;

    public CategoryServiceImageImpl(CategoryImageRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    public void addNewCategory(CategoryImage category) {
        categoryRepository.save(category);
    }


    public List<CategoryImage> getAllCategories() {
        List<CategoryImage> l = new ArrayList<>();
        categoryRepository.findAll().forEach(l::add);
        return l;
    }


    public Optional<CategoryImage> getCategory(Long id) {
        return categoryRepository.findById(id);
    }


    public CategoryImage getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    private static final Map.Entry<String, String>[] CATEGORIES = new AbstractMap.SimpleEntry[] {
            new AbstractMap.SimpleEntry<>("Geography", "Questions about geography")
    };

    @PostConstruct
    public void init() {
        // Add categories, ensuring there's only 1 of them always
        for (Map.Entry<String, String> category : CATEGORIES) {
            if (categoryRepository.findByName(category.getKey())==null) {
                addNewCategory(new CategoryImage(category.getKey(), category.getValue()));
            }
        }
    }
}