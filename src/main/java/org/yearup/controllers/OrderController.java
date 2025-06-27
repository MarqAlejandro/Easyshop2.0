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

@RestController
@RequestMapping("cart")
@PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
@CrossOrigin
public class OrderController {

    private final ShoppingCartDao shoppingCartDao;
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final ProfileDao profileDao;

    @Autowired
    public OrderController(ShoppingCartDao shoppingCartDao,
                           OrderDao orderDao,
                           UserDao userDao,
                           ProfileDao profileDao) { // <-- inject here
        this.shoppingCartDao = shoppingCartDao;
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> checkout(Principal principal) {
        String username = principal.getName();
        User user = userDao.getByUserName(username);
        Profile profile = profileDao.getByUserId(user.getId());

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
        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());
        order.setShippingAmount(new BigDecimal("5.99"));

        orderDao.createOrder(order); // Order ID should now be set

        List<OrderLineItem> orderItems = new ArrayList<>();

        // 4. Create and persist OrderLineItems
        for (ShoppingCartItem item : cart.getItems().values()) {
            OrderLineItem lineItem = new OrderLineItem();
            lineItem.setOrderId(order.getId());
            lineItem.setProductId(item.getProductId());
            lineItem.setQuantity(item.getQuantity());
            lineItem.setPrice(item.getProduct().getPrice()); // Storing per-unit price
            lineItem.setDiscount(new BigDecimal("0.00"));
            orderDao.addLineItem(lineItem);
            orderItems.add(lineItem);
        }

        // 5. Clear user's shopping cart
        shoppingCartDao.clearCart(user.getId());

        // Return a proper DTO
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