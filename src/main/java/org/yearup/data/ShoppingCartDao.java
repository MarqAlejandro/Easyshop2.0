package org.yearup.data;

import org.yearup.models.ShoppingCart;

/**
 * Interface for handling shopping cart operations.
 * Implementations should manage product entries, quantity updates,
 * and retrievals specific to a user's shopping cart.
 */
public interface ShoppingCartDao // change
{
    /**
     * Retrieves the shopping cart for a given user ID.
     *
     * @param userId the ID of the user
     * @return the user's ShoppingCart containing all items
     */
    ShoppingCart getByUserId(int userId);

    /**
     * Adds a product to the user's shopping cart.
     * If the product already exists, increment the quantity.
     *
     * @param userId the ID of the user
     * @param productId the ID of the product to add
     */
    void addProductToCart(int userId, int productId);

    /**
     * Clears all items from the user's shopping cart.
     *
     * @param userId the ID of the user
     */
    void clearCart(int userId);

    /**
     * Checks if a specific product exists in the user's cart.
     *
     * @param userId the ID of the user
     * @param productId the ID of the product
     * @return true if the product exists in the cart, false otherwise
     */
    boolean existsInCart(int userId, int productId);

    /**
     * Updates the quantity of a specific product in the cart.
     *
     * @param userId the ID of the user
     * @param productId the ID of the product to update
     * @param quantity the new quantity to set
     */
    void updateQuantity(int userId, int productId, int quantity);
}