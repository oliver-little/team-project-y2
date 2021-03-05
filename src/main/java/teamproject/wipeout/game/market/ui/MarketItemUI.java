package teamproject.wipeout.game.market.ui;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketItem;

/**
 * Creates the UI for a single item in the market
 */
public class MarketItemUI extends VBox {
    public MarketItemUI(Item item, Market market, SpriteManager spriteManager) {
        super();

        InventoryComponent ic = item.getComponent(InventoryComponent.class);

        HBox titleLayout = new HBox();
        ImageView iconView = null;
        try {
            iconView = new ImageView(spriteManager.getSpriteSet(ic.spriteSheetName, ic.spriteSetName)[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Label title = new Label(item.name);
        titleLayout.getChildren().addAll(iconView, title);

        HBox buySellLayout = new HBox();
        Spinner<Integer> quantity = new Spinner<Integer>(1, item.getComponent(InventoryComponent.class).stackSizeLimit, 1, 1);

        MarketItem marketItem = market.stockDatabase.get(item.id);

        Button buy = new Button();
        // Set button text to update with quantity and buy price changes
        buy.textProperty().bind(Bindings.concat("Buy: ").concat(Bindings.createDoubleBinding(() -> {
            return (double) Math.round(market.calculateTotalCost(item.id, quantity.valueProperty().get(), true) * 100d) / 100d;
        }, quantity.getValueFactory().valueProperty(), marketItem.currentBuyPrice())));

        Button sell = new Button();

        // Set sell price to update with quantity and sell price changes
        sell.textProperty().bind(Bindings.concat("Sell: ").concat(Bindings.createDoubleBinding(() -> {
            return (double) Math.round(market.calculateTotalCost(item.id, quantity.valueProperty().get(), false) * 100d) / 100d;
        }, quantity.getValueFactory().valueProperty(), marketItem.currentSellPrice())));

        buySellLayout.getChildren().addAll(buy, sell, quantity);

        this.getChildren().addAll(titleLayout, buySellLayout);
    }
}
