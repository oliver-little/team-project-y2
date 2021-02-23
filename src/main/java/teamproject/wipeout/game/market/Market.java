package teamproject.wipeout.game.market;

import java.util.Map;

import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.Item.ItemType;

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
        //TODO Add sabotage and task support.
        //TODO Link with systems to reduce quantities over time.
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
     * @param id The item ID they want to buy.
     * @param quantity The quantity of the item they want to buy.
     * @return True if the items were bought successfully, otherwise false.
     */
    public boolean buyItem (Integer id, int quantity) {

        if (!stockDatabase.containsKey(id)) {
            System.out.println("The requested item is not for sale.");
            return false;
        }
        else if (hasMinBreached(id, quantity)) {
            System.out.println("The market hasn't got enough stock for that purchase.");
            return false;
        }

        if (stockDatabase.get(id).getItemType() == ItemType.CONSTRUCTABLE || stockDatabase.get(id).getItemType() == ItemType.USABLE) {
            //TODO Add to inventory and remove money here.
            return true;
        }

        //TODO Add a check that the player has enough money to buy the item & has enough inventory space.
        //TODO Add to inventory and remove money here.

        stockDatabase.get(id).decrementQuantity(quantity);
        stockDatabase.get(id).updatePrices();

        return true;
    }

    /**
     * This function is run when a player sells an item from the market.
     * @param id The item ID they want to sell.
     * @param quantity The quantity of the item they want to sell.
     * @return True if the items were sold successfully, otherwise false.
     */
    public boolean sellItem (Integer id, int quantity) {

        if (!stockDatabase.containsKey(id)) {
            System.out.println("The requested item is not for sale.");
            return false;
        }
        else if (hasMaxBreached(id, quantity)) {
            System.out.println("The market hasn't got enough capacity for that sale.");
            return false;
        }

        if (stockDatabase.get(id).getItemType() == ItemType.CONSTRUCTABLE || stockDatabase.get(id).getItemType() == ItemType.USABLE) {
            System.out.println("Cannot sell constructable or usable item types.");
            return false;
        }

        //TODO Remove from inventory and add money here.

        stockDatabase.get(id).incrementQuantity(quantity);
        stockDatabase.get(id).updatePrices();

        return true;
    }

    private boolean hasMaxBreached (Integer id, int quantity) {
        if ((stockDatabase.get(id).getQuantity()) + quantity > MAXMARKETCAPACITY) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean hasMinBreached (Integer id, int quantity) {
        if ((stockDatabase.get(id).getQuantity()) - quantity < MINMARKETCAPACITY) {
            return true;
        }
        else {
            return false;
        }
    }
}
