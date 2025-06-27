package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;

/**
 * Interface for managing orders and order line items in the database.
 * Implementations of this interface should provide logic for creating orders
 * and adding associated line items.
 */
public interface OrderDao { // create

    /**
     * Creates a new order record in the database.
     * The order object should contain user ID, shipping address,
     * order date, and shipping amount.
     *
     * After insertion, the generated order ID should be set
     * on the given Order object.
     *
     * @param order the Order object to be created
     */
    void createOrder(Order order);

    /**
     * Adds a new line item to an existing order.
     * Each line item represents a product, its quantity,
     * per-unit sales price, and any discount applied.
     *
     * @param item the OrderLineItem to add
     */
    void addLineItem(OrderLineItem item);
}