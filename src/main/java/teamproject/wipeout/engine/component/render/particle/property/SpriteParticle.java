package teamproject.wipeout.engine.component.render.particle.property;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import teamproject.wipeout.engine.component.render.particle.type.ParticleRender;

public class SpriteParticle implements ParticleType {
    public Image sprite;

    public SpriteParticle(Image sprite) {
        this.sprite = sprite;
    }

    public ParticleRender renderFactory() {
        return (GraphicsContext gc, double x, double y, double width, double height) -> {
            gc.drawImage(sprite, x, y, width, height);
        };
    }
}