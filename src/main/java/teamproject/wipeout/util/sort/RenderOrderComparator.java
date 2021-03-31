package teamproject.wipeout.util.sort;

import java.util.Comparator;

import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.entity.GameEntity;

public class RenderOrderComparator implements Comparator<GameEntity> {
    @Override
    public int compare(GameEntity a, GameEntity b) {
        Transform transformA = a.getComponent(Transform.class);
        Transform transformB = b.getComponent(Transform.class);

        if (transformA != null && transformB != null) {
            int zComp = Integer.compare(transformA.getZPosition(), transformB.getZPosition());

            if (zComp != 0) {
                return zComp;
            }
            
            RenderComponent rcA = a.getComponent(RenderComponent.class);
            RenderComponent rcB = b.getComponent(RenderComponent.class);

            if (rcA != null && rcB != null) {
                return Double.compare(transformA.getWorldPosition().getY() + rcA.getHeight(), transformB.getWorldPosition().getY() + rcB.getHeight());
            }
            else {
                return Double.compare(transformA.getWorldPosition().getY(), transformB.getWorldPosition().getY());
            }
        }

        return 0;
    }
}