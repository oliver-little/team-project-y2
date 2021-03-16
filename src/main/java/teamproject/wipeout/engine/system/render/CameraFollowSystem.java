package teamproject.wipeout.engine.system.render;

import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.render.CameraFollowComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

public class CameraFollowSystem implements GameSystem
{

	protected SignatureEntityCollector _entityCollector;
	
    public CameraFollowSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, CameraFollowComponent.class));
    }
	
	@Override
	public void cleanup()
	{
		this._entityCollector.cleanup();
		
	}

	@Override
	public void accept(Double timeStep)
	{
		List<GameEntity> entities = this._entityCollector.getEntities();
		
		for(int i=0; i < entities.size(); i++) {
			GameEntity ge = entities.get(i);
			CameraFollowComponent cf = ge.getComponent(CameraFollowComponent.class);
			cf.updateCameraPosition(timeStep, ge);
		}
		
	}

}
