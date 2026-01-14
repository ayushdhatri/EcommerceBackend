package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{
    private CategoryRepository categoryRepository;
    private Long nextId = 1L;

    public CategoryServiceImpl(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategoreis() {
       return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long id) {
        List<Category> categories = categoryRepository.findAll();
        Category category = categories.stream()
                .filter(c -> c.getCategoryId().equals(id))
                .findFirst().orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource Not Found"));
        if(category == null){
            return "Category with id " + id + "not found";
        }
        categoryRepository.delete(category);
        return "Category with category id {}" + id + "deleted";
    }

    @Override
    public Category updateCategory(Category category, Long id) {
            List<Category> categories = categoryRepository.findAll();
           Optional<Category> savedCategory = categories.stream()
                   .filter(c -> c.getCategoryId().equals(id))
                   .findFirst();
           if(savedCategory.isPresent()) {

               savedCategory.get().setCategoryName(category.getCategoryName());
               return categoryRepository.save(savedCategory.get());
           }
           else{
               throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource Not Found");
           }

    }
}
