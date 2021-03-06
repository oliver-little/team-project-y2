package teamproject.wipeout.engine.system;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import teamproject.wipeout.engine.component.PlayerAnimatorComponent;
import teamproject.wipeout.engine.component.physics.FacingDirection;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.AnimatedSpriteRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.FunctionalSignatureCollector;
import teamproject.wipeout.util.EventObserver;

/**
 * Animates entities with a PlayerAnimatorComponent based on their acceleration (which is tied to the input they receive)
 */
public class PlayerAnimatorSystem implements EventSystem {

    private FunctionalSignatureCollector entityCollector;

    private Map<GameEntity, EventObserver<FacingDirection>> listeningTo;
    private Map<GameEntity, AnimatedSpriteRenderable> spriteRenderables;

    /**
     * Creates a new instance of PlayerAnimatorSystem
     * 
     * @param scene The GameScene this system is part of
     */
    public PlayerAnimatorSystem(GameScene scene) {
        this.entityCollector = new FunctionalSignatureCollector(scene, Set.of(MovementComponent.class, RenderComponent.class, PlayerAnimatorComponent.class), this.add, this.remove, this.remove);

        this.listeningTo = new HashMap<>();
        this.spriteRenderables = new HashMap<>();
    }

    public void cleanup() {
        this.entityCollector.cleanup();
    }

    /**
     * Updates the animation on a given entity when its facing direction changes
     * 
     * @param entity The entity to update (which has a PlayerAnimatorComponent and a RenderComponent)
     * @param direction The direction the entity is facing
     */
    public void updateAnimation(GameEntity entity, FacingDirection direction) {
        PlayerAnimatorComponent animator = entity.getComponent(PlayerAnimatorComponent.class);
        AnimatedSpriteRenderable render = this.spriteRenderables.get(entity);

        switch (direction) {
            case UP:
                render.setFrames(animator.getUpFrames());
                break;
            case RIGHT:
                render.setFrames(animator.getRightFrames());
                break;
            case DOWN:
                render.setFrames(animator.getDownFrames());
                break;
            case LEFT:
                render.setFrames(animator.getLeftFrames());
                break;
            case NONE:
                render.setFrames(animator.getIdleFrames());
                break;
        }
    }

    /**
     * Lambda function to begin listening to an entity's facingDirectionChanged event
     */
    private Consumer<GameEntity> add = (entity) -> {
        if (!this.listeningTo.containsKey(entity)) {
            MovementComponent m = entity.getComponent(MovementComponent.class);
            EventObserver<FacingDirection> onDirectionChange = (direction) -> this.updateAnimation(entity, direction);
            this.listeningTo.put(entity, onDirectionChange);
            m.facingDirectionChanged.addObserver(onDirectionChange);

            RenderComponent render = entity.getComponent(RenderComponent.class);
            AnimatedSpriteRenderable newRenderable = new AnimatedSpriteRenderable(entity.getComponent(PlayerAnimatorComponent.class).getIdleFrames(), 15);
            render.addRenderable(newRenderable);
            this.spriteRenderables.put(entity, newRenderable);
        }
    };

    /**
     * Lambda function to check if the system is listening to an entity's facingDirectionChanged event, then remove the listener if it is.
     */
    private Consumer<GameEntity> remove = (entity) -> {
        if (this.listeningTo.containsKey(entity)) {
            entity.getComponent(MovementComponent.class).facingDirectionChanged.removeObserver(this.listeningTo.remove(entity));
            
            entity.getComponent(RenderComponent.class).removeRenderable(this.spriteRenderables.remove(entity));
        }
    };
}
