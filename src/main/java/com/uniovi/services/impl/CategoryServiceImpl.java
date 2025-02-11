package com.uniovi.services.impl;

import com.uniovi.entities.Category;
import com.uniovi.repositories.CategoryRepository;
import com.uniovi.services.CategoryService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void addNewCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> l = new ArrayList<>();
        categoryRepository.findAll().forEach(l::add);
        return l;
    }

    @Override
    public Optional<Category> getCategory(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category getCategoryByName(String name) {
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
                addNewCategory(new Category(category.getKey(), category.getValue()));
            }
        }
    }
}