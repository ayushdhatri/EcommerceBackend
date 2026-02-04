package com.ecommerce.project.service;


import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

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
         if(carts.isEmpty()){
             throw new APIException("No Cart Exist");
         }
        return carts.stream().map((cart)->{
            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
            List<ProductDTO> productDTOS = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();
            cartDTO.setProducts(productDTOS);
            return cartDTO;

        }).toList();
    }

    @Override
    public CartDTO getUserCart() {

        // we need to fetch the details of loggedIn user
        User loggedInuser = authUtil.loggedInUser();
        // from user fetch the details of cart
        Cart userCart = cartRepository.findCartByEmail(loggedInuser.getEmail());
        if(userCart == null){
            throw new APIException("No Item added in cart! Cart is Empty!");
        }
        CartDTO userCartDTO = new CartDTO();
        List<CartItem> cartItems = userCart.getCartItems();
        cartItems.forEach(cartItem -> cartItem.getProduct().setQuantity(cartItem.getQuantity()));
        List<ProductDTO> productDTOS = cartItems.stream()
                        .map((c)->{
                            return modelMapper.map(c.getProduct(),ProductDTO.class);
                        }).toList();


        userCartDTO.setCartId(userCart.getCartId());
        userCartDTO.setProducts(productDTOS);
        return userCartDTO;

    }

    @Override
    @Transactional
    public CartDTO updateCartProduct(Long productId, int operation) {
        // First we need the user Cart
        // Then list of cartItems
        // check which cartItems productId matches with productId
        // based on operation decide increase to decrease
        // update the database as well so that this change is persistent
        // return the updated DTO
        User loggedInUser = authUtil.loggedInUser();
        Cart userCart = cartRepository.findCartByEmail(loggedInUser.getEmail());
        Cart cart = cartRepository.findById(userCart.getCartId()).orElseThrow(()-> new ResourceNotFoundException("Cart", "CartId", userCart.getCartId()));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "ProductId", productId));
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }
        if(product.getQuantity() < operation){
            throw new APIException("Please, make an order of the " + product.getQuantity() + " less than or equal to the quantity");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(userCart.getCartId(), productId);
        if(cartItem == null){
            throw new APIException("Product " + product.getProductName() + " Not available in the cart");
        }

        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setQuantity(cartItem.getQuantity() + operation);
        cartItem.setDiscount(product.getDiscount());
        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * operation));

        cartRepository.save(cart);
        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item ->{
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    @Override
    @Transactional
    public String deleteProductFromCart(Long cartId, Long productId) {
        // get the information about cart,
        // search cartItem with product Id
        // if null return else delete the item from cart
        // save the repository
        // return positive response
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart","CartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if(cartItem == null){
            throw new ResourceNotFoundException("Product", "ProductId",productId);
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);


        return "CartItem Deleted Successfully";

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
