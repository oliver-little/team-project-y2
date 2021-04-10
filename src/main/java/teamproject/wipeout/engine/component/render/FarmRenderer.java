package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

import java.io.IOException;

/**
 * Specifies how a farm is rendered.
 */
public class FarmRenderer implements Renderable {

    protected Point2D farmSize;
    protected SpriteManager spriteManager;

    public FarmRenderer(Point2D farmSize, SpriteManager spriteManager) {
        this.farmSize = farmSize;
        this.spriteManager = spriteManager;

        try {
            this.spriteManager.loadSpriteSheet("gameworld/soil-descriptor.json", "gameworld/soil.png");

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public double getWidth() {
        return this.farmSize.getX();
    }

    public double getHeight() {
        return this.farmSize.getY();
    }

    public void setFarmSize(Point2D farmSize) {
        this.farmSize = farmSize;
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        try {
            Point2D scaledFarmSize = this.farmSize.multiply(scale);
            double farmStartX = x * scale;
            double farmStartY = y * scale;
            double farmEndX = farmStartX + scaledFarmSize.getX();
            double farmEndY = farmStartY + scaledFarmSize.getY();
            double spriteWidth = FarmEntity.SQUARE_SIZE * scale;
            double spriteHeight = FarmEntity.SQUARE_SIZE * scale;

            // Render top row
            double topX = farmStartX;
            gc.drawImage(this.getFarmTile("top-left"), topX, farmStartY, spriteWidth, spriteHeight);
            Image top = this.getFarmTile("top");
            for (topX += spriteWidth; topX < farmEndX - spriteWidth; topX += spriteWidth) {
                gc.drawImage(top, topX, farmStartY, spriteWidth, spriteHeight);
            }
            gc.drawImage(this.getFarmTile("top-right"), topX, farmStartY, spriteWidth, spriteHeight);

            // Render centre rows
            Image left = this.getFarmTile("centre-left");
            Image centre = this.getFarmTile("centre");
            Image right = this.getFarmTile("centre-right");
            double centerY = y * scale;
            for (centerY += spriteHeight; centerY < farmEndY - spriteHeight; centerY += spriteHeight) {
                double centerX = x * scale;
                gc.drawImage(left, centerX, centerY, spriteWidth, spriteHeight);

                for (centerX += spriteWidth; centerX < farmEndX - spriteWidth; centerX += spriteWidth) {
                    gc.drawImage(centre, centerX, centerY, spriteWidth, spriteHeight);
                }

                gc.drawImage(right, centerX, centerY, spriteWidth, spriteHeight);
            }

            // Render bottom row
            double bottomX = x * scale;
            double bottomY = centerY;//farmEndY - spriteHeight;
            gc.drawImage(this.getFarmTile("bottom-left"), bottomX, bottomY, spriteWidth, spriteHeight);
            Image bottom = this.getFarmTile("bottom");
            for (bottomX += spriteWidth; bottomX < farmEndX - spriteWidth; bottomX += spriteWidth) {
                gc.drawImage(bottom, bottomX, bottomY, spriteWidth, spriteHeight);
            }
            gc.drawImage(this.getFarmTile("bottom-right"), bottomX, bottomY, spriteWidth, spriteHeight);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Returns an appropriate farm-soil sprite for a given tile name.
     *
     * @param tile Sprite tile name
     * @return Sprite for the given name.
     * @throws IOException If the sprite does not exist.
     */
    protected Image getFarmTile(String tile) throws IOException {
        return this.spriteManager.getSpriteSet("soil", tile)[0];
    }

}
