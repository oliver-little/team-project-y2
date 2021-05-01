package teamproject.wipeout.game.market;

import javafx.application.Platform;
import javafx.util.Pair;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.TradableComponent;
import teamproject.wipeout.networking.client.GameClient;
import teamproject.wipeout.networking.data.GameUpdate;
import teamproject.wipeout.networking.data.GameUpdateType;
import teamproject.wipeout.networking.data.MarketOperationRequest;
import teamproject.wipeout.networking.state.MarketState;
import teamproject.wipeout.networking.state.StateUpdatable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Defines the market in which players can buy and sell goods for. The market also regulates the quantites and prices of the goods for sale.
 */
public class Market implements StateUpdatable<MarketState> {

    public Map<Integer, MarketItem> stockDatabase;

    public Supplier<Integer> serverIDGetter;
    public Consumer<GameUpdate> serverUpdater;

    private Supplier<GameClient> clientSupplier;

    private final boolean isLocal;

    /**
     * Default constructor for market, this takes in all available items from a JSON file and creates a stock database setting default prices and quantities.
     */
    public Market(ItemStore itemsForSale, boolean local) {

        stockDatabase = new HashMap<>();

        for (Item item : itemsForSale.getData().values()) {
            TradableComponent tradableComponent = item.getComponent(TradableComponent.class);
            if (tradableComponent == null) {
                continue;
            }
            MarketItem marketItem = new MarketItem(item.id, tradableComponent);
            stockDatabase.put(item.id, marketItem);
        }

        isLocal = local;
    }

    public void setClientSupplier(Supplier<GameClient> clientSupplier) {
        this.clientSupplier = clientSupplier;
    }

    /**
     * Gets the current state of the market.
     *
     * @return Current {@link MarketState}
     */
    public MarketState getCurrentState() {
        return null; // TODO fix
    }

    /**
     * Updates the market based on a given {@link MarketState}.
     *
     * @param newState New state of the market
     */
    public void updateFromState(MarketState newState) {
        Platform.runLater(() -> {
            this.stockDatabase.get(newState.getItemID()).setQuantityDeviation(newState.getItemDeviation());
        });
    }

    /**
     * This function is run when a player purchases an item from the market.
     *
     * @param id The item ID they want to buy.
     * @param quantity The quantity of the item they want to buy.
     * @return The total cost that the market will charge them or -1 if you cannot buy the item.
     */
    public double buyItem(int id, int quantity) {

        if (!stockDatabase.containsKey(id)) {
            return -1;
        }

        MarketItem item = stockDatabase.get(id);

        double totalCost;
        if (item.getDefaultSellPrice() < 0) {
            totalCost = quantity * item.getDefaultBuyPrice();
        } else {
            totalCost = calculateTotalCost(id, quantity, true);
        }

        if (isLocal) {
            this.sendRequest(new MarketOperationRequest(id, quantity, true));

        } else {
            item.incrementQuantityDeviation(quantity);
            sendMarketUpdate(item);
        }

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
            return -1;
        }

        MarketItem item = stockDatabase.get(id);

        if (item.getDefaultSellPrice() < 0) {
            return -1;
        }

        Pair<Double, Integer> costDeviationPair = calculateTotalCostAndDeviations(id, quantity, false);
        double totalCost = costDeviationPair.getKey();
        double deviations = costDeviationPair.getValue();

        if (isLocal) {
            sendRequest(new MarketOperationRequest(id, quantity, false));

        } else {
            if (deviations != -1) {
                item.decrementQuantityDeviation(deviations);
            }
            else {
                item.decrementQuantityDeviation(quantity);
            }
            sendMarketUpdate(item);
        }

        return totalCost;
    }

    /**
     * Calculates the cost of buying/selling a certain number of items.
     * @param id The item id.
     * @param quantity The number of items to buy/sell.
     * @param buy Whether the player is buying or selling. True for buying, false for selling.
     * @return The cost to buy/sell.
     */
    public double calculateTotalCost(int id, int quantity, boolean buy) {
        return calculateTotalCostAndDeviations(id, quantity, buy).getKey();
    }

    /**
     * Calculates the cost of buying/selling a certain number of items. This function additionally accounts for the game's pricing model. This function returns the quantity deviation for which the cost of an item falls below 0.01, used for the MarketPriceUpdater and market pricing models.
     * @param id The item id.
     * @param quantity The number of items to buy/sell.
     * @param buy Whether the player is buying or selling. True for buying, false for selling.
     * @return A pair of values: The cost to buy/sell and the quantity deviation at which the price of an item falls below 0.01.
     */
    public Pair<Double, Integer> calculateTotalCostAndDeviations(int id, int quantity, boolean buy) {

        MarketItem item = stockDatabase.get(id);

        double totalCost = 0;

        double costDeviation = 0;

        double quantityDeviation = item.getQuantityDeviation();

        double price;

        int numDeviationsBeforeZero = -1;

        if (buy) {
            price = item.getDefaultBuyPrice();
        }
        else {
            price = item.getDefaultSellPrice();
        }

        for (int i = 0; i < quantity; i++) {
            costDeviation = MarketItem.costFunction(quantityDeviation) + price;
            if (costDeviation <= 0.01) {
                if (numDeviationsBeforeZero == -1) {
                    numDeviationsBeforeZero = i;
                }
                costDeviation = 0.01;
            }
            else {
                if (buy) {
                    quantityDeviation++;
                }
                else {
                    quantityDeviation--;
                }
            }

            totalCost += costDeviation; 
        }

        return new Pair<Double, Integer>(totalCost, numDeviationsBeforeZero);
    }

    /**
     * (Server) sends a new market state to all clients.
     * (Server-side method)
     */
    protected void sendMarketUpdate(MarketItem marketItem) {
        if (serverUpdater != null) {
            MarketState  marketState = new MarketState(marketItem.getID(), marketItem.getQuantityDeviation());

            GameUpdate update = new GameUpdate(GameUpdateType.MARKET_STATE, serverIDGetter.get(), marketState);
            serverUpdater.accept(update);
        }
    }

    /**
     * (Client) sends a {@link MarketOperationRequest} to the server.
     * (Client-side method)
     *
     * @param request Request for the server
     */
    private void sendRequest(MarketOperationRequest request) {
        if (this.clientSupplier == null) {
            return;
        }
        GameClient client = this.clientSupplier.get();
        if (client != null) {
            client.send(new GameUpdate(GameUpdateType.REQUEST, client.getClientID(), request));
        }
    }

}
