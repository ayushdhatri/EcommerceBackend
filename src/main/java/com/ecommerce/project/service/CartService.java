package com.ecommerce.project.service;


import com.ecommerce.project.payload.CartDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {

    CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getUserCart();

    @Transactional
    CartDTO updateCartProduct(Long productId, int operation);
    @Transactional
    String deleteProductFromCart(Long cartId, Long productId);
}
