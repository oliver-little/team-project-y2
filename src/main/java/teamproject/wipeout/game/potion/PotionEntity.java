package teamproject.wipeout.game.potion;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.ScriptComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.physics.GeometryUtil;
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
import teamproject.wipeout.engine.component.shape.Circle;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.util.SupplierGenerator;

public class PotionEntity extends GameEntity {

    public static final double POTION_EFFECT_RADIUS = 100.0;
    public static final double THROW_SPEED = 600.0;
    public static final double EXPLOSION_CONE_ANGLE = 20.0;
    public static final EaseCurve EASE_CURVE = new EaseCurve(EaseType.INVERSE_EASE_IN_OUT);

    private Item potion;
    private Collection<GameEntity> possibleEffectEntities;

    private Point2D startPosition;
    private Point2D endPosition;
    private double throwDistance;

    private ParticleComponent trail;
    private ParticleComponent explosion;

    private AudioComponent audio;
    private Runnable potionRemover;

    public PotionEntity(GameScene scene, SpriteManager sm, Item potion, Collection<GameEntity> possibleEffectEntities, Point2D startPosition, Point2D endPosition) {
        super(scene);

        this.potion = potion;
        this.possibleEffectEntities = possibleEffectEntities;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.throwDistance = endPosition.subtract(startPosition).magnitude();

        this.addComponent(new Transform(startPosition, 0, 1));

        this.addComponent(new ScriptComponent(onStep));

        audio = new AudioComponent();
        this.addComponent(audio);

        Image potionSprite = null;
        InventoryComponent itemInventory = potion.getComponent(InventoryComponent.class);
        try {
            potionSprite = sm.getSpriteSet(itemInventory.spriteSheetName, itemInventory.spriteSetName)[0];
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Point2D movementVector = endPosition.subtract(startPosition).normalize().multiply(THROW_SPEED);
;
        if (!potion.name.equals("Cheese")) {
            // Set up particle parameters
            Color potionColor = Color.BLACK;

            // Sample hardcoded pixel in potion image to get highlight color
            if (potionSprite.getWidth() > 9 && potionSprite.getHeight() > 8) {
                potionColor = potionSprite.getPixelReader().getColor(9, 8);
            }

            this.addComponent(new RenderComponent(new Point2D(-2, -2), new OvalRenderable(potionColor.brighter(), 4, 4)));

            GameEntity pTrail = new GameEntity(scene);
            pTrail.addComponent(new Transform(0, 0, 0));
            trail = new ParticleComponent(potionTrailFactory(potionColor));
            pTrail.addComponent(trail);
            pTrail.setParent(this);

            GameEntity pExplosion = new GameEntity(scene);
            pExplosion.addComponent(new Transform(0, 0, 0));
            explosion = new ParticleComponent(potionExplosionFactory(potionColor));
            pExplosion.addComponent(explosion);
            pExplosion.setParent(this);
            trail.play();
        }
        else {
            Point2D offset = new Point2D(-potionSprite.getWidth()/4, -potionSprite.getHeight()/4);
            this.addComponent(new RenderComponent(offset, new SpriteRenderable(potionSprite, 0.5)));
        }

        this.addComponent(new MovementComponent(movementVector.multiply(0.5), movementVector, 0.98));
    }

    public Integer getPotionID() {
        return this.potion.id;
    }

    public Point2D getStartPosition() {
        return this.startPosition;
    }

    public Point2D getEndPosition() {
        return this.endPosition;
    }

    public void setPotionRemover(Runnable potionRemover) {
        this.potionRemover = potionRemover;
    }

    public static ParticleParameters potionTrailFactory(Color particleColor) {
        ParticleParameters parameters = new ParticleParameters(100, true, new OvalParticle(particleColor), ParticleSimulationSpace.WORLD, SupplierGenerator.rangeSupplier(0.5, 1.5), SupplierGenerator.rangeSupplier(1.0, 2.0), null, SupplierGenerator.staticSupplier(1.0), SupplierGenerator.circlePointSupplier(5, 10));
        parameters.setEmissionRate(50);
        parameters.addUpdateFunction((particle, percentage) -> {
            double val = particle.getStartWidth() * EASE_CURVE.apply(percentage);
            particle.width = val;
            particle.height = val;
        });

        return parameters;
    }

    public static ParticleParameters potionExplosionFactory(Color particleColor) {

        ExplosionSupplier velocitySupplier = new ExplosionSupplier(20, 380);

        ParticleParameters parameters = new ParticleParameters(3.0, false, new OvalParticle(particleColor), ParticleSimulationSpace.WORLD, SupplierGenerator.rangeSupplier(3.5, 4.0), SupplierGenerator.rangeSupplier(1.0, 4.0), null, SupplierGenerator.staticSupplier(1.0), velocitySupplier);
        parameters.setBursts(List.of(new ParticleBurst(0.0, SupplierGenerator.staticSupplier(600))));
        parameters.addUpdateFunction((particle, percentage) -> {
            double val = particle.getStartWidth() * EASE_CURVE.apply(percentage);
            particle.width = val;
            particle.height = val;
            particle.velocity = particle.velocity.multiply(0.952);
        });
        return parameters;
    }

    public Consumer<Double> onStep = (timeStep) -> {
        if (this.getComponent(Transform.class).getWorldPosition().subtract(startPosition).magnitude() > throwDistance) {
            this.getComponent(ScriptComponent.class).requestDeletion = true;
            this.removeComponent(MovementComponent.class);
            this.removeComponent(RenderComponent.class);

            Point2D hitPosition = this.getComponent(Transform.class).getWorldPosition();
            Circle potionArea = new Circle(hitPosition.getX(), hitPosition.getY(), POTION_EFFECT_RADIUS);

            for (GameEntity entity : possibleEffectEntities) {
                if (entity.hasComponent(Transform.class)) {
                    Transform transform = entity.getComponent(Transform.class);

                    if (entity.hasComponent(RenderComponent.class)) {
                        RenderComponent rc = entity.getComponent(RenderComponent.class);

                        Rectangle r = new Rectangle(transform.getWorldPosition().getX(), transform.getWorldPosition().getY(), rc.getWidth(), rc.getHeight());
                        if (GeometryUtil.intersects(potionArea, r)) {
                            System.out.println("hit");
                            entity.addComponent(potion.getComponent(SabotageComponent.class));
                        }
                    }
                }
            }

            if (!potion.name.equals("Cheese")) {
                trail.parameters.setEmissionRate(0);

                explosion.onStop = () -> {
                    Platform.runLater(() -> this.destroyMyself());
                };

                explosion.parameters.addUpdateFunction((particle, percentage) -> {
                    if (particle.position.distance(hitPosition) > POTION_EFFECT_RADIUS) {
                        particle.position = hitPosition.add(particle.position.subtract(hitPosition).normalize().multiply(POTION_EFFECT_RADIUS));
                    }
                });

                audio.play("glassSmashing2.wav");
                explosion.play();

            } else {
                this.destroyMyself();
            }
        }
    };

    private void destroyMyself() {
        this.destroy();
        if (this.potionRemover != null) {
            this.potionRemover.run();
        }
    }

}
