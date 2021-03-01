package teamproject.wipeout.game.market;

import java.util.HashMap;
import java.util.Map;

import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.TradableComponent;

/**
 * Defines the market in which players can buy and sell goods for. The market also regulates the quantites and prices of the goods for sale.
 */
public class Market {

    public Map<Integer, Item> itemsForSale;

    public Map<Integer, MarketItem> stockDatabase;
    
    /**
     * Default constructor for market, this takes in all available items from a JSON file and creates a stock database setting default prices and quantities.
     */
    public Market(Map<Integer, Item> itemsForSale) {
        //TODO Add sabotage and task support.
        //TODO Link with systems to reduce quantities over time.

        this.itemsForSale = itemsForSale;

        stockDatabase = new HashMap<>();

        for (Item item : itemsForSale.values()) {
            TradableComponent tradableComponent = item.getComponent(TradableComponent.class);
            if (tradableComponent == null) {
                continue;
            }
            MarketItem marketItem = new MarketItem(item.id, tradableComponent);
            stockDatabase.put(item.id, marketItem);
        }
    }
    
    
    /**
     * This function is run when a player purchases an item from the market.
     * @param id The item ID they want to buy.
     * @param quantity The quantity of the item they want to buy.
     * @return The total cost that the market will charge them or -1 if you cannot buy the item.
     */
    public double buyItem(int id, int quantity) {

        if (!stockDatabase.containsKey(id)) {
            System.out.println("The requested item is not for sale.");
            return -1;
        }

        MarketItem item = stockDatabase.get(id);

        if (item.getDefaultSellPrice() < 0) { 
            return quantity * item.getDefaultBuyPrice();
        }

        //TODO Add a check that the player has enough money to buy the item & has enough inventory space.
        //TODO Add to inventory and remove money here.

        double totalCost = calculateTotalCost(id, quantity, true);
        item.incrementQuantityDeviation(quantity);

        return totalCost;
    }

    /**
     * This function is run when a player sells an item from the market.
     * @param id The item ID they want to sell.
     * @param quantity The quantity of the item they want to sell.
     * @return The total cost that the market will pay them or -1 if you cannot sell the item.
     */
    public double sellItem(int id, int quantity) {

        if (!stockDatabase.containsKey(id)) {
            System.out.println("The requested item is not for sale.");
            return -1;
        }

        MarketItem item = stockDatabase.get(id);

        if (item.getDefaultSellPrice() < 0) {
            System.out.println("Cannot sell this kind of item.");
            return -1;
        }

        //TODO Remove from inventory and add money here.

        double totalCost = calculateTotalCost(id, quantity, false);
        item.decrementQuantityDeviation(quantity);

        return totalCost;
    }

    /**
     * Calculates the cost of buying/selling a certain number of items. This function specifically accounts for the game's pricing model.
     * @param id The item id.
     * @param quantity The number of items to buy/sell.
     * @param buy Whether the player is buying or selling. True for buying, false for selling.
     * @return The cost to buy/sell.
     */
    public double calculateTotalCost(int id, int quantity, boolean buy) {

        MarketItem item = stockDatabase.get(id);

        double totalCost = 0;

        double costDeviation = 0;

        double quantityDeviation = item.getQuantityDeviation();

        double price;

        if (buy) {
            price = item.getDefaultBuyPrice();
        }
        else {
            price = item.getDefaultSellPrice();
        }

        for (int i = 0; i < quantity; i++) {
            costDeviation = MarketItem.costFunction(quantityDeviation) + price;
            if (costDeviation <= 0.01) {
                costDeviation = 0.01;
            }
            totalCost += costDeviation;

            if (buy) {
                quantityDeviation++;
            }
            else {
                quantityDeviation--;
            }
            
        }

        return totalCost;
    }
}
