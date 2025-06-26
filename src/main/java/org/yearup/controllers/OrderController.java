package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.OrderDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
public class OrderController {

    private final ShoppingCartDao shoppingCartDao;
    private final OrderDao orderDao;
    private final UserDao userDao;

    @Autowired
    public OrderController(ShoppingCartDao shoppingCartDao, OrderDao orderDao, UserDao userDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.orderDao = orderDao;
        this.userDao = userDao;
    }

    @PostMapping
    public ResponseEntity<?> checkout(Principal principal) {
        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        // 1. Retrieve user's shopping cart
        ShoppingCart cart = shoppingCartDao.getByUserId(user.getId());

        if (cart == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Shopping cart is empty.");
        }

        // 2. Calculate total (already includes quantity and discount logic)
        BigDecimal total = cart.getTotal();

        // 3. Create and persist Order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(total);

        orderDao.createOrder(order); // Order ID should now be set

        // 4. Create and persist OrderLineItems
        for (ShoppingCartItem item : cart.getItems().values()) {
            OrderLineItem lineItem = new OrderLineItem();
            lineItem.setOrderId(order.getId());
            lineItem.setProductId(item.getProductId());
            lineItem.setQuantity(item.getQuantity());
            lineItem.setPrice(item.getProduct().getPrice()); // Storing per-unit price

            orderDao.addLineItem(lineItem);
        }

        // 5. Clear user's shopping cart
        shoppingCartDao.clearCart(user.getId());

        return ResponseEntity.ok("Order placed successfully.");
    }
}