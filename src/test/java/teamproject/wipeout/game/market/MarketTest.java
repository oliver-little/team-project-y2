package teamproject.wipeout.game.market;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.util.resources.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarketTest {

    private static ItemStore itemStore;
    private Market market;

    @BeforeAll
    public static void initialization() {
        ResourceLoader.setTargetClass(MarketTest.class);
        itemStore = assertDoesNotThrow(() -> new ItemStore("marketitemstest.json"));
    }

    @BeforeEach
    public void setup() {
        market = assertDoesNotThrow(() -> new Market(itemStore, false));
    }
    
    @Test
    public void testBuying1Item() {

        double totalCost = market.buyItem(1, 1);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(1, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("2.26", String.format("%.2f", buyPrice));
        assertEquals("1.26", String.format("%.2f", sellPrice));

        assertEquals("2.00", String.format("%.2f", totalCost));
    }

    @Test
    public void testBuying50Items() {

        double totalCost = market.buyItem(1, 50);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(50, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("1098.63", String.format("%.2f", buyPrice));
        assertEquals("1097.63", String.format("%.2f", sellPrice));

        assertEquals("7382.09", String.format("%.2f", totalCost));
    }

    @Test
    public void testBuying100Items() {

        double totalCost = market.buyItem(1, 100);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(100, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("1202606.28", String.format("%.2f", buyPrice));
        assertEquals("1202605.28", String.format("%.2f", sellPrice));

        assertEquals("8002938.76", String.format("%.2f", totalCost));
    }

    @Test
    public void testSelling1Item() {

        double totalCost = market.sellItem(1, 1);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(-1, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("1.74", String.format("%.2f", buyPrice));
        assertEquals("0.74", String.format("%.2f", sellPrice));

        assertEquals("1.00", String.format("%.2f", totalCost));
    }

    @Test
    public void testSelling40Items() {

        double totalCost = market.sellItem(1, 40);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(-4, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("0.96", String.format("%.2f", buyPrice));
        assertEquals("0.01", String.format("%.2f", sellPrice));

        assertEquals("2.81", String.format("%.2f", totalCost));
    }

    @Test
    public void testSelling100Items() {

        double totalCost = market.sellItem(1, 100);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(-4, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("0.96", String.format("%.2f", buyPrice));
        assertEquals("0.01", String.format("%.2f", sellPrice));

        assertEquals("3.41", String.format("%.2f", totalCost));
    }

    @Test
    public void testBuyingAndSellingItems() {

        double totalCost = market.buyItem(1, 50);
        totalCost = market.sellItem(1, 40);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(10, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("5.75", String.format("%.2f", buyPrice));
        assertEquals("4.75", String.format("%.2f", sellPrice));

        assertEquals("8400.82", String.format("%.2f", totalCost));
    }
    
}
