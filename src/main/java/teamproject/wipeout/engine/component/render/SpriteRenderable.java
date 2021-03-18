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
    public Point2D offset;

    /**
     * Creates an instance of SpriteRenderable
     * 
     * @param sprite The sprite image to render
     */
    public SpriteRenderable(Image sprite) {
        this.sprite = sprite;
        this.spriteScale = new Point2D(1, 1);
        this.offset = Point2D.ZERO;
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
        return this.sprite.getWidth() * this.spriteScale.getX();
    }

    public double getHeight() {
        return this.sprite.getHeight() * this.spriteScale.getY();
    }

    public void render(GraphicsContext gc, double x, double y, double scale){
        gc.drawImage(this.sprite, (x + this.offset.getX()) * scale, (y + this.offset.getY()) * scale, sprite.getWidth() * scale * this.spriteScale.getX(), sprite.getHeight() * scale * this.spriteScale.getY());
    }
}
