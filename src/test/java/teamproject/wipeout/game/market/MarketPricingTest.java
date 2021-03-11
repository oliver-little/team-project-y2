package teamproject.wipeout.game.market;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import teamproject.wipeout.game.item.ItemStore;


public class MarketPricingTest {
    private static ItemStore itemStore;
    private Market market;

    @BeforeAll
    public static void initialization() {
        itemStore = assertDoesNotThrow(() -> new ItemStore("marketitemstest.json"));
    }

    @BeforeEach
    public void setup() {
        market = assertDoesNotThrow(() -> new Market(itemStore, false));
    }
    
    @Test
    public void decreasingPrices() {

        market.buyItem(50, 10);

        MarketItem marketItem = market.stockDatabase.get(50);

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, true);

        assertEquals(10, marketItem.getQuantityDeviation());

        assertEquals("10.52", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("5.52", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*1000) + 100);
            assertEquals(9.5, marketItem.getQuantityDeviation());
            assertEquals("10.49", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("5.49", String.format("%.2f", marketItem.getCurrentSellPrice()));
            
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*1000));
            assertEquals(9, marketItem.getQuantityDeviation());
            assertEquals("10.47", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("5.47", String.format("%.2f", marketItem.getCurrentSellPrice()));
        }
        catch (Exception e) {
            System.out.println(e);
        }

        marketPriceUpdater.stop();
    }

    @Test
    public void decreasingPrices2() {

        market.buyItem(50, 10);

        MarketItem marketItem = market.stockDatabase.get(50);

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, true);

        assertEquals(10, marketItem.getQuantityDeviation());

        assertEquals("10.52", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("5.52", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*2000) + 100);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        market.buyItem(50, 10);

        assertEquals(19, marketItem.getQuantityDeviation());

        assertEquals("11.10", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("6.10", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*1000));
            assertEquals(18.5, marketItem.getQuantityDeviation());
            assertEquals("11.06", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("6.06", String.format("%.2f", marketItem.getCurrentSellPrice()));
            
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*7000));
            assertEquals(15, marketItem.getQuantityDeviation());
            assertEquals("10.82", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("5.82", String.format("%.2f", marketItem.getCurrentSellPrice()));
        }
        catch (Exception e) {
            System.out.println(e);
        }

        marketPriceUpdater.stop();
    }

    @Test
    public void decreasingPrices3() {

        market.buyItem(50, 10);

        MarketItem marketItem = market.stockDatabase.get(50);

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, true);

        assertEquals(10, marketItem.getQuantityDeviation());

        assertEquals("10.52", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("5.52", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*2000) + 100);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        market.buyItem(50, 10);

        assertEquals(19, marketItem.getQuantityDeviation());

        assertEquals("11.10", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("6.10", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {          
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*8500));

            market.sellItem(50, 14);

            assertEquals(1, marketItem.getQuantityDeviation());
            assertEquals("10.05", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("5.05", String.format("%.2f", marketItem.getCurrentSellPrice()));

            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*5000));

            assertEquals(0, marketItem.getQuantityDeviation());
            assertEquals("10.00", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("5.00", String.format("%.2f", marketItem.getCurrentSellPrice()));
        }
        catch (Exception e) {
            System.out.println(e);
        }


        marketPriceUpdater.stop();
    }

    @Test
    public void decreasingPrices4() {

        market.buyItem(50, 10);

        MarketItem marketItem = market.stockDatabase.get(50);

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, true);

        assertEquals(10, marketItem.getQuantityDeviation());

        assertEquals("10.52", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("5.52", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*2000) + 100);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        market.buyItem(50, 10);

        assertEquals(19, marketItem.getQuantityDeviation());

        assertEquals("11.10", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("6.10", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {          
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*8500));

            market.sellItem(50, 24);

            assertEquals(-9, marketItem.getQuantityDeviation());
            assertEquals("9.53", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("4.53", String.format("%.2f", marketItem.getCurrentSellPrice()));

            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*9000));

            assertEquals(-4.5, marketItem.getQuantityDeviation());
            assertEquals("9.77", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("4.77", String.format("%.2f", marketItem.getCurrentSellPrice()));

            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*11000));

            assertEquals(0, marketItem.getQuantityDeviation());
            assertEquals("10.00", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("5.00", String.format("%.2f", marketItem.getCurrentSellPrice()));
        }
        catch (Exception e) {
            System.out.println(e);
        }


        marketPriceUpdater.stop();
    }

    @Test
    public void increasingPrices() {

        market.sellItem(50,60);

        MarketItem marketItem = market.stockDatabase.get(50);

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, true);

        assertEquals(-60, marketItem.getQuantityDeviation());

        assertEquals("0.01", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("0.01", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*1000) + 100);
            assertEquals(-59.5, marketItem.getQuantityDeviation());
            assertEquals("0.23", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("0.01", String.format("%.2f", marketItem.getCurrentSellPrice()));
            
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*19500));
            assertEquals(-50, marketItem.getQuantityDeviation());
            assertEquals("3.95", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("0.01", String.format("%.2f", marketItem.getCurrentSellPrice()));

            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*10000));
            assertEquals(-45, marketItem.getQuantityDeviation());
            assertEquals("5.31", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("0.31", String.format("%.2f", marketItem.getCurrentSellPrice()));
        }
        catch (Exception e) {
            System.out.println(e);
        }

        marketPriceUpdater.stop();
    }
}
