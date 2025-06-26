package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;

public interface OrderDao {
    void createOrder(Order order);
    void addLineItem(OrderLineItem item);
}