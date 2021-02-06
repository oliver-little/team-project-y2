package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class SpriteRenderable implements Renderable {
    
    public Image sprite;
    public Point2D offset;

    public SpriteRenderable(Image sprite) {
        this.sprite = sprite;
        this.offset = Point2D.ZERO;
    }

    public SpriteRenderable(Image sprite, Point2D offset) {
        this.sprite = sprite;
        this.offset = offset;
    }

    public void render(GraphicsContext gc, double x, double y){
        gc.drawImage(sprite, x + offset.getX(), y + offset.getY());
    }
}
