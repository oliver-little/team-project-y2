package teamproject.wipeout.engine.entity.collector;

import java.util.function.Consumer;

import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;

public class FunctionalEntityCollector extends BaseEntityCollector {

    public Consumer<GameEntity> addComponentFunction;
    public Consumer<GameEntity> removeComponentFunction;
    public Consumer<GameEntity> removeEntityFunction;

    public FunctionalEntityCollector(GameScene scene, Consumer<GameEntity> addComponent, Consumer<GameEntity> removeComponent, Consumer<GameEntity> removeEntity) {
        super(scene);
        this.addComponentFunction = addComponent;
        this.removeComponentFunction = removeComponent;
        this.removeEntityFunction = removeEntity;
    }

    protected void addComponent(GameEntity entity) {
        if (addComponentFunction != null) {
            this.addComponentFunction.accept(entity);
        }
    }

    protected void removeComponent(GameEntity entity) {
        if (removeComponentFunction != null) {
            this.removeComponentFunction.accept(entity);
        }
    }

    protected void removeEntity(GameEntity entity) {
        if (removeEntityFunction != null) {
            this.removeEntityFunction.accept(entity);
        }
    }
}
