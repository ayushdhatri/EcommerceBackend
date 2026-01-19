package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService{

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }
    @Override
    public ProductDTO addProduct(Long categoryId, Product product) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));
        product.setCategory(category);
        product.setSpecialPrice(product.getPrice() - product.getDiscount() * 0.01 * product.getPrice());
        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);

    }

    @Override
    public ProductResponse getAllProducts(){
      List<Product> savedProduct = productRepository.findAll();
      List<ProductDTO> savedProductDTO = savedProduct.stream().map((product) -> {
          return modelMapper.map(product, ProductDTO.class);
      }).toList();
      ProductResponse productResponse = new ProductResponse();
      productResponse.setContent(savedProductDTO);
      return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));

        List<Product> savedProduct = productRepository.getProductByCategoryOrderByPriceAsc(category);
        List<ProductDTO> savedProductDTO = savedProduct.stream()
                .map((product) -> {
                    return modelMapper.map(product, ProductDTO.class);
                }).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(savedProductDTO);
        return productResponse;


    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword) {
        List<Product> savedProduct = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%');
        List<ProductDTO> productsHavingKeyword  = savedProduct.stream()
                .map((product) -> {
                    return modelMapper.map(product,ProductDTO.class);
                }).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productsHavingKeyword);
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO product) {
        Product savedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product_id", productId));
        savedProduct.setProductName(product.getProductName());
        savedProduct.setPrice(product.getPrice());
        savedProduct.setQuantity(product.getQuantity());
        savedProduct.setDiscount(product.getDiscount());
        savedProduct.setSpecialPrice(product.getPrice() - product.getDiscount() * 0.01 * product.getPrice());
        productRepository.save(savedProduct);
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product savedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.delete(savedProduct);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

}
