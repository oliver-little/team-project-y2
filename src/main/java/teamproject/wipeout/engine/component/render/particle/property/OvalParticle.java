package teamproject.wipeout.engine.component.render.particle.property;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import teamproject.wipeout.engine.component.render.particle.type.ParticleRender;

public class OvalParticle implements ParticleType {
    public Paint color;

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