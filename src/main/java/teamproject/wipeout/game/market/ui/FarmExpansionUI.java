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
import teamproject.wipeout.game.player.CurrentPlayer;

/**
 * Creates the UI for a single farm expansion element in the market.
 */
public class FarmExpansionUI extends VBox {

    public static final double FARM_EXPANSION_START_PRICE = 100.0;

    public static final int IMAGE_SIZE = 48;

    public static final double PRICE_MULTIPLIER = 1.5; //Amount to multiply price by after each purchase.

    public double expansionPrice; //Initial expansion price.

    public FarmExpansionUI(CurrentPlayer currentPlayer, SpriteManager spriteManager) {
        super();
        this.expansionPrice = FARM_EXPANSION_START_PRICE;

        this.getStyleClass().add("vbox");
        this.setPrefWidth(300);

        HBox titleLayout = new HBox();
        titleLayout.setAlignment(Pos.CENTER);

        titleLayout.getStyleClass().add("hbox");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ImageView iconView = null;
        try {
            Image sprite = spriteManager.getSpriteSet("arrow", "arrow")[0];
            iconView = new ImageView(sprite);
            iconView.setFitWidth(IMAGE_SIZE);
            iconView.setFitHeight(IMAGE_SIZE);
            iconView.setSmooth(false);
            iconView.setPreserveRatio(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Label title = new Label("Expand Farm");
        titleLayout.getChildren().addAll(iconView, spacer, title);

        HBox layout = new HBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(20);

        Button expandButton = new Button();
        expandButton.setPrefWidth(250);

        expandButton.textProperty().bind(Bindings.concat("Expand: " + String.format("%.2f",expansionPrice)));

        // Set buy click event
        expandButton.setOnAction((e) -> {
            if ((currentPlayer.hasEnoughMoney(expansionPrice)) && currentPlayer.getMyFarm() != null) {
                currentPlayer.setMoney(currentPlayer.getMoney() - expansionPrice);
                currentPlayer.getMyFarm().expandFarmByN(1);

                //Update price.
                expansionPrice *= PRICE_MULTIPLIER;
                expandButton.textProperty().bind(Bindings.concat("Expand: " + String.format("%.2f",expansionPrice)));

                //Check if the size has the farm has hit the maximum limit, if it has, disable the expand button.
                if (currentPlayer.getMyFarm().isMaxSize()) {
                    expandButton.textProperty().bind(Bindings.concat("MAX SIZE REACHED"));
                    expandButton.setDisable(true);
                }
            }

        });

        layout.getChildren().addAll(expandButton);

        this.getChildren().addAll(titleLayout, layout);
    }
}
