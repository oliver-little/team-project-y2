package teamproject.wipeout.game.potion;

import java.io.FileNotFoundException;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.OvalRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.component.render.particle.ParticleComponent;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters;
import teamproject.wipeout.engine.component.render.particle.ParticleParameters.ParticleSimulationSpace;
import teamproject.wipeout.engine.component.render.particle.property.EaseCurve;
import teamproject.wipeout.engine.component.render.particle.property.OvalParticle;
import teamproject.wipeout.engine.component.render.particle.property.ParticleBurst;
import teamproject.wipeout.engine.component.render.particle.property.EaseCurve.EaseType;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.util.SupplierGenerator;

public class PotionEntity extends GameEntity {
    public static final double THROW_SPEED = 600.0;
    public static final EaseCurve INV_EASE_CURVE = new EaseCurve(EaseType.EASE_OUT);

    public PotionEntity(GameScene scene, SpriteManager sm, Item potion, Point2D startPosition, Point2D endPosition) {
        super(scene);

        this.addComponent(new Transform(startPosition, 0, 1));

        Image potionSprite = null;
        InventoryComponent itemInventory = potion.getComponent(InventoryComponent.class);
        try {
            potionSprite = sm.getSpriteSet(itemInventory.spriteSheetName, itemInventory.spriteSetName)[0];
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Set up particle parameters
        Color potionColor = Color.BLACK;

        // Sample hardcoded pixel in potion image to get highlight color
        if (potionSprite.getWidth() > 9 && potionSprite.getHeight() > 8) {
            potionColor = potionSprite.getPixelReader().getColor(9, 8);
        }

        this.addComponent(new RenderComponent(new Point2D(-2, -2), new OvalRenderable(potionColor.brighter(), 4, 4)));

        ParticleParameters parameters = potionParticleFactory(potionColor);

        GameEntity particle = new GameEntity(scene);
        particle.addComponent(new Transform(0, 0, 0));
        ParticleComponent pc = new ParticleComponent(parameters);
        particle.addComponent(pc);
        particle.setParent(this);

        Point2D movementVector = endPosition.subtract(startPosition).normalize().multiply(THROW_SPEED);
        this.addComponent(new MovementComponent(movementVector.multiply(0.5), movementVector, 0.98));
        pc.play();
    }

    public static ParticleParameters potionParticleFactory(Color particleColor) {
        ParticleParameters parameters = new ParticleParameters(100, true, new OvalParticle(particleColor), ParticleSimulationSpace.WORLD, SupplierGenerator.rangeSupplier(0.5, 1.5), SupplierGenerator.rangeSupplier(1.0, 2.0), null, SupplierGenerator.staticSupplier(1.0), SupplierGenerator.circlePointSupplier(5, 10));
        parameters.setEmissionRate(50);
        parameters.setBursts(List.of(new ParticleBurst(0.0, SupplierGenerator.staticSupplier(200))));
        parameters.addUpdateFunction((particle, percentage) -> {
            double val = particle.getStartWidth() * INV_EASE_CURVE.apply(percentage);
            particle.width = val;
            particle.height = val;

        });

        return parameters;
    }
}
