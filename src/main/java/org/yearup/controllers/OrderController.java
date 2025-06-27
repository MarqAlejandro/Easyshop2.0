package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController // Indicates this class is a REST controller returning JSON responses
@RequestMapping("cart") // Base URL path for all endpoints in this controller
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')") // Only users with these roles can access the methods
@CrossOrigin // Enables cross-origin requests (e.g. from frontend on a different port)
public class OrderController {
    // DAO dependencies for cart, order, user, and profile access
    private final ShoppingCartDao shoppingCartDao;
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final ProfileDao profileDao;

    // Constructor-based dependency injection
    @Autowired
    public OrderController(ShoppingCartDao shoppingCartDao,
                           OrderDao orderDao,
                           UserDao userDao,
                           ProfileDao profileDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    /**
     * POST /cart/checkout
     * Performs checkout for the current user: creates order, line items, and clears the cart.
     *
     * @param principal Authenticated user's information
     * @return ResponseEntity with created OrderDTO or error status
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED) // Sets default response status to 201 Created
    public ResponseEntity<?> checkout(Principal principal) {
        // Retrieve the authenticated user's username
        String username = principal.getName();
        User user = userDao.getByUserName(username);

        // Return 401 if the user is not found
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        // Load the user's profile to get shipping address details
        Profile profile = profileDao.getByUserId(user.getId());

        // Get the user's shopping cart
        ShoppingCart cart = shoppingCartDao.getByUserId(user.getId());

        // Return 400 if the cart is empty or null
        if (cart == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Shopping cart is empty.");
        }

        // Calculate the total cost of the items in the cart
        BigDecimal total = cart.getTotal();

        // Create an Order object and populate its fields
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(total);
        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());
        order.setShippingAmount(new BigDecimal("5.99")); // Flat-rate shipping

        // Save the order to the database (order ID is generated here)
        orderDao.createOrder(order);

        // Create line items based on cart contents
        List<OrderLineItem> orderItems = new ArrayList<>();

        for (ShoppingCartItem item : cart.getItems().values()) {
            OrderLineItem lineItem = new OrderLineItem();
            lineItem.setOrderId(order.getId());
            lineItem.setProductId(item.getProductId());
            lineItem.setQuantity(item.getQuantity());
            lineItem.setPrice(item.getProduct().getPrice()); // Per-unit price
            lineItem.setDiscount(new BigDecimal("0.00")); // No discount applied
            orderDao.addLineItem(lineItem);
            orderItems.add(lineItem);
        }

        // Clear the shopping cart after the order is placed
        shoppingCartDao.clearCart(user.getId());

        // Build and return the response DTO with order and line item details
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAmount(order.getShippingAmount());
        dto.setAddress(order.getAddress());
        dto.setCity(order.getCity());
        dto.setState(order.getState());
        dto.setZip(order.getZip());
        dto.setLineItems(orderItems);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}