package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ImageUploadDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.service.ImageStorage.ImageStorageStrategy;
import com.ecommerce.project.service.ImageStorage.MultipartUploadStrategy;
import com.ecommerce.project.payload.StoredImage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Service
public class ProductServiceImpl implements ProductService{

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private ImageStorageStrategy imageStorageStrategy;
    @Value("${project.image}")
    private String path;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;

        this.imageStorageStrategy = new MultipartUploadStrategy();
    }
    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        // check if product already present or not
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));
        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();
        for (Product value : products) {
            if (value.getProductName().equals(productDTO.getProductName())) {
                isProductNotPresent = false;
                break;
            }
        }
        if(!isProductNotPresent){
            throw new APIException("Product Already exist");
        }
        Product product = modelMapper.map(productDTO, Product.class);
        product.setCategory(category);
        product.setSpecialPrice(product.getPrice() - product.getDiscount() * 0.01 * product.getPrice());
        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);

    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder){
        // if product size is 0 you can create some
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> productPage = productRepository.findAll(pageDetails);
      List<Product> savedProduct = productPage.getContent();
      List<ProductDTO> savedProductDTO = savedProduct.stream().map((product) -> {
          return modelMapper.map(product, ProductDTO.class);
      }).toList();
      ProductResponse productResponse = new ProductResponse();
      productResponse.setContent(savedProductDTO);
      productResponse.setPageNumber(productPage.getNumber());
      productResponse.setPagesize(productPage.getSize());
      productResponse.setTotalElements(productPage.getTotalElements());
      productResponse.setLastPage(productPage.isLast());
      return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> productPage = productRepository.getProductByCategoryOrderByPriceAsc(category,pageDetails);
        List<Product> savedProduct = productPage.getContent() ;
        List<ProductDTO> savedProductDTO = savedProduct.stream()
                .map((product) -> {
                    return modelMapper.map(product, ProductDTO.class);
                }).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(savedProductDTO);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPagesize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;


    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
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

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the productId from DB
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        // Upload image to server
        // Get the file name of the uploaded image
        // set the path of image in Image request DTO
        ImageUploadDTO imageUploadDTO = new ImageUploadDTO();
        imageUploadDTO.setFile(image);
        imageUploadDTO.setFileName(image.getOriginalFilename());
        imageUploadDTO.setContentType(image.getContentType());
        imageUploadDTO.setPath(path);  // where you want to upload


        StoredImage storedImage = imageStorageStrategy.uploadImage(imageUploadDTO);

        // updating the new file to the product
        Product updateProduct = productRepository.save(productFromDB);
        // return DTO after mapping product to DTO
        return modelMapper.map(updateProduct, ProductDTO.class);

    }

}
