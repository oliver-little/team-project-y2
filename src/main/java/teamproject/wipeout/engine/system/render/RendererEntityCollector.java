package teamproject.wipeout.engine.system.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.BaseEntityCollector;

public class RendererEntityCollector extends BaseEntityCollector {

    public static final Set<Class<? extends GameComponent>> signature = Set.of(Transform.class, RenderComponent.class);
    protected List<GameEntity> dynamicEntityList;
    protected List<GameEntity> staticEntityList;

    public RendererEntityCollector(GameScene scene) {
        super(scene);

        this.dynamicEntityList = new ArrayList<GameEntity>();
        this.staticEntityList = new ArrayList<GameEntity>();
    }

    public List<GameEntity> getEntities() {
        return this.dynamicEntityList;
    }

    public List<GameEntity> getStaticEntities() {
        return this.staticEntityList;
    }

    protected void _addComponent(GameEntity entity) {
        if (this._testComponent(entity)) {
            RenderComponent rc = entity.getComponent(RenderComponent.class);
            if (rc.isStatic() && !this.staticEntityList.contains(entity)) {
                this.staticEntityList.add(entity);
            }
            else if (!rc.isStatic() && !this.dynamicEntityList.contains(entity)) {
                this.dynamicEntityList.add(entity);
            }
        }
    }

    protected void _removeComponent(GameEntity entity) {
        if (!this._testComponent(entity)) {
            this.dynamicEntityList.remove(entity);
            this.staticEntityList.remove(entity);
        }
    }

    protected void _removeEntity(GameEntity entity) {
        this.dynamicEntityList.remove(entity);
        this.staticEntityList.remove(entity);
    }
    

    private boolean _testComponent(GameEntity entity) {
        for (Class<? extends GameComponent> componentClass : signature) {
            if (!entity.hasComponent(componentClass)) {
                return false;
            }
        }
        return true;
    }
}
