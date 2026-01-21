package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> getProductByCategory(Category category);

    Page<Product> getProductByCategoryOrderByPriceAsc(Category category, Pageable pageDetails);

    List<Product> findByProductNameLikeIgnoreCase(String keyword);

    Product findByProductNameIgnoreCase(String productName);
}
