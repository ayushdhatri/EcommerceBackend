package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
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
    public CategoryServiceImpl(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategoreis() {
        List<Category> allCategories = categoryRepository.findAll();
        if(allCategories.isEmpty()){
            throw new APIException("There is not categories created!");
        }
        return allCategories;
    }

    @Override
    public void createCategory(Category category) {
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory != null){
            throw new APIException("Category with the name " + category.getCategoryName() + " already exist");
        }
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        Category toBeDeletedCategory = categoryOptional.orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId", id));

        if(toBeDeletedCategory == null){
            return "Category with id " + id + "not found";
        }
        categoryRepository.delete(toBeDeletedCategory);
        return "Category with category id {}" + id + "deleted";
    }

    @Override
    public Category updateCategory(Category category, Long id) {
           Optional<Category> savedCategoryOptinoal = categoryRepository.findById(id);
           Category savedCategory = savedCategoryOptinoal.orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId", id));

           if(savedCategory != null) {
               savedCategory.setCategoryName(category.getCategoryName());
               return categoryRepository.save(savedCategory);
           }
           else{
               throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource Not Found");
           }

    }
}
