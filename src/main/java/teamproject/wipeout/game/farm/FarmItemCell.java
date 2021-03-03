package teamproject.wipeout.game.farm;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;

import java.io.FileNotFoundException;

public class FarmItemCell extends ListCell<FarmItem> {

    protected ImageView imageView;
    protected Label title;
    protected Label growth;
    protected Button harvestButton;

    protected final SpriteManager spriteManager;

    public FarmItemCell(EventHandler<ActionEvent> harvestAction, SpriteManager spriteManager) {
        super();
        this.spriteManager = spriteManager;

        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(true);
        this.imageView.setFitWidth(32);
        this.imageView.setFitHeight(32);

        this.title = new Label();
        this.growth = new Label();
        this.harvestButton = new Button("Harvest");
        this.harvestButton.setDefaultButton(true);
        this.harvestButton.setOnAction(harvestAction);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox hbox = new HBox(this.imageView, this.title, spacer, this.growth, this.harvestButton);
        hbox.setAlignment(Pos.CENTER_LEFT);
        this.setGraphic(hbox);
        this.setText(null);
    }

    @Override
    protected void updateItem(FarmItem farmItem, boolean empty) {
        super.updateItem(farmItem, empty);

        if (empty || farmItem == null) {
            this.setContentDisplay(ContentDisplay.TEXT_ONLY);
            return;
        } else {
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

        int growthStage = farmItem.getCurrentGrowthStage();
        this.growth.setText(String.valueOf(growthStage));

        boolean finishedGrowing = farmItem.getCurrentGrowthStage() == RowGrowthComponent.GROWTH_STAGES;
        this.harvestButton.setDisable(!finishedGrowing);
    }

}
