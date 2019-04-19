package exchangetask;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExchangeTest {

    //Highest Buy Price Tests

    @Test
    public void shouldReturnZeroAsHighestPriceIfExchangeIsEmpty() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //Then
        assertEquals(0, exchange.getHighestBuyPrice());
    }

    @Test
    public void shouldReturnZeroAsHighestPriceIfThereAreNoBuyOrders() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When
        exchange.send(1, false, 1, 1);

        //Then
        assertEquals(0, exchange.getHighestBuyPrice());
    }

    @Test
    public void shouldReturnCorrectHighestBuyPriceForNonEmptyExchange() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When
        exchange.send(1, true, 2, 5);
        exchange.send(2, true, 1, 5);
        exchange.send(3, true, 1, 5);
        exchange.send(4, true, 5, 1);
        //cancel order with highest price
        exchange.cancel(4);
        exchange.send(5, false, 100, 1);

        //Then
        assertEquals(2, exchange.getHighestBuyPrice());
    }

    //Lowest Sell Price Tests

    @Test
    public void shouldReturnZeroAsLowestPriceIfExchangeIsEmpty() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //Then
        assertEquals(0, exchange.getLowestSellPrice());
    }

    @Test
    public void shouldReturnZeroAsLowestPriceIfThereAreNoBuyOrders() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When
        exchange.send(1, true, 1, 1);

        //Then
        assertEquals(0, exchange.getLowestSellPrice());
    }

    @Test
    public void shouldReturnCorrectLowestSellPrice() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When
        exchange.send(1, false, 2, 5);
        exchange.send(2, false, 3, 5);
        exchange.send(3, false, 2, 5);
        exchange.send(4, false, 1, 1);
        //cancel order with highest price
        exchange.cancel(4);

        exchange.send(5, true, 1, 1);

        //Then
        assertEquals(2, exchange.getLowestSellPrice());
    }

    // Fulfillment Tests

    @Test
    public void shouldNotAllowToSendNonPositivePriceSellOrder() {
        //Given
        Exchange exchange = new Exchange();

        //Then
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, false, -1, 2));
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, false, 0, 2));

    }

    @Test
    public void shouldNotAllowToSendNonPositiveSizeSellOrder() {
        //Given
        Exchange exchange = new Exchange();

        //Then
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, false, 1, -1));
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, false, 2, 0));

    }

    @Test
    public void shouldNotAllowToSendNonPositivePriceBuyOrder() {
        //Given
        Exchange exchange = new Exchange();

        //Then
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, true, -1, 2));
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, true, 0, 2));

    }

    @Test
    public void shouldNotAllowToSendNonPositiveSizeBuyOrder() {
        //Given
        Exchange exchange = new Exchange();

        //Then
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, true, 1, -1));
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, true, 2, 0));

    }

    @Test
    public void shouldNotAllowSendOrderWithDuplicateId() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When
        exchange.send(1, true, 1, 1);
        exchange.send(2, false, 1, 1);

        //Then
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, true, 1, -1));
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(1, false, 1, -1));

        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(2, true, 1, -1));
        Assertions.assertThrows(RequestRejectedException.class,
                () -> exchange.send(2, false, 1, -1));

    }


    @Test
    public void shouldNotFulfillOrderIfThereAreNoMatches() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When(there are no matching sell orders)
        exchange.send(1, false, 3, 5);
        exchange.send(2, true, 1, 5);

        //Then
        assertEquals(3, exchange.getLowestSellPrice());
        assertEquals(1, exchange.getHighestBuyPrice());
    }

    @Test
    public void shouldFulFillSellOrderPartiallyIfThereAreMatchingBuys() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When(there are multiple buy orders that partially match sell order)
        exchange.send(1, true, 3, 1);
        exchange.send(2, true, 4, 1);
        exchange.send(4, true, 2, 1);
        exchange.send(3, false, 3, 5);

        //Then
        assertEquals(3, exchange.getLowestSellPrice());
        assertEquals(2, exchange.getHighestBuyPrice());
        assertEquals(3, exchange.getTotalSizeAtPrice(3));
    }

    @Test
    public void shouldFulFillSellOrderEntirelyIfThereArePricierBuys() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When(there is buy order that entirely matches sell order)
        exchange.send(2, true, 4, 2);
        exchange.send(3, false, 3, 1);


        //Then
        assertEquals(0, exchange.getLowestSellPrice());
        assertEquals(4, exchange.getHighestBuyPrice());
        assertEquals(0, exchange.getTotalSizeAtPrice(3));
        assertEquals(1, exchange.getTotalSizeAtPrice(4));
    }

    @Test
    public void shouldFulFillSellOrderEntirelyWithTheBestMatch() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When(if there are multiple matching buy orders, we should choose those with higher prices
        exchange.send(1, true, 4, 1);
        exchange.send(2, true, 3, 3);
        exchange.send(3, true, 4, 1);
        exchange.send(4, false, 3, 3);

        //Then
        assertEquals(0, exchange.getLowestSellPrice());
        assertEquals(3, exchange.getHighestBuyPrice());
        assertEquals(2, exchange.getTotalSizeAtPrice(3));
    }

    @Test
    public void shouldFulfillBuyOrderPartiallyIfThereAreCheaperSells() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When(when there are sell orders that fulfill buy order - we should match them and keep what is left in resting orders)
        exchange.send(1, false, 3, 1);
        exchange.send(3, false, 4, 2);
        exchange.send(4, false, 5, 2);
        exchange.send(2, true, 4, 6);

        //Then
        assertEquals(5, exchange.getLowestSellPrice());
        assertEquals(4, exchange.getHighestBuyPrice());
        assertEquals(3, exchange.getTotalSizeAtPrice(4));
    }

    @Test
    public void shouldFulfillBuyOrderEntirelyIfThereAreCheaperSells() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When(when there are sell orders with price<= buy price that entirely match buy order - we should match all of them)
        exchange.send(1, false, 3, 1);
        exchange.send(2, false, 4, 2);
        exchange.send(3, false, 4, 3);
        exchange.send(4, true, 4, 6);

        //Then
        assertEquals(0, exchange.getLowestSellPrice());
        assertEquals(0, exchange.getHighestBuyPrice());
        assertEquals(0, exchange.getTotalSizeAtPrice(4));
    }

    @Test
    public void shouldFulFillBuyOrderEntirelyWithTheBestMatch() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When(if there are multiple matching sell orders, we should choose those with lower prices
        exchange.send(1, false, 1, 1);
        exchange.send(2, false, 2, 3);
        exchange.send(3, false, 1, 1);
        exchange.send(4, true, 2, 3);

        //Then
        assertEquals(2, exchange.getLowestSellPrice());
        assertEquals(0, exchange.getHighestBuyPrice());
        assertEquals(2, exchange.getTotalSizeAtPrice(2));
    }

    // Modify order tests

    @Test
    public void shouldThrowAnExceptionIfOrderNotExists() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //Then
        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(1, 1, 3));
    }

    @Test
    public void shouldModifySellOrderPriceIfOrderExists() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();
        exchange.send(1, false, 1, 1);

        //When
        exchange.modify(1, 2, 2);

        //Then
        assertEquals(2, exchange.getTotalSizeAtPrice(2));
        assertEquals(2, exchange.getLowestSellPrice());

    }

    @Test
    public void shouldModifyBuyOrderPriceIfOrderExists() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();
        exchange.send(1, true, 1, 1);

        //When
        exchange.modify(1, 2, 2);

        //Then
        assertEquals(2, exchange.getTotalSizeAtPrice(2));
        assertEquals(2, exchange.getHighestBuyPrice());
    }

    @Test
    public void shouldNotAllowToSetNonPositivePrice() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();
        exchange.send(1, true, 1, 1);
        exchange.send(2, false, 2, 1);

        //Then
        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(1, -1, 3));
        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(2, -1, 3));

        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(1, 0, 3));
        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(2, 0, 3));
    }

    @Test
    public void shouldNotAllowToSetNonPositiveSize() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();
        exchange.send(1, true, 1, 1);
        exchange.send(2, false, 2, 1);

        //Then
        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(1, 1, -3));
        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(2, 1, -3));

        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(1, 1, 0));
        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.modify(2, 1, 0));
    }

    //Cancel Tests

    @Test
    public void shouldNotAllowCancelNonExistingOrder() {
        //Given
        Exchange exchange = new Exchange();

        //Then
        Assertions.assertThrows(RequestRejectedException.class, () -> exchange.cancel(1));
    }

    @Test
    public void shouldCancelRestingSellOrder() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When
        exchange.send(1, false, 1, 1);
        exchange.cancel(1);

        //Then
        assertEquals(0, exchange.getLowestSellPrice());
        assertEquals(0, exchange.getTotalSizeAtPrice(1));
    }

    @Test
    public void shouldCancelRestingBuyOrder() throws RequestRejectedException {
        //Given
        Exchange exchange = new Exchange();

        //When
        exchange.send(1, true, 1, 1);
        exchange.cancel(1);

        //Then
        assertEquals(0, exchange.getHighestBuyPrice());
        assertEquals(0, exchange.getTotalSizeAtPrice(1));
    }
}
