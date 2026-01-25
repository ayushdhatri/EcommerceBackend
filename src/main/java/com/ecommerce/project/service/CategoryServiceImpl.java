package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;
    public CategoryServiceImpl(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> allCategories = categoryPage.getContent();
        if(allCategories.isEmpty()){
            throw new APIException("There is not categories created!");
        }
        List<CategoryDTO> categoryDTOS = allCategories.stream()
                .map(category -> {
                    return modelMapper.map(category, CategoryDTO.class);
                }).toList();
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPagesize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        Category savedCategory = categoryRepository.findByCategoryName(categoryDTO.getCategoryName());
        if(savedCategory != null){
            throw new APIException("Category with the name " + categoryDTO.getCategoryName() + " already exist");
        }
        Category newCategory =  modelMapper.map(categoryDTO,Category.class);
        categoryRepository.save(newCategory);
        return  categoryDTO;
    }

    @Override
    public CategoryDTO deleteCategory(Long id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        Category toBeDeletedCategory = categoryOptional.orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId", id));
        categoryRepository.delete(toBeDeletedCategory);
        return modelMapper.map(toBeDeletedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long id) {
           Optional<Category> savedCategoryOptional = categoryRepository.findById(id);
           Category savedCategory = savedCategoryOptional.orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId", id));
           savedCategory.setCategoryName(categoryDTO.getCategoryName());
           CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory, CategoryDTO.class);
           categoryRepository.save(savedCategory);
           return savedCategoryDTO;
    }
}
