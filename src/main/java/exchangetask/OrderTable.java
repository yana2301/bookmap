package exchangetask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The OrderTable class stores resting orders from one side.
 * It has O(logN) average running time for almost all methods.
 * It's not thread safe.
 */
public class OrderTable {
    private final Map<Long, Order> orderCache = new HashMap<>();
    private final TreeMap<Integer, Set<Order>> orderIndexByPrice = new TreeMap<>();


    /**
     * average running time - O(logN)
     *
     * @param order order to add
     */
    public void add(Order order) {
        orderCache.put(order.orderId, order);
        Set<Order> ordersAtSamePrice = orderIndexByPrice.getOrDefault(order.price, new HashSet<>());
        ordersAtSamePrice.add(order);
        orderIndexByPrice.put(order.price, ordersAtSamePrice);
    }

    /**
     * Removes order from the list of resting orders
     * average running time - O(logN)
     *
     * @param orderId id of order to remove
     */
    public void remove(long orderId) {
        Order order = orderCache.remove(orderId);
        removeOrderFromIndex(order);
    }

    /**
     * Removes order from internal index. This index allows quick access for sorted sequences of order.
     * average running time - O(logN)
     *
     * @param order order to remove
     */
    private void removeOrderFromIndex(Order order) {
        orderIndexByPrice.get(order.price).remove(order);
        if (orderIndexByPrice.get(order.price).isEmpty()) {
            orderIndexByPrice.remove(order.price);
        }
    }

    /**
     * Returns order with the highest price
     * average running time - O(logN)
     */
    public Optional<Order> getHighestPriceOrder() {
        if (!orderIndexByPrice.isEmpty()) {
            Set<Order> highestPriceOrders = orderIndexByPrice.lastEntry().getValue();
            return Optional.of(highestPriceOrders.iterator().next());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns order with the lowest price
     * average running time - O(logN)
     */
    public Optional<Order> getLowestPriceOrder() {
        if (!orderIndexByPrice.isEmpty()) {
            Set<Order> lowestPriceOrders = orderIndexByPrice.firstEntry().getValue();
            return Optional.of(lowestPriceOrders.iterator().next());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns true if order with such id exists
     * average running time - O(1)
     *
     * @param orderId id of order to check
     */
    public boolean exists(long orderId) {
        return orderCache.containsKey(orderId);
    }

    /**
     * Modifies order with specified id if it exists
     * average running time - O(logN)
     *
     * @param orderId id of order to modify
     * @param price   new price of order
     * @param size    new size of order
     */
    public void modify(long orderId, int price, int size) {
        if (orderCache.containsKey(orderId)) {
            Order newOrder = new Order(orderId, price, size);
            Order oldOrder = orderCache.get(orderId);
            removeOrderFromIndex(oldOrder);
            add(newOrder);
        }
    }

    /**
     * Returns collection of order which price is equal to specified price
     * average running time - O(logN)
     *
     * @param price price to check
     * @return collection of orders whose price is equal to specified price
     */
    public Collection<Order> getAllOrdersAtPrice(int price) {
        return orderIndexByPrice.getOrDefault(price, Collections.emptySet());
    }

    /**
     * Average running time - O(logN)
     *
     * @param price price to check
     * @return collection of orders whose price is equal or less than specified price. Sorted in ascending order.
     */
    public Collection<Order> getSamePriceOrCheaperOrders(int price) {
        Map<Integer, Set<Order>> sameOrCheaperOrders = orderIndexByPrice.headMap(price, true);
        return sameOrCheaperOrders.values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingInt(order -> order.price))
                .collect(Collectors.toList());
    }

    /**
     * Average running time - O(logN)
     *
     * @param price price to check
     * @return collection of orders whose price is equal or higher than specified price. Sorted in descending order.
     */
    public Collection<Order> getSamePriceOrMoreExpensiveOrders(int price) {
        Map<Integer, Set<Order>> sameOrPricierOrders = orderIndexByPrice.tailMap(price, true);
        return sameOrPricierOrders.values().stream()
                .flatMap(Collection::stream)
                .sorted((o1, o2) -> -Integer.compare(o1.price, o2.price))
                .collect(Collectors.toList());
    }
}
