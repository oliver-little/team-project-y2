package teamproject.wipeout.engine.system;

import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.component.GravityComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.VelocityComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class GravitySystem implements GameSystem {
    
    protected SignatureEntityCollector _entityCollector;

    public GravitySystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, GravityComponent.class));
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = this._entityCollector.getEntities();

        for (GameEntity entity : entities) {
            Transform t = entity.getComponent(Transform.class);
            VelocityComponent m = entity.getComponent(VelocityComponent.class);
            GravityComponent g = entity.getComponent(GravityComponent.class);
            m.yspeed += g.g * timeStep;
            //t.position = t.position.add(m.xspeed * timeStep, m.yspeed * timeStep);
        }
    }
}
