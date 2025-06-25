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

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    private final ProductDao productDao;

    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao) {
        super(dataSource);
        this.productDao = productDao;
    }
//================================================================get cart info
    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();

        String sql = "SELECT * " +
                "FROM shopping_cart " +
                "WHERE user_id = ? ";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");

                // Use ProductDao to get full Product info
                Product product = productDao.getById(productId);

                if (product != null)
                {
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    item.setDiscountPercent(BigDecimal.ZERO); // default to no discount

                    cart.add(item);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error loading shopping cart for user ID: " + userId, e);
        }

        return cart;

    }
//=========================================================================add product to cart logic
    @Override
    public void addProductToCart(int userId, int productId) {
        if (existsInCart(userId, productId)) {
            incrementQuantity(userId, productId);
        } else {
            insertNewProduct(userId, productId);
        }
    }

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
//==============================================================================delete all cart items
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
//==============================================================================update quantity manually

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
