package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.farm.RowGrowthComponent;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.FarmItem;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantComponent;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Specifies how a row of items is rendered.
 */
public class ItemsRowRenderer implements Renderable {

    public final ArrayList<FarmItem> farmRow;
    public ItemStore itemStore;
    public SpriteManager spriteManager;

    protected HashMap<String, Image> currentSprites;

    public ItemsRowRenderer(ArrayList<FarmItem> row, SpriteManager spriteManager, ItemStore itemStore) {
        this.farmRow = row;
        this.itemStore = itemStore;
        this.spriteManager = spriteManager;

        this.currentSprites = new HashMap<String, Image>();
    }

    public double getWidth() {
        return FarmEntity.SQUARE_SIZE * farmRow.size();
    }

    public double getHeight() {
        return FarmEntity.SQUARE_SIZE;
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        double itemX = x;
        for (FarmItem farmItem : farmRow) {
            itemX += FarmEntity.SQUARE_SIZE;
            if (farmItem == null) {
                continue;
            }
            Item currentItem = farmItem.get();
            if (currentItem == null) {
                continue;
            }

            int growthStage = farmItem.getCurrentGrowthStage();

            PlantComponent plant = currentItem.getComponent(PlantComponent.class);
            String idString = currentItem.id + "." + growthStage;
            Image sprite = this.currentSprites.get(idString);

            if (sprite == null) {
                try {
                    Image[] sprites = this.spriteManager.getSpriteSet(plant.spriteSheetName, plant.spriteSetName);
                    sprite = sprites[growthStage];
                    this.currentSprites.put(idString, sprite);

                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                    continue;
                }
            }

            Point2D spriteSize = this.rescaleToFitWidth(plant.width, sprite.getWidth(), sprite.getHeight());
            double itemY = (y - spriteSize.getY() * 0.5);

            gc.drawImage(sprite, itemX * scale, itemY * scale, spriteSize.getX() * scale, spriteSize.getY() * scale);
        }
    }

    protected Point2D rescaleToFitWidth(int squareScale, double w, double h) {
        double scaleFactor = squareScale * FarmEntity.SQUARE_SIZE / w;
        return new Point2D(w * scaleFactor, h * scaleFactor);
    }

}
