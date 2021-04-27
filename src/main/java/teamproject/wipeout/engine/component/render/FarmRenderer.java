package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.farm.entity.FarmEntity;

import java.io.IOException;

/**
 * Specifies how a farm field is rendered.
 *
 * @see Renderable
 */
public class FarmRenderer implements Renderable {

    private final SpriteManager spriteManager;

    private Point2D farmSize;

    /**
     * Creates an instance of a {@code FarmRenderer}.
     *
     * @param farmSize      {@link Point2D} size of the farm
     * @param spriteManager Current {@link SpriteManager}
     */
    public FarmRenderer(Point2D farmSize, SpriteManager spriteManager) throws IOException {
        this.spriteManager = spriteManager;

        this.farmSize = farmSize;

        this.spriteManager.loadSpriteSheet("gameworld/soil-descriptor.json", "gameworld/soil.png");
    }

    /**
     * @return {@code double} value of the farm's width
     */
    public double getWidth() {
        return this.farmSize.getX();
    }

    /**
     * @return {@code double} value of the farm's height
     */
    public double getHeight() {
        return this.farmSize.getY();
    }

    /**
     * Sets the farm size for the renderer
     *
     * @param farmSize {@link Point2D} value of the new farm size
     */
    public void setFarmSize(Point2D farmSize) {
        this.farmSize = farmSize;
    }

    /**
     * Renders the farm field to the given GraphicsContext.
     *
     * @param gc    The GraphicsContext to Render to
     * @param x     The x position to render at
     * @param y     The y position to render at
     * @param scale The scale to render this Renderable at (1 for normal size)
     */
    public void render(GraphicsContext gc, double x, double y, double scale) {
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
        double bottomY = centerY;
        gc.drawImage(this.getFarmTile("bottom-left"), bottomX, bottomY, spriteWidth, spriteHeight);
        Image bottom = this.getFarmTile("bottom");
        for (bottomX += spriteWidth; bottomX < farmEndX - spriteWidth; bottomX += spriteWidth) {
            gc.drawImage(bottom, bottomX, bottomY, spriteWidth, spriteHeight);
        }
        gc.drawImage(this.getFarmTile("bottom-right"), bottomX, bottomY, spriteWidth, spriteHeight);
    }

    /**
     * Returns an appropriate farm-soil sprite for the given tile name.
     *
     * @param tile Sprite tile name
     * @return {@code Image} sprite for the given tile name, or {@code null} if the sprite does not exist
     */
    protected Image getFarmTile(String tile) {
        try {
            return this.spriteManager.getSpriteSet("soil", tile)[0];

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return null;
    }

}
