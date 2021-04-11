package teamproject.wipeout.game.farm.entity;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.particle.ParticleComponent;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;
import teamproject.wipeout.engine.component.render.particle.property.EaseCurve;
import teamproject.wipeout.engine.component.render.particle.property.OvalParticle;
import teamproject.wipeout.engine.component.render.particle.property.ParticleBurst;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.util.SupplierGenerator;

public class PlantParticleEntity extends GameEntity {
    
    public PlantParticleEntity(GameScene scene, double x, double y) {
        super(scene);

        ParticleParameters plantEffect = new ParticleParameters(3, false, 
            new OvalParticle(new Color(0.2078, 0.1216, 0.0902, 1.0)), 
            ParticleSimulationSpace.LOCAL, 
            SupplierGenerator.rangeSupplier(0.1, 0.5), 
            SupplierGenerator.rangeSupplier(1.0, 3.0), 
            SupplierGenerator.rangeSupplier(1.0, 3.0), 
            SupplierGenerator.staticSupplier(1.0),
            SupplierGenerator.rangeSupplier(new Point2D(-70, -60), new Point2D(70, -2)));

        plantEffect.setBursts(List.of(new ParticleBurst(0.0, SupplierGenerator.rangeSupplier(50, 100))));
        plantEffect.addUpdateFunction((particle, percentage, timeStep) -> {
            particle.width = particle.getStartWidth() * EaseCurve.EASE_OUT.apply(percentage);
            particle.height = particle.getStartHeight() * EaseCurve.EASE_OUT.apply(percentage);
            particle.velocity = particle.velocity.add(0, 80 * timeStep).multiply(0.95);
        });

        this.addComponent(new Transform(x, y, 0));
        ParticleComponent particleComponent = new ParticleComponent(plantEffect);
        particleComponent.onStop = () -> this.destroy();
        this.addComponent(particleComponent);
        particleComponent.play();
    }
}
