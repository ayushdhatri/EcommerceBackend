package com.ecommerce.project.service;


import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService{
    @Autowired
    AuthUtil authUtil;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ModelMapper modelMapper;

    private final CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find existing cart or create one
        Cart userCart = createCart();
        // Retrieve Product Details
        Product savedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        // Perform Validations
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(userCart.getCartId(),productId);
        if(cartItem != null){
            throw new APIException("Product" + savedProduct.getProductName() + "already Existed in cart");
        }
        if(savedProduct.getQuantity() == 0){
            throw new APIException(savedProduct.getProductName() + " is not available ");
        }
        if(savedProduct.getQuantity() < quantity){
            throw new APIException("Please, make an order of the " + savedProduct.getProductName() + " less than or equal to the quantity " + savedProduct.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(savedProduct);
        newCartItem.setCart(userCart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(savedProduct.getDiscount());
        newCartItem.setProductPrice(savedProduct.getSpecialPrice());
        cartItemRepository.save(newCartItem);

        // Here we are not updating the stock, stock will only get updated when order is placed.
        savedProduct.setQuantity(savedProduct.getQuantity());
        // create Cart Item
        //save cart item
        userCart.setTotalPrice(userCart.getTotalPrice() + (savedProduct.getSpecialPrice() * quantity));
        // return updated cart
        cartRepository.save(userCart);

        CartDTO cartDTO =  modelMapper.map(userCart, CartDTO.class);
        List<ProductDTO> cartProducts = userCart.getCartItems().stream()
                .map(item -> {
                    ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                    map.setQuantity(item.getQuantity());
                    return map;
                }).toList();
        cartDTO.setProducts(cartProducts);
        return cartDTO;

    }

    @Override
    public List<CartDTO> getAllCarts() {
         List<Cart> carts = cartRepository.findAll();
         if(carts.size() == 0){
             throw new APIException("No Cart Exist");
         }
         List<CartDTO> cartDTOS = carts.stream().map((cart)->{
             CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
             List<ProductDTO> productDTOS = cart.getCartItems().stream()
                     .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();
             cartDTO.setProducts(productDTOS);
             return cartDTO;

         }).toList();
         return cartDTOS;
    }

    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }
        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser((authUtil.loggedInUser()));
        Cart newCart = cartRepository.save(cart);
        return cart;
    }

}
