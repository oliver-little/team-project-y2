package teamproject.wipeout.game.farm.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;

import java.io.FileNotFoundException;

public class FarmItemCell extends ListCell<FarmItem> {

    protected ImageView imageView;
    protected Label title;
    protected Label growth;
    protected Button harvestButton;

    protected final SpriteManager spriteManager;

    public FarmItemCell(FarmItemCellDelegate harvestAction, SpriteManager spriteManager) {
        super();
        this.spriteManager = spriteManager;

        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(true);
        this.imageView.setFitWidth(32);
        this.imageView.setFitHeight(32);

        this.title = new Label();

        this.growth = new Label();
        this.growth.setPadding(new Insets(0, 5, 0, 5));

        this.harvestButton = new Button("Harvest");
        this.harvestButton.setDefaultButton(true);
        this.harvestButton.setOnAction((event) -> harvestAction.harvest(this.getItem()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox hbox = new HBox(this.imageView, this.title, spacer, this.growth, this.harvestButton);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5, 0, 5, 0));
        this.setGraphic(hbox);
        this.setText(null);
    }

    @Override
    public void updateSelected(boolean selected) {
        return; // disables ListCell selection
    }

    @Override
    protected void updateItem(FarmItem farmItem, boolean empty) {
        super.updateItem(farmItem, empty);

        if (empty || farmItem == null) {
            this.setBackground(null);
            this.setContentDisplay(ContentDisplay.TEXT_ONLY);
            return;
        } else {
            this.setBackground(new Background(new BackgroundFill(Color.WHEAT, new CornerRadii(10), new Insets(5, 0, 5, 0))));
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        Item item = farmItem.get();

        InventoryComponent inventory = item.getComponent(InventoryComponent.class);
        try {
            Image itemImage = this.spriteManager.getSpriteSet(inventory.spriteSheetName, inventory.spriteSetName)[0];
            this.imageView.setImage(itemImage);
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }

        this.title.setText(item.name);

        int growthPercentage = farmItem.getCurrentGrowthPercentage();
        this.growth.setText(growthPercentage + " %");

        this.harvestButton.setDisable(growthPercentage != 100);
    }

}
