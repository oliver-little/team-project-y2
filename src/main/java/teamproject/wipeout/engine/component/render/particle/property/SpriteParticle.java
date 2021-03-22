package teamproject.wipeout.engine.component.render.particle.property;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.render.particle.type.ParticleRender;

/**
 * Represents a particle that renders a sprite 
 */
public class SpriteParticle implements ParticleType {
    public Image sprite;

    /**
     * Creates a new instance of SpriteParticle
     * @param sprite The sprite image to draw 
     */
    public SpriteParticle(Image sprite) {
        this.sprite = sprite;
    }

    public ParticleRender renderFactory() {
        return (GraphicsContext gc, double x, double y, double width, double height) -> {
            gc.drawImage(sprite, x, y, width, height);
        };
    }
}