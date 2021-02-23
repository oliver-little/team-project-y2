package teamproject.wipeout.game.market;

import java.util.Map;

import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;

public class Market {

    public Map<Integer, Item> itemsForSale;

    public Map<Integer, MarketItem> stockDatabase;
    
    public Market() {
        try {
            itemsForSale = ItemStore.getItemFileFromJSON("items.JSON");
        } 
        catch(Exception e) {
            System.out.println("An error occured while loading the market database: " + e);
        }

        for (int i = 0; i < itemsForSale.size(); i++) {
            Item item = itemsForSale.get(i);
            MarketItem marketItem = new MarketItem(item.id, item.itemType, item.defaultBuy, item.defaultSell);
            stockDatabase.put(item.id, marketItem);
        }
    }
    
    
    public static void buy (Integer ID, int Quantity) {

    }

    public static void sell (Integer ID, int Quantity) {

    }
}
