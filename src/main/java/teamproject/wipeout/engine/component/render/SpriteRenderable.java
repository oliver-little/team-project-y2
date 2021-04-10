package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Renderable to display a static sprite to the screen
 */
public class SpriteRenderable implements Renderable {
    
    public Image sprite;
    public Point2D spriteScale;

    /**
     * Creates an instance of SpriteRenderable
     * 
     * @param sprite The sprite image to render
     */
    public SpriteRenderable(Image sprite) {
        this.sprite = sprite;
        this.spriteScale = new Point2D(1, 1);
    }

    /**
     * Creates an instance of SpriteRenderable
     * 
     * @param sprite The sprite image to render
     * @param scale A scale to multiply the width and height by
     */
    public SpriteRenderable(Image sprite, double scale) {
        this.sprite = sprite;
        this.spriteScale = new Point2D(scale, scale);
    }

    /**
     * Creates an instance of SpriteRenderable
     * 
     * @param sprite The sprite image to render
     * @param scaleX A scale to multiply the width by
     * @param scaleY A scale to multiply the height by
     */
    public SpriteRenderable(Image sprite, double scaleX, double scaleY) {
        this.sprite = sprite;
        this.spriteScale = new Point2D(scaleX, scaleY);
    }

    public double getWidth() {
        if (sprite != null) {
            return this.sprite.getWidth() * this.spriteScale.getX();
        }
        else {
            return 0;
        }
    }

    public double getHeight() {
        if (sprite != null) {
            return this.sprite.getHeight() * this.spriteScale.getY();
        }
        else {
            return 0;
        }
    }

    public void render(GraphicsContext gc, double x, double y, double scale){
        if (this.sprite != null) {
            gc.drawImage(this.sprite, x * scale, y * scale, sprite.getWidth() * scale * this.spriteScale.getX(), sprite.getHeight() * scale * this.spriteScale.getY());
        }
    }
}
