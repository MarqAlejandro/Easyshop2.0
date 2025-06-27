package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

@Component // Marks this class as a Spring component for DI
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    private final ProductDao productDao;

    // Constructor injecting DataSource and ProductDao dependencies
    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao) {
        super(dataSource);
        this.productDao = productDao;
    }

    /**
     * Retrieves the shopping cart for a specific user by userId.
     * Loads product details for each item using ProductDao.
     *
     * @param userId the ID of the user
     * @return ShoppingCart object containing items and quantities
     */
    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();

        String sql = "SELECT * FROM shopping_cart WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            // Iterate through all rows for the user's cart items
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");

                // Fetch full product info from ProductDao
                Product product = productDao.getById(productId);

                if (product != null) {
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    item.setDiscountPercent(BigDecimal.ZERO); // Default discount to zero

                    cart.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading shopping cart for user ID: " + userId, e);
        }

        return cart;
    }

    /**
     * Adds a product to the user's shopping cart.
     * If the product already exists in the cart, increments quantity by 1.
     * Otherwise, inserts a new cart record with quantity 1.
     *
     * @param userId the ID of the user
     * @param productId the ID of the product to add
     */
    @Override
    public void addProductToCart(int userId, int productId) {
        if (existsInCart(userId, productId)) {
            incrementQuantity(userId, productId);
        } else {
            insertNewProduct(userId, productId);
        }
    }

    /**
     * Checks if a product already exists in a user's cart.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if product exists in cart, false otherwise
     */
    @Override
    public boolean existsInCart(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if product exists in cart", e);
        }
        return false;
    }

    /**
     * Helper method to increment the quantity of an existing product in the cart.
     *
     * @param userId the user ID
     * @param productId the product ID
     */
    private void incrementQuantity(int userId, int productId) {
        String sql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error incrementing product quantity", e);
        }
    }

    /**
     * Helper method to insert a new product into the cart with quantity 1.
     *
     * @param userId the user ID
     * @param productId the product ID
     */
    private void insertNewProduct(int userId, int productId) {
        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting new product to cart", e);
        }
    }

    /**
     * Deletes all items in a user's shopping cart.
     *
     * @param userId the user ID whose cart will be cleared
     */
    @Override
    public void clearCart(int userId){
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
        catch (SQLException e){
            throw new RuntimeException("Error clearing shopping cart for user ID: " + userId, e);
        }
    }

    /**
     * Updates the quantity of a specific product in the user's cart.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @param quantity the new quantity to set
     */
    @Override
    public void updateQuantity(int userId, int productId, int quantity) {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, productId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("No cart item found to update");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating quantity", e);
        }
    }

}