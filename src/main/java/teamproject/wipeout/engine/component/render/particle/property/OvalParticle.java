package teamproject.wipeout.engine.component.render.particle.property;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import teamproject.wipeout.engine.component.render.particle.type.ParticleRender;

/**
 * Represents an Oval (or circular) particle
 */
public class OvalParticle implements ParticleType {
    public Paint color;

    /**
     * Creates a new instance of OvalParticle
     * @param color The color to draw the particle as
     */
    public OvalParticle(Paint color) {
        this.color = color;
    }

    public ParticleRender renderFactory() {
        return (GraphicsContext gc, double x, double y, double width, double height) -> {
            gc.setFill(color);
            gc.fillOval(x, y, width, height);
        };
    }
}