package teamproject.wipeout.game.market.ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.util.ImageUtil;

/**
 * Creates the UI for purchasable tasks in the market.
 */
public class MarketTaskUI extends VBox {
    public static final int IMAGE_SIZE = 48;

    /**
     * Initialise the Market UI cards for purchasable tasks
     * @param task - the task to be displayed
     * @param item - the repsonsible item for the task
     * @param market - the market
     * @param currentPlayer - the current player
     * @param spriteManager - the spriteManager in order to display the related icon
     */
    public MarketTaskUI(Task task, Item item, Market market, CurrentPlayer currentPlayer, SpriteManager spriteManager) {
        super();

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

        Label title = new Label(task.descriptionWithoutMoney);
        titleLayout.getChildren().addAll(iconView, spacer, title);

        HBox layout = new HBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(20);

        Button buy = new Button();
        buy.setPrefWidth(250);

        buy.textProperty().bind(Bindings.concat("Buy: " + String.format("%.2f", (task.priceToBuy))));

        buy.setOnAction((e) -> {
            if (currentPlayer.buyTask(task)) {
                buy.textProperty().bind(Bindings.concat("TASK ALREADY PURCHASHED"));
                buy.setDisable(true);
            }
        });

        layout.getChildren().addAll(buy);

        this.getChildren().addAll(titleLayout, layout);
    }
}
