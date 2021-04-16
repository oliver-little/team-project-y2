package teamproject.wipeout.game.player;

import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AIPlayerHelper {

    public final MarketItem[] seedStockDatabase;
    public final Map<Integer, MarketItem> grownUpStockDatabase;
    public final Map<Integer, MarketItem> potionStockDatabase;

    public AIPlayerHelper(Market market) {
        Map<Integer, MarketItem> stockDatabase = market.stockDatabase;
        this.seedStockDatabase = stockDatabase.values().stream()
                .filter((mItm) -> mItm.getID() > 27 && mItm.getID() < 50)
                .toArray((arrSize) -> new MarketItem[arrSize]);

        this.grownUpStockDatabase = new HashMap<Integer, MarketItem>();
        for (Iterator<Map.Entry<Integer, MarketItem>> it = stockDatabase.entrySet().stream().filter((entry) -> entry.getKey() < 27).iterator(); it.hasNext();) {
            Map.Entry<Integer, MarketItem> entry = it.next();
            this.grownUpStockDatabase.put(entry.getKey(), entry.getValue());
        }

        this.potionStockDatabase = new HashMap<Integer, MarketItem>();
        for (Iterator<Map.Entry<Integer, MarketItem>> it = stockDatabase.entrySet().stream().filter((entry) -> entry.getKey() > 50).iterator(); it.hasNext();) {
            Map.Entry<Integer, MarketItem> entry = it.next();
            this.potionStockDatabase.put(entry.getKey(), entry.getValue());
        }
    }

}
