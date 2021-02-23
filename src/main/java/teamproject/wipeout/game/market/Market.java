package teamproject.wipeout.game.market;

import java.util.Map;

import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;

/**
 * Defines the market in which players can buy and sell goods for. The market also regulates the quantites and prices of the goods for sale.
 */
public class Market {

    public Map<Integer, Item> itemsForSale;

    public Map<Integer, MarketItem> stockDatabase;

    public static final Integer MAXMARKETCAPACITY = 500;

    public static final Integer MINMARKETCAPACITY = 0;
    
    /**
     * Default constructor for market, this takes in all available items from a JSON file and creates a stock database setting default prices and quantities.
     */
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
    
    
    /**
     * This function is run when a player purchases an item from the market.
     * @param ID The item ID they want to buy.
     * @param Quantity The quantity of the item they want to buy.
     * @return True if the items were bought successfully, otherwise false.
     */
    public boolean buy (Integer ID, int Quantity) {

        //TODO Needs functionality to add/remove money from the user's account & add/remove the items from their inventories.
        if (!stockDatabase.containsKey(ID)) {
            System.out.println("The requested item is not for sale.");
        }
        else if (hasMinBreached(ID, Quantity)) {
            System.out.println("The market hasn't got enough stock for that purchase.");
            return false;
        }
        
        return false;
    }

    /**
     * This function is run when a player sells an item from the market.
     * @param ID The item ID they want to sell.
     * @param Quantity The quantity of the item they want to sell.
     * @return True if the items were sold successfully, otherwise false.
     */
    public boolean sell (Integer ID, int Quantity) {

        //TODO Needs functionality to add/remove money from the user's account & add/remove the items from their inventories.
        if (!stockDatabase.containsKey(ID)) {
            System.out.println("The requested item is not for sale.");
        }
        else if (hasMaxBreached(ID, Quantity)) {
            System.out.println("The market hasn't got enough capacity for that sale.");
            return false;
        }

        return false;
    }

    private boolean hasMaxBreached (Integer ID, int Quantity) {
        if ((stockDatabase.get(ID).getQuantity()) + Quantity > MAXMARKETCAPACITY) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean hasMinBreached (Integer ID, int Quantity) {
        if ((stockDatabase.get(ID).getQuantity()) - Quantity < MINMARKETCAPACITY) {
            return true;
        }
        else {
            return false;
        }
    }
}
