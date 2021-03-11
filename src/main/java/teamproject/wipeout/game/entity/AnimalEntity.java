package teamproject.wipeout.game.entity;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.PlayerAnimatorComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.ai.NavigationMesh;
import teamproject.wipeout.engine.component.ai.NavigationSquare;
import teamproject.wipeout.engine.component.ai.SteeringComponent;
import teamproject.wipeout.engine.component.physics.CollisionResolutionComponent;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.system.ai.PathFindingSystem;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

public class AnimalEntity extends GameEntity {

    private NavigationMesh navMesh;

    private Transform transformComponent;

    public AnimalEntity(GameScene scene, Point2D position, NavigationMesh navMesh, SpriteManager spriteManager) {
        super(scene);

        this.navMesh = navMesh;

        transformComponent = new Transform(position.getX(), position.getY(), 1);

        this.addComponent(transformComponent);
        this.addComponent(new MovementComponent());
        this.addComponent(new RenderComponent());
        this.addComponent(new HitboxComponent(new Rectangle(0, 0, 32, 32)));

        try {
            this.addComponent(new PlayerAnimatorComponent(
                spriteManager.getSpriteSet("mouse", "up"),
                spriteManager.getSpriteSet("mouse", "right"),
                spriteManager.getSpriteSet("mouse", "down"),
                spriteManager.getSpriteSet("mouse", "left"),
                spriteManager.getSpriteSet("mouse", "idle")));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        System.out.println(navMesh.squares.toString());

        this.pickRandomPath.run();
    }

    private Runnable pickRandomPath = () -> {

        int rand = new Random().nextInt(navMesh.squares.size());

        NavigationSquare randomSquare = navMesh.squares.get(rand);
        
        int randX = new Random().nextInt(Math.abs((int) randomSquare.bottomRight.getX() - (int) randomSquare.topLeft.getX())) + (int) randomSquare.topLeft.getX();

        int randY = new Random().nextInt(Math.abs((int) randomSquare.bottomRight.getY() - (int) randomSquare.topLeft.getY())) + (int) randomSquare.topLeft.getY();
        Point2D wp = transformComponent.getWorldPosition();

        List<Point2D> path = PathFindingSystem.findPath(new Point2D((int) wp.getX(), (int) wp.getY()), new Point2D(randX, randY), navMesh);
        System.out.println(path);
        followPath(path);
    };

    private void followPath(List<Point2D> path) {
        this.addComponent(new SteeringComponent(path, pickRandomPath, 250));
    }
}
