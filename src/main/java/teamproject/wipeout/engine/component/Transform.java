package teamproject.wipeout.engine.component;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.entity.GameEntity;

/**
 * Represents a position and rotation in physical space
 */
public class Transform implements EntityAwareGameComponent {

    // The entity this Transform is attached to
    private GameEntity entity;
    // The entity's parent last time the world position was calculated
    private GameEntity lastParent;

    private Point2D position;
    private Point2D worldPosition;
    private double rotation;
    private boolean worldPositionDirty;

    /**
     * Creates a new instance of Transform with default parameters
     * 
     * @param entity The entity this transform is part of
     */
    public Transform() {
        this.position = Point2D.ZERO;
        this.worldPositionDirty = true;
        this.rotation = 0;
    }

    public Transform(double x, double y) {
        this.position = new Point2D(x, y);
        this.worldPositionDirty = true;
    }

    public Transform(Point2D position, double rotation) {
        this.position = position;
        this.worldPositionDirty = true;
        this.rotation = rotation;
    }

    public Transform(double x, double y, double rotation) {
        this.position = new Point2D(x, y);
        this.worldPositionDirty = true;
        this.rotation = rotation;
    }

    public String getType() {
        return "transform";
    }

    public GameEntity getEntity() {
        return this.entity;
    }

    /**
     * Changes the entity this Transform is attached to
     * 
     * @param newEntity The new GameEntity
     */
    public void setEntity(GameEntity newEntity) {
        this.entity = newEntity;
        this.lastParent = this.entity.getParent();
        if (this.entity.getParent() != null) {
            this.worldPositionDirty = true;
        }
    }

    /**
     * Marks the world position of this component as dirty, meaning it will be recalculated next time it is requested.
     */
    public void setWorldPosDirty() {
        this.worldPositionDirty = true;

        this.invalidateChildPositions();
    }

    /**
     * Gets the local position of this transform (relative to the parent)
     * 
     * @return The local position of this transform
     */
    public Point2D getPosition() {
        return this.position;
    }

    /**
     * Sets the local position of this transform
     * 
     * @param newPosition The new local position
     */
    public void setPosition(Point2D newPosition) {
        this.position = newPosition;
        
        this.invalidateChildPositions();
    }

    /**
     * Gets the world position of this transform
     * 
     * @return The world position
     */
    public Point2D getWorldPosition() {
        if (this.worldPositionDirty || this.lastParent != this.entity.getParent()) {
            GameEntity currentEntity = this.entity.getParent();
            while (currentEntity != null && !currentEntity.hasComponent(Transform.class)) {
                currentEntity = currentEntity.getParent();
            }
            if (currentEntity == null) {
                this.worldPosition = this.position;
            }
            else {
                this.worldPosition = this.position.add(currentEntity.getComponent(Transform.class).getWorldPosition());
            }
        }

        this.lastParent = this.entity.getParent();

        return this.worldPosition;
    }

    /**
     * Gets the rotation of this transform
     * 
     * @return The rotation in degrees
     */
    public double getRotation() {
        return this.rotation;
    }

    /**
     * Sets the rotation of this transform
     * 
     * @param newRotation The new rotation in degrees
     */
    public void setRotation(double newRotation) {
        this.rotation = newRotation;
    }

    /**
     * Invalidates the world position of all children of this transform's entity
     * (this recursively invalidates all their children, etc)
     */
    private void invalidateChildPositions() {
        // Recursively go down the hierarchy and invalidate world positions
        for (GameEntity child : this.entity.getChildren()) {
            if (child.hasComponent(Transform.class)) {
                child.getComponent(Transform.class).setWorldPosDirty();
            }
        }
    }
}

