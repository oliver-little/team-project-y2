package teamproject.wipeout.game.entity;

import java.util.function.Supplier;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.particle.ParticleComponent;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;
import teamproject.wipeout.engine.component.render.particle.property.EaseCurve;
import teamproject.wipeout.engine.component.render.particle.property.RectParticle;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.util.SupplierGenerator;

public class ParticleEntity extends GameEntity {

    private ParticleComponent particleComponent;
    
    public ParticleEntity(GameScene scene, int zIndex, ParticleParameters parameters) {
        super(scene);

        this.addComponent(new Transform(0, 0, zIndex));
        particleComponent = new ParticleComponent(parameters);
        this.addComponent(particleComponent);
    }

    public ParticleParameters getParameters() {
        return particleComponent.parameters;
    }

    public boolean isPlaying() {
        return particleComponent.isPlaying();
    }

    public void play() {
        particleComponent.play();
    }

    public void stop() {
        particleComponent.stop();
    }
}
