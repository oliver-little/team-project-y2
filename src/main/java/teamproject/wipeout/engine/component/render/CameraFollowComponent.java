package teamproject.wipeout.engine.component.render;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.entity.GameEntity;

/**
 * Component that can be added to an entity with a CameraComponent to track a target entity.
 */
public class CameraFollowComponent implements GameComponent
{
	/**
	 * The game entity to be tracked by the camera
	 */
	private GameEntity target;
	
	/**
	 * How closely the camera should stick to the target.
	 * The higher the stickiness the closer the camera will stick to the target's movement.
	 * 0 stickiness indicates that the camera will not update when target moves.
	 */
	private double stickiness = 5f;
	
	/**
	 * Camera position offset from the target
	 */
	private ObjectProperty<Point2D> offset;

	// Skips interpolation for one frame
	private boolean skipInterpolation = false;
	
	public CameraFollowComponent(GameEntity target) {
		this.target = target;
		this.offset = new SimpleObjectProperty<>(Point2D.ZERO);
	}

	public CameraFollowComponent(GameEntity target, ObjectProperty<Point2D> offset) {
		this.target = target;
		this.offset = offset;
		this.offset.addListener((observable, oldValue, newValue) -> {
			skipInterpolation = true;
		});
	}

	public void setTarget(GameEntity target) {
		this.target = target;
	}
	
	public void setStickiness(double stickiness) {
		this.stickiness = stickiness;
	}
	
	/**
	 * Updates the camera position smoothly relative to target
	 * @param timestep 
	 * @param camera The camera to be updated
	 */
	public void updateCameraPosition(double timestep, GameEntity camera) {
		Point2D destinationPos = target.getComponent(Transform.class).getWorldPosition().add(offset.getValue());
		Transform cameraTransform = camera.getComponent(Transform.class);
		if (skipInterpolation) {
			cameraTransform.setPosition(destinationPos);
			skipInterpolation = false;
		}
		else {
			Point2D currentPos = cameraTransform.getWorldPosition();
			cameraTransform.setPosition(currentPos.interpolate(destinationPos, timestep*stickiness));
		}
	}
	
	@Override
	public String getType()
	{
		return "cameraFollow";
	}

	/*
      	//camera follows player
      	float cameraZoom = camera.getComponent(CameraComponent.class).zoom; 
      	RenderComponent targetRC = nge.getComponent(RenderComponent.class);
		Point2D targetDimensions = new Point2D(targetRC.getWidth(), targetRC.getHeight()).multiply(0.5);
        Point2D camPos = new Point2D(windowWidth, windowHeight).multiply(-0.5).multiply(1/cameraZoom).add(targetDimensions);
        camera.addComponent(new CameraFollowComponent(nge, camPos));
	 */
	
}
