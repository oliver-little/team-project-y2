package teamproject.wipeout.game.market.ui;

import java.util.Map;

import javafx.scene.layout.TilePane;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.market.MarketItem;

public class ItemMenu extends TilePane {
    public ItemMenu(Item[] displayItems, Map<Integer, MarketItem> marketItems) {
        super();

        this.setPrefSize(600, 800);
        this.setPrefColumns(5);

        for (Item item : displayItems) {
            this.getChildren().add(new MarketItemUI(item, marketItems.get(item.id)));
        }
    }
}