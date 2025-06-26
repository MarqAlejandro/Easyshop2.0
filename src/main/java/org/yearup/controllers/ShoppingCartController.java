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

@RestController
@RequestMapping("cart")
@CrossOrigin
public class ShoppingCartController
{
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    @Autowired
    public ShoppingCartController(UserDao userDao,
                                  ShoppingCartDao shoppingCartDao,
                                  ProductDao productDao)
    {
        this.userDao = userDao;
        this.shoppingCartDao = shoppingCartDao;
        this.productDao = productDao;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // get the currently logged in username
            String userName = principal.getName();

            // find database user by userId
            User user = userDao.getByUserName(userName);

            if (user == null) {
                System.out.println("User not found in DB for: " + userName);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userName);
            }


            int userId = user.getId();


            // use the shoppingcartDao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad get cart.");
        }
    }

    @PostMapping("/products/{productId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ShoppingCart addProductToCart(@PathVariable int productId, Principal principal)
    {
        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        shoppingCartDao.addProductToCart(user.getId(), productId);

        // return updated cart
        return shoppingCartDao.getByUserId(user.getId());
    }
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

        Integer newQuantity = body.get("quantity");
        if (newQuantity == null || newQuantity < 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quantity must be >= 1");
            return;
        }

        boolean exists = shoppingCartDao.existsInCart(user.getId(), productId);
        if (!exists) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found in cart");
            return;
        }

        shoppingCartDao.updateQuantity(user.getId(), productId, newQuantity);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal, HttpServletResponse response) throws IOException {
        try {
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found.");
                return;
            }

            shoppingCartDao.clearCart(user.getId());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to clear shopping cart.");
        }
    }
}
