package teamproject.wipeout.engine.component.render;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.entity.GameEntity;

public class CameraFollowComponent implements GameComponent
{

	private GameEntity target;
	
	private double damping = 5f;
	private Point2D offset = new Point2D(0,0);
		
	public CameraFollowComponent(GameEntity target) {
		this.target = target;
	}

	public CameraFollowComponent(GameEntity target, Point2D offset) {
		this.target = target;
		Point2D p = target.getComponent(Transform.class).getWorldPosition();
		this.offset = p.add(offset);
	}
	
	public void setTarget(GameEntity target) {
		this.target = target;
	}
	
	public void setDamping(double damping) {
		this.damping = damping;
	}
	
	public void updateCameraPosition(double timestep, GameEntity camera) {
		Point2D destinationPos = target.getComponent(Transform.class).getWorldPosition().add(offset);
		Transform cameraTransform = camera.getComponent(Transform.class);
		Point2D currentPos = cameraTransform.getWorldPosition();
		cameraTransform.setPosition(currentPos.interpolate(destinationPos, timestep*damping));
	}
	
	@Override
	public String getType()
	{
		return "cameraFollow";
	}

}
