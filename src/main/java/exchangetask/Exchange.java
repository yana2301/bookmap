package exchangetask;


import java.util.Collection;

/**
 * The Exchange class implements ExchangeInterface and QueryInterface.
 * It processes incoming orders and keeps cache of resting orders.
 * The class is not thread safe.
 */

public class Exchange implements ExchangeInterface, QueryInterface {

    private final OrderTable buyOrders = new OrderTable();

    private final OrderTable sellOrders = new OrderTable();


    /**
     * Sends order to execution
     * average running time - O(logN)
     *
     * @param orderId id of new order
     * @param isBuy   side of the order
     * @param price   order price. Must be greater than zero.
     * @param size    order size. Must be greater than zero.
     */
    public void send(long orderId, boolean isBuy, int price, int size) throws RequestRejectedException {
        validateGreaterThanZero(price, "Order price should be higher than 0 ");
        validateGreaterThanZero(size, "Order size should be higher than 0 ");
        if (sellOrders.exists(orderId) || buyOrders.exists(orderId)) {
            throw new RequestRejectedException("Order already exists");
        } else {
            if (isBuy) {
                Collection<Order> matchingOrders = sellOrders.getSamePriceOrCheaperOrders(price);
                fulfillOrder(orderId, price, size, matchingOrders, sellOrders, buyOrders);
            } else {
                Collection<Order> matchingOrders = buyOrders.getSamePriceOrMoreExpensiveOrders(price);
                fulfillOrder(orderId, price, size, matchingOrders, buyOrders, sellOrders);
            }
        }
    }

    /**
     * Selects matching orders and sends them to execution. Stores unmatched amount in resting orders.
     *
     * @param orderId                  id of new order
     * @param price                    order price. Must be greater than zero.
     * @param size                     order size. Must be greater than zero.
     * @param matchingOrders           list of opposite side orders that can be matched with current order
     * @param tableForMatchingOrders   table where opposite side orders are stored
     * @param tableForNonMatchedAmount table where to store non-matched amount
     */
    private void fulfillOrder(long orderId, int price, int size, Collection<Order> matchingOrders,
                              OrderTable tableForMatchingOrders, OrderTable tableForNonMatchedAmount) {
        int notFulfilledAmount = matchOrders(size, matchingOrders, tableForMatchingOrders);
        if (notFulfilledAmount > 0) {
            tableForNonMatchedAmount.add(new Order(orderId, price, notFulfilledAmount));
        }
    }

    /**
     * Selects matching orders and sends them to execution. Stores unmatched amount in resting orders.
     *
     * @param size                   order size. Must be greater than zero.
     * @param matchingOrders         list of opposite side orders that can be matched with current order
     * @param tableForMatchingOrders table where opposite side orders are stored
     * @return nonmatched amount of the current order
     */
    private int matchOrders(int size, Collection<Order> matchingOrders, OrderTable tableForMatchingOrders) {
        int sizeLeft = size;
        for (Order nextOrder : matchingOrders) {
            if (nextOrder.size > sizeLeft) {
                tableForMatchingOrders.modify(nextOrder.orderId, nextOrder.price, nextOrder.size - sizeLeft);
                return 0;
            } else {
                tableForMatchingOrders.remove(nextOrder.orderId);
                sizeLeft = sizeLeft - nextOrder.size;
            }
        }
        return sizeLeft;
    }

    /**
     * Selects matching orders and sends them to execution. Stores unmatched amount in resting orders.
     *
     * @param orderId id of the order to modify. Must be existing order id.
     * @param price   order price. Must be greater than zero.
     * @param size    order size. Must be greater than zero.
     */
    public void modify(long orderId, int price, int size) throws RequestRejectedException {
        validateGreaterThanZero(price, "Order price should be higher than 0 ");
        validateGreaterThanZero(size, "Order size should be higher than 0 ");
        if (buyOrders.exists(orderId)) {
            buyOrders.modify(orderId, price, size);
        } else if (sellOrders.exists(orderId)) {
            sellOrders.modify(orderId, price, size);
        } else {
            throw new RequestRejectedException("Order does not exist id = " + orderId);
        }
    }

    private void validateGreaterThanZero(int val, String msg) throws RequestRejectedException {
        if (val <= 0) {
            throw new RequestRejectedException(msg);
        }
    }

    /**
     * Cancels order by id.
     *
     * @param orderId id of the order to cancel. Must be existing order id.
     */
    public void cancel(long orderId) throws RequestRejectedException {
        if (buyOrders.exists(orderId)) {
            buyOrders.remove(orderId);
        } else if (sellOrders.exists(orderId)) {
            sellOrders.remove(orderId);
        } else {
            throw new RequestRejectedException("Order not found id = " + orderId);
        }
    }

    /**
     * @param price price to check
     * @return total size of orders from both sides whose price is equal to specified.
     */
    public int getTotalSizeAtPrice(int price) throws RequestRejectedException {
        int buyOrdersTotal = buyOrders.getAllOrdersAtPrice(price).stream().mapToInt(order -> order.size).sum();
        int sellOrdersTotal = sellOrders.getAllOrdersAtPrice(price).stream().mapToInt(order -> order.size).sum();
        return buyOrdersTotal + sellOrdersTotal;
    }

    /**
     * @return highest buy price among resting orders.
     */
    public int getHighestBuyPrice() throws RequestRejectedException {
        return buyOrders.getHighestPriceOrder().map(o -> o.price).orElse(0);
    }


    /**
     * @return lowest sell price among resting orders.
     */
    public int getLowestSellPrice() throws RequestRejectedException {
        return sellOrders.getLowestPriceOrder().map(o -> o.price).orElse(0);
    }
}
