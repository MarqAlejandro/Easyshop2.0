package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;

import javax.sql.DataSource;
import java.sql.*;

@Component // Registers this class as a Spring component for dependency injection
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao
{
    // Constructor that passes the DataSource to the base DAO class
    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Inserts a new order into the 'orders' table.
     * Populates the generated order ID back into the given Order object.
     *
     * @param order the order to be created
     */
    @Override
    public void createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Try-with-resources ensures connections and statements are closed properly
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set the parameter values
            stmt.setInt(1, order.getUserId());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            stmt.setString(3, order.getAddress());
            stmt.setString(4, order.getCity());
            stmt.setString(5, order.getState());
            stmt.setString(6, order.getZip());
            stmt.setBigDecimal(7, order.getShippingAmount());

            // Execute the insert command
            stmt.executeUpdate();

            // Retrieve and set the generated order ID
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order", e);
        }
    }

    /**
     * Inserts an order line item into the 'order_line_items' table.
     *
     * @param item the OrderLineItem to add
     */
    @Override
    public void addLineItem(OrderLineItem item) {
        String sql = "INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the parameter values
            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getPrice());     // per-unit price
            stmt.setInt(4, item.getQuantity());
            stmt.setBigDecimal(5, item.getDiscount());

            // Execute the insert command
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add order line item", e);
        }
    }
}