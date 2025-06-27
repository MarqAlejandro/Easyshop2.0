package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@RestController // Marks this class as a REST controller where every method returns a domain object instead of a view
@RequestMapping("cart") // Base route for all endpoints in this controller
@CrossOrigin // Allows cross-origin requests (important for frontend/backend communication)
public class ShoppingCartController
{
    // Dependencies required for managing the shopping cart
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    // Constructor-based dependency injection
    @Autowired
    public ShoppingCartController(UserDao userDao,
                                  ShoppingCartDao shoppingCartDao,
                                  ProductDao productDao)
    {
        this.userDao = userDao;
        this.shoppingCartDao = shoppingCartDao;
        this.productDao = productDao;
    }

    /**
     * Retrieves the current user's shopping cart.
     *
     * @param principal represents the authenticated user
     * @return ShoppingCart object for the current user
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')") // Ensures only authenticated users with ROLE_USER can access this
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // Get the currently logged-in user's username
            String userName = principal.getName();

            // Find the corresponding User object
            User user = userDao.getByUserName(userName);

            if (user == null) {
                System.out.println("User not found in DB for: " + userName);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userName);
            }

            // Retrieve and return the user's shopping cart
            return shoppingCartDao.getByUserId(user.getId());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad get cart.");
        }
    }

    /**
     * Adds a product to the current user's cart.
     *
     * @param productId ID of the product to add
     * @param principal authenticated user info
     * @return updated ShoppingCart
     */
    @PostMapping("/products/{productId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ShoppingCart addProductToCart(@PathVariable int productId, Principal principal)
    {
        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Add the product to the cart
        shoppingCartDao.addProductToCart(user.getId(), productId);

        // Return the updated cart
        return shoppingCartDao.getByUserId(user.getId());
    }

    /**
     * Updates the quantity of a specific product in the cart.
     *
     * @param productId ID of the product to update
     * @param body JSON body containing the new quantity
     * @param principal authenticated user info
     * @param response used to send HTTP error codes
     */
    @PutMapping("/products/{productId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public void updateProductQuantity(
            @PathVariable int productId,
            @RequestBody Map<String, Integer> body,
            Principal principal,
            HttpServletResponse response) throws IOException
    {
        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        // Extract new quantity from request body
        Integer newQuantity = body.get("quantity");

        // Validate quantity
        if (newQuantity == null || newQuantity < 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quantity must be >= 1");
            return;
        }

        // Check if product exists in the user's cart
        boolean exists = shoppingCartDao.existsInCart(user.getId(), productId);
        if (!exists) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found in cart");
            return;
        }

        // Update quantity
        shoppingCartDao.updateQuantity(user.getId(), productId, newQuantity);
    }

    /**
     * Clears the entire shopping cart for the current user.
     *
     * @param principal authenticated user info
     * @param response used to send HTTP error codes
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Indicates success with no content returned
    public void clearCart(Principal principal, HttpServletResponse response) throws IOException {
        try {
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found.");
                return;
            }

            // Clear all items in the cart
            shoppingCartDao.clearCart(user.getId());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to clear shopping cart.");
        }
    }
}