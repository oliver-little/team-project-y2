package teamproject.wipeout.game.market;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MarketTest {
    
    @Test
    public void testBuying1Item() {

        Market market = new Market();

        market.buyItem(1, 1);

        double buyPrice = Market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = Market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals(-1, Market.stockDatabase.get(1).getQuantity());

        assertEquals("2.02", String.format("%.2f", buyPrice));
        assertEquals("1.02", String.format("%.2f", sellPrice));
    }

    @Test
    public void testBuying50Items() {

        Market market = new Market();

        market.buyItem(1, 50);

        double buyPrice = Market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = Market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals("3.19", String.format("%.2f", buyPrice));
        assertEquals("2.00", String.format("%.2f", sellPrice));
    }

    @Test
    public void testBuying100Items() {

        Market market = new Market();

        market.buyItem(1, 100);

        double buyPrice = Market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = Market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals("59.29", String.format("%.2f", buyPrice));
        assertEquals("2.00", String.format("%.2f", sellPrice));
    }

    @Test
    public void testSelling1Item() {

        Market market = new Market();

        market.sellItem(1, 1);

        double buyPrice = Market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = Market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals("1.98", String.format("%.2f", buyPrice));
        assertEquals("0.98", String.format("%.2f", sellPrice));
    }

    @Test
    public void testSelling40Items() {

        Market market = new Market();

        market.sellItem(1, 40);

        double buyPrice = Market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = Market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals("1.16", String.format("%.2f", buyPrice));
        assertEquals("0.16", String.format("%.2f", sellPrice));
    }

    @Test
    public void testSelling50Items() {

        Market market = new Market();

        market.sellItem(1, 50);

        double buyPrice = Market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = Market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals("1.00", String.format("%.2f", buyPrice));
        assertEquals("0.01", String.format("%.2f", sellPrice));
    }

    @Test
    public void testSelling100Items() {

        Market market = new Market();

        market.sellItem(1, 100);

        double buyPrice = Market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = Market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals("1.00", String.format("%.2f", buyPrice));
        assertEquals("0.01", String.format("%.2f", sellPrice));
    }

    @Test
    public void testBuyingAndSellingItems() {

        Market market = new Market();

        market.sellItem(1, 100);
        market.buyItem(1, 120);

        double buyPrice = Market.stockDatabase.get(1).getCurrentBuyPrice();
        double sellPrice = Market.stockDatabase.get(1).getCurrentSellPrice();

        assertEquals("2.36", String.format("%.2f", buyPrice));
        assertEquals("1.36", String.format("%.2f", sellPrice));
    }
    
}
