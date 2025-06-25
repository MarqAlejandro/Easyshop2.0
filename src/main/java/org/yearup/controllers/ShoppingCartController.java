package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

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
    public ResponseEntity<?> addProductToCart(@PathVariable int productId, Principal principal) {
        String username = principal.getName();

        User user = userDao.getByUserName(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        shoppingCartDao.addProductToCart(user.getId(), productId);
        return ResponseEntity.ok("Product added to cart");
    }

    @PutMapping("/products/{productId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateProductQuantity(
            @PathVariable int productId,
            @RequestBody Map<String, Integer> body,
            Principal principal)
    {
        String username = principal.getName();
        User user = userDao.getByUserName(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Integer newQuantity = body.get("quantity");
        if (newQuantity == null || newQuantity < 1) {
            return ResponseEntity.badRequest().body("Quantity must be >= 1");
        }

        boolean exists = shoppingCartDao.existsInCart(user.getId(), productId);
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in cart");
        }

        shoppingCartDao.updateQuantity(user.getId(), productId, newQuantity);
        return ResponseEntity.ok("Quantity updated");
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> clearCart(Principal principal){
        try{
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            if (user == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            shoppingCartDao.clearCart(user.getId());

            return ResponseEntity.ok("Shopping cart cleared.");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to clear shopping cart.");
        }
    }


}
