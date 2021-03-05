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
        itemStore = assertDoesNotThrow(() -> new ItemStore("items.json"));
    }

    @BeforeEach
    public void setup() {
        market = assertDoesNotThrow(() -> new Market(itemStore));
    }
    
    @Test
    public void increasingPrices() {

        market.buyItem(50, 10);

        MarketItem marketItem = market.stockDatabase.get(50);

        MarketPriceUpdater marketPriceUpdater = new MarketPriceUpdater(market);

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
}
