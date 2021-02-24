package teamproject.wipeout.game.market;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import teamproject.wipeout.game.item.ItemStore;

public class MarketTest {
    
    @Test
    public void testBuying1Item() {

        Market market = assertDoesNotThrow(() -> new Market(ItemStore.getItemFileFromJSON("items.JSON")));

        double totalCost = market.buyItem(1, 1);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(1, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("2.05", String.format("%.2f", buyPrice));
        assertEquals("1.05", String.format("%.2f", sellPrice));

        assertEquals("2.00", String.format("%.2f", totalCost));
    }

    @Test
    public void testBuying50Items() {

        Market market = assertDoesNotThrow(() -> new Market(ItemStore.getItemFileFromJSON("items.JSON")));

        double totalCost = market.buyItem(1, 50);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(50, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("8.05", String.format("%.2f", buyPrice));
        assertEquals("7.05", String.format("%.2f", sellPrice));

        assertEquals("199.64", String.format("%.2f", totalCost));
    }

    @Test
    public void testBuying100Items() {

        Market market = assertDoesNotThrow(() -> new Market(ItemStore.getItemFileFromJSON("items.JSON")));

        double totalCost = market.buyItem(1, 100);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(100, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("76.20", String.format("%.2f", buyPrice));
        assertEquals("75.20", String.format("%.2f", sellPrice));

        assertEquals("1627.40", String.format("%.2f", totalCost));
    }

    @Test
    public void testSelling1Item() {

        Market market = assertDoesNotThrow(() -> new Market(ItemStore.getItemFileFromJSON("items.JSON")));

        double totalCost = market.sellItem(1, 1);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(-1, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("1.95", String.format("%.2f", buyPrice));
        assertEquals("0.95", String.format("%.2f", sellPrice));

        assertEquals("1.00", String.format("%.2f", totalCost));
    }

    @Test
    public void testSelling40Items() {

        Market market = assertDoesNotThrow(() -> new Market(ItemStore.getItemFileFromJSON("items.JSON")));

        double totalCost = market.sellItem(1, 40);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(-40, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("0.01", String.format("%.2f", buyPrice));
        assertEquals("0.01", String.format("%.2f", sellPrice));

        assertEquals("10.07", String.format("%.2f", totalCost));
    }

    @Test
    public void testSelling100Items() {

        Market market = assertDoesNotThrow(() -> new Market(ItemStore.getItemFileFromJSON("items.JSON")));

        double totalCost = market.sellItem(1, 100);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(-100, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("0.01", String.format("%.2f", buyPrice));
        assertEquals("0.01", String.format("%.2f", sellPrice));

        assertEquals("10.67", String.format("%.2f", totalCost));
    }

    @Test
    public void testBuyingAndSellingItems() {

        Market market = assertDoesNotThrow(() -> new Market(ItemStore.getItemFileFromJSON("items.JSON")));

        double totalCost = market.buyItem(1, 50);
        totalCost = market.sellItem(1, 40);

        double buyPrice = market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(10, market.stockDatabase.get(1).getQuantityDeviation());

        assertEquals("2.52", String.format("%.2f", buyPrice));
        assertEquals("1.52", String.format("%.2f", sellPrice));

        assertEquals("142.88", String.format("%.2f", totalCost));
    }
    
}