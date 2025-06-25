package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Category;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    private final ProductDao productDao;

    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao) {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();

        String sql = """
        SELECT product_id, quantity
        FROM shopping_cart_items
        WHERE user_id = ?
        """;

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


}
