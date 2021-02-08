package teamproject.wipeout.engine.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.render.RectRenderComponent;
import teamproject.wipeout.engine.component.physics.CollisionComponent;
import teamproject.wipeout.engine.component.physics.FacingDirection;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class CollisionSystem implements GameSystem {
    
    protected SignatureEntityCollector _entityCollector;

    public CollisionSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(Transform.class, CollisionComponent.class));
    }

    public void accept(Double timeStep) {
        List<GameEntity> entities = this._entityCollector.getEntities();

        for(int i=0; i < entities.size(); i++) {
            for(int j=0; j < entities.size(); j++) {
            	if(i!=j) {
                	if(CollisionComponent.collides(entities.get(i), entities.get(j))) {
                		
                		//TODO make sure components behave correctly now we have detected the collision
                		//This is a quick hacky thing just to see if it works
                		//Only works because both components have a gravity and velocity component 
                		//at the moment a collision causes both objects to freeze
                		MovementComponent m1 = entities.get(i).getComponent(MovementComponent.class);
                        m1.velocity= new Point2D(0,0);
                        m1.acceleration = new Point2D(0,0);
                        
                        MovementComponent m2 = entities.get(j).getComponent(MovementComponent.class);
                        m2.velocity= new Point2D(0,0);
                        m2.acceleration = new Point2D(0,0);

                        
                        
                	}
            	}
            }
        }
        
    }
    


}
