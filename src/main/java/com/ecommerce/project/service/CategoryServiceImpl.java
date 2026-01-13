package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{
    private List<Category> categories = new ArrayList<>();
    private Long nextId = 1L;
    @Override
    public List<Category> getAllCategoreis() {
       return categories;
    }

    @Override
    public void createCategory(Category category) {
        category.setCategoryId(nextId++);
        this.categories.add(category);
    }

    @Override
    public String deleteCategory(Long id) {
        Category category = categories.stream()
                .filter(c -> c.getCategoryId().equals(id))
                .findFirst().orElse(null);
        if(category == null){
            return "Category with id " + id + "not found";
        }
        categories.remove(category);
        return "Category with category id {}" + id + "deleted";
    }
}
