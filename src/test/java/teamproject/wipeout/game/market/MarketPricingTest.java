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
        //TODO FIX THIS
        market.buyItem(50, 10);

        double buyPrice = market.stockDatabase.get(50).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(50).getCurrentSellPrice();

        assertEquals(10, market.stockDatabase.get(50).getQuantityDeviation());

        assertEquals("10.52", String.format("%.2f", buyPrice));
        assertEquals("5.52", String.format("%.2f", sellPrice));

        try {
            Thread.sleep(2000);
            assertEquals("10.47", String.format("%.2f", buyPrice));
            assertEquals("5.47", String.format("%.2f", sellPrice));
        }
        catch (Exception e) {
            System.out.println(e);
        }
        

    }
}
