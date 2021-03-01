package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.entity.FarmEntity;
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
    }

    public double getWidth() {
        return this.farmSize.getX();
    }

    public double getHeight() {
        return this.farmSize.getY();
    }

    public void render(GraphicsContext gc, double x, double y, double scale) {
        try {
            Point2D scaledFarmSize = this.farmSize.multiply(scale);
            double farmEndX = x + scaledFarmSize.getX();
            double farmEndY = y + scaledFarmSize.getY();
            double spriteWidth = FarmEntity.SQUARE_SIZE * scale;
            double spriteHeight = FarmEntity.SQUARE_SIZE * scale;

            // Render top row
            double topX = x;
            gc.drawImage(this.getFarmTile("top-left"), topX, y, spriteWidth, spriteHeight);
            Image top = this.getFarmTile("top");
            for (topX = x + spriteWidth; topX < farmEndX - spriteWidth; topX += spriteWidth) {
                gc.drawImage(top, topX, y, spriteWidth, spriteHeight);
            }
            gc.drawImage(this.getFarmTile("top-right"), topX, y, spriteWidth, spriteHeight);

            // Render centre rows
            Image left = this.getFarmTile("centre-left");
            Image centre = this.getFarmTile("centre");
            Image right = this.getFarmTile("centre-right");
            for (double centerY = y + spriteHeight; centerY < farmEndY - spriteHeight; centerY += spriteHeight) {
                double centerX = x;
                gc.drawImage(left, centerX, centerY, spriteWidth, spriteHeight);

                for (centerX += spriteWidth; centerX < farmEndX - spriteWidth; centerX += spriteWidth) {
                    gc.drawImage(centre, centerX, centerY, spriteWidth, spriteHeight);
                }

                gc.drawImage(right, centerX, centerY, spriteWidth, spriteHeight);
            }

            // Render bottom row
            double bottomX = x;
            double bottomY = farmEndY - spriteHeight;
            gc.drawImage(this.getFarmTile("bottom-left"), bottomX, bottomY, spriteWidth, spriteHeight);
            Image bottom = this.getFarmTile("bottom");
            for (bottomX = x + spriteWidth; bottomX < farmEndX - spriteWidth; bottomX += spriteWidth) {
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
        this.spriteManager.loadSpriteSheet("soil-descriptor.json", "soil.png");
        return this.spriteManager.getSpriteSet("soil", tile)[0];
    }

}
