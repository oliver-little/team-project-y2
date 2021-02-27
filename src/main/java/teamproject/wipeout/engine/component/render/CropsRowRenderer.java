package teamproject.wipeout.engine.component.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Pair;
import teamproject.wipeout.engine.entity.FarmEntity;
import teamproject.wipeout.engine.system.GrowthSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantableComponent;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Specifies how a row of crops is rendered.
 */
public class CropsRowRenderer implements Renderable {

    public final ArrayList<Pair<Item, Double>> farmRow;
    public ItemStore itemStore;
    public SpriteManager spriteManager;

    public CropsRowRenderer(ArrayList<Pair<Item, Double>> row, SpriteManager spriteManager, ItemStore itemStore) {
        this.farmRow = row;
        this.itemStore = itemStore;
        this.spriteManager = spriteManager;
    }

    public double getWidth() {
        return FarmEntity.SQUARE_SIZE * farmRow.size();
    }

    public double getHeight() {
        return FarmEntity.SQUARE_SIZE;
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        double cropX = x;
        double cropY = y - (FarmEntity.SQUARE_SIZE * 0.78);
        for (Pair<Item, Double> pair : farmRow) {
            cropX += FarmEntity.SQUARE_SIZE;
            if (pair == null) {
                continue;
            }
            Item currentItem = pair.getKey();
            PlantableComponent crop = currentItem.getComponent(PlantableComponent.class);
            int growthStage = GrowthSystem.getCurrentGrowthStage(crop.growthRate, pair.getValue());

            try {
                Image[] sprites = this.spriteManager.getSpriteSet(crop.growthSpriteSheetName, crop.growthSpriteSetName);
                if (growthStage >= sprites.length) {
                    growthStage = sprites.length - 1;
                }
                Image sprite = sprites[growthStage];

                double spriteWidth = sprite.getWidth();
                double spriteHeight = sprite.getHeight();
                double centeredX = cropX;// - spriteWidth/2;
                double centeredY = cropY;// - spriteHeight/2;

                gc.drawImage(sprite, centeredX * scale, centeredY * scale, spriteWidth * scale, spriteHeight * scale);

            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }

}
