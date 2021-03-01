package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Pair;
import teamproject.wipeout.engine.entity.FarmEntity;
import teamproject.wipeout.engine.system.GrowthSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.item.components.SeedComponent;

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
        for (Pair<Item, Double> pair : farmRow) {
            cropX += FarmEntity.SQUARE_SIZE;
            if (pair == null) {
                continue;
            }
            Item currentItem = pair.getKey();
            PlantComponent crop = currentItem.getComponent(PlantComponent.class);
            int growthStage = GrowthSystem.getCurrentGrowthStage(crop.growthRate, pair.getValue());

            try {
                Image[] sprites = this.spriteManager.getSpriteSet(crop.spriteSheetName, crop.spriteSetName);
                if (growthStage >= sprites.length) {
                    growthStage = sprites.length - 1;
                }
                Image sprite = sprites[growthStage];

                Point2D spriteSize = this.rescaleToFitWidth(sprite.getWidth(), sprite.getHeight());
                double cropY = (y - spriteSize.getY() * 0.5);

                gc.drawImage(sprite, cropX * scale, cropY * scale, spriteSize.getX() * scale, spriteSize.getY() * scale);

            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }

    protected Point2D rescaleToFitWidth(double w, double h) {
        double scaleFactor = FarmEntity.SQUARE_SIZE / w;
        return new Point2D(w * scaleFactor, h * scaleFactor);
    }

}
