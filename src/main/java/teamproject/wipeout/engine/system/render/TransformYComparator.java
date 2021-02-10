package teamproject.wipeout.engine.system.render;

import java.util.Comparator;

import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.entity.GameEntity;

public class TransformYComparator implements Comparator<GameEntity> {
    @Override
    public int compare(GameEntity a, GameEntity b) {
        Transform transformA = a.getComponent(Transform.class);
        Transform transformB = b.getComponent(Transform.class);

        if (transformA != null && transformB != null) {
            return Double.compare(transformA.position.getY(), transformB.position.getY());
        }

        return 0;
    }
}