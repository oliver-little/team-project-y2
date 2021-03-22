package teamproject.wipeout.engine.component.render.particle.property;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import teamproject.wipeout.engine.component.render.particle.type.ParticleRender;

public class RoundRectParticle extends RectParticle {
    public double radius;

    public RoundRectParticle(Paint color, double radius) {
        super(color);

        this.radius = radius;
    }

    public ParticleRender renderFactory() {
        return (GraphicsContext gc, double x, double y, double width, double height) -> {
            gc.setFill(color);
            gc.fillRoundRect(x, y, width, height, radius, radius);
        };
    }
}