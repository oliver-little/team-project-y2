package teamproject.wipeout.game.market.ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.MarketItem;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.util.ImageUtil;

/**
 * Creates the UI for a single item in the market
 */
public class MarketItemUI extends VBox {

    public static final int IMAGE_SIZE = 48;

    public MarketItemUI(Item item, Market market, Player player, SpriteManager spriteManager, StackPane errorPane) {
        super();

        final double doubleCompare = 0.0000001;

        this.getStyleClass().add("vbox");
        this.setPrefWidth(300);

        InventoryComponent ic = item.getComponent(InventoryComponent.class);

        HBox titleLayout = new HBox();
        titleLayout.setAlignment(Pos.CENTER);

        titleLayout.getStyleClass().add("hbox");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ImageView iconView = null;
        try {
            Image sprite = spriteManager.getSpriteSet(ic.spriteSheetName, ic.spriteSetName)[0];

            // Scale the sprite up if it is too small (JavaFX attempts to anti-alias it)
            if (sprite.getWidth() < 48) {
                sprite = ImageUtil.scaleImage(sprite, IMAGE_SIZE / sprite.getWidth());
            }

            iconView = new ImageView(sprite);
            iconView.setFitWidth(IMAGE_SIZE);
            iconView.setFitHeight(IMAGE_SIZE);
            iconView.setSmooth(false);
            iconView.setPreserveRatio(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Label title = new Label(item.name);
        titleLayout.getChildren().addAll(iconView, spacer, title);

        HBox buySellLayout = new HBox();
        buySellLayout.setAlignment(Pos.CENTER);
        buySellLayout.setSpacing(20);

        Spinner<Integer> quantity = new Spinner<Integer>(1, item.getComponent(InventoryComponent.class).stackSizeLimit, 1, 1);

        // Make spinner editable and only allow number input
        quantity.editableProperty().set(true);
        quantity.getEditor().textProperty().addListener((o, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantity.getEditor().setText(oldValue);
            }
            else if (newValue.equals("")) {
                quantity.getEditor().setText(oldValue);
            }
        });

        MarketItem marketItem = market.stockDatabase.get(item.id);

        Button buy = new Button();
        buy.setPrefWidth(85);

        // Set button text to update with quantity and buy price changes
        buy.textProperty().bind(Bindings.concat("Buy: ").concat(Bindings.createStringBinding(() -> {
            return String.format("%.2f", (market.calculateTotalCost(item.id, quantity.valueProperty().get(), true)));
        }, quantity.getValueFactory().valueProperty(), marketItem.quantityDeviationProperty())));

        // Set buy click event
        buy.setOnAction((e) -> {
            player.buyItem(market, item.id, quantity.getValue());
        });


        Button sell = new Button();
        sell.setPrefWidth(85);

        //Check if the item is sellable.
        if (Math.abs(marketItem.getDefaultSellPrice() - (-1)) < doubleCompare) {
            sell.setDisable(true);
            sell.textProperty().bind(Bindings.concat("Sell: 0.00"));
        }
        else {
            // Set sell price to update with quantity and sell price changes
            sell.textProperty().bind(Bindings.concat("Sell: ").concat(Bindings.createStringBinding(() -> {
                return String.format("%.2f", (market.calculateTotalCost(item.id, quantity.valueProperty().get(), false)));
            }, quantity.getValueFactory().valueProperty(), marketItem.quantityDeviationProperty())));

            // Set sell click event
            sell.setOnAction((e) -> {
                player.sellItem(market, item.id, quantity.getValue());
            });
        }

        buySellLayout.getChildren().addAll(buy, sell, quantity);

        this.getChildren().addAll(titleLayout, buySellLayout);
    }
}
