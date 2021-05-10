package teamproject.wipeout.engine.system.render;

import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.CameraFollowComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

/**
 * System that updates a camera's position so that it tracks its target entity
*/
public class CameraFollowSystem implements GameSystem
{

	protected SignatureEntityCollector entityCollector;
	
    public CameraFollowSystem(GameScene e) {
        this.entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, CameraFollowComponent.class));
    }
	
	@Override
	public void cleanup() {
		this.entityCollector.cleanup();
		
	}

	@Override
	public void accept(Double timeStep) {
		List<GameEntity> entities = this.entityCollector.getEntities();
		
		for(int i=0; i < entities.size(); i++) {
			GameEntity ge = entities.get(i);
			CameraFollowComponent cf = ge.getComponent(CameraFollowComponent.class);
			cf.updateCameraPosition(timeStep, ge);
		}
	}
}
