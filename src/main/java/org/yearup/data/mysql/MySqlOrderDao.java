package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;

import javax.sql.*;
import java.sql.*;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {


    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    public void createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getUserId());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            stmt.setString(3, order.getAddress());
            stmt.setString(4, order.getCity());
            stmt.setString(5, order.getState());
            stmt.setString(6, order.getZip());
            stmt.setBigDecimal(7, order.getShippingAmount());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order", e);
        }
    }

    public void addLineItem(OrderLineItem item) {
        String sql = "INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setInt(4, item.getQuantity());
            stmt.setBigDecimal(5, item.getDiscount());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add order line item", e);
        }
    }
}
