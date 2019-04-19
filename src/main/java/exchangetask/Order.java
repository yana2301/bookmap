package exchangetask;

import java.util.Objects;

public class Order {
    final long orderId;
    final int price;
    final int size;

    public Order(long orderId, int price, int size) {
        this.orderId = orderId;
        this.price = price;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId &&
                price == order.price &&
                size == order.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, price, size);
    }
}
