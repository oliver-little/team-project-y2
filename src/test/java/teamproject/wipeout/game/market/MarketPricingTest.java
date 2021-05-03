package teamproject.wipeout.game.market;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.util.resources.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that the market prices change as expected when items are bought/sold from the market.
 * In addition, this further tests the market price updater - checking that it automatically increases/decreases over time by "walking" the prices over our mathematical model.
 * The test waits for a set amount of time and checks the expected price after the wait against the actual result.
 */
public class MarketPricingTest {
    private static ItemStore itemStore;
    private Market market;

    @BeforeAll
    public static void initialization() {
        ResourceLoader.setTargetClass(MarketPricingTest.class);
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

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, false);

        assertEquals(10, marketItem.getQuantityDeviation());

        assertEquals("13.75", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("8.75", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*1000) + 100);
            assertEquals(9.9, marketItem.getQuantityDeviation());
            assertEquals("13.69", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("8.69", String.format("%.2f", marketItem.getCurrentSellPrice()));
            
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*1000));
            assertEquals(9.8, marketItem.getQuantityDeviation());
            assertEquals("13.63", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("8.63", String.format("%.2f", marketItem.getCurrentSellPrice()));
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

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, false);

        assertEquals(10, marketItem.getQuantityDeviation());

        assertEquals("13.75", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("8.75", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*2000) + 100);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        market.buyItem(50, 10);

        assertEquals(19.8, marketItem.getQuantityDeviation());

        assertEquals("25.90", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("20.90", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*1000));
            assertEquals(19.7, marketItem.getQuantityDeviation());
            assertEquals("25.67", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("20.67", String.format("%.2f", marketItem.getCurrentSellPrice()));
            
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*7000));
            assertEquals("24.19", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("19.19", String.format("%.2f", marketItem.getCurrentSellPrice()));
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

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, false);

        assertEquals(10, marketItem.getQuantityDeviation());

        assertEquals("13.75", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("8.75", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*2000) + 100);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        market.buyItem(50, 10);

        assertEquals(19.8, marketItem.getQuantityDeviation());

        assertEquals("25.90", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("20.90", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {          
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*8000));

            market.sellItem(50, 18);

            assertEquals("10.26", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("5.26", String.format("%.2f", marketItem.getCurrentSellPrice()));
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

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, false);

        assertEquals(10, marketItem.getQuantityDeviation());

        assertEquals("13.75", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("8.75", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*2000) + 100);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        market.buyItem(50, 10);

        assertEquals(19.8, marketItem.getQuantityDeviation());

        assertEquals("25.90", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("20.90", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {          
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*8000));

            market.sellItem(50, 25);

            assertEquals("8.38", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("3.38", String.format("%.2f", marketItem.getCurrentSellPrice()));
        }
        catch (Exception e) {
            System.out.println(e);
        }


        marketPriceUpdater.stop();
    }

    @Test
    public void increasingPrices() {

        market.sellItem(50,17);

        MarketItem marketItem = market.stockDatabase.get(50);

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market, false);

        assertEquals(-14, marketItem.getQuantityDeviation());

        assertEquals("4.78", String.format("%.2f", marketItem.getCurrentBuyPrice()));
        assertEquals("0.01", String.format("%.2f", marketItem.getCurrentSellPrice()));

        try {
            Thread.sleep((MarketPriceUpdater.TIMEFREQUENCY*10000) + 100);
            assertEquals("5.40", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("0.40", String.format("%.2f", marketItem.getCurrentSellPrice()));
            
            market.buyItem(50,12);
            assertEquals("9.74", String.format("%.2f", marketItem.getCurrentBuyPrice()));
            assertEquals("4.74", String.format("%.2f", marketItem.getCurrentSellPrice()));
        }
        catch (Exception e) {
            System.out.println(e);
        }

        marketPriceUpdater.stop();
    }
}
