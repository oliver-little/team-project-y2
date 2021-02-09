package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Renderable to display a static sprite to the screen
 */
public class SpriteRenderable implements Renderable {
    
    public Image sprite;
    public Point2D offset;

    /**
     * Creates an instance of SpriteRenderable
     * 
     * @param sprite The sprite image to render
     */
    public SpriteRenderable(Image sprite) {
        this.sprite = sprite;
        this.offset = Point2D.ZERO;
    }

    /**
     * Creates an instance of SpriteRenderable
     * 
     * @param sprite The sprite image to render
     * @param offset An offset position to display at, from the top left of this object
     */
    public SpriteRenderable(Image sprite, Point2D offset) {
        this.sprite = sprite;
        this.offset = offset;
    }

    public double getWidth() {
        return this.sprite.getWidth();
    }

    public double getHeight() {
        return this.sprite.getHeight();
    }

    public void render(GraphicsContext gc, double x, double y, double scale){
        if (scale != 1) {
            gc.drawImage(this.sprite, x + offset.getX() * scale, y + offset.getY() * scale, this.sprite.getWidth() * scale, this.sprite.getHeight() * scale);
        }
        else {
            gc.drawImage(this.sprite, x + offset.getX(), y + offset.getY());
        }
    }
}
