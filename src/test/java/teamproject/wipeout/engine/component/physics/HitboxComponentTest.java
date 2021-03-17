package teamproject.wipeout.engine.component.physics;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.shape.Rectangle;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;

public class HitboxComponentTest {

    @Test
    void rectangleCollisionTest(){
        GameScene gameScene = new GameScene();

        GameEntity g1 = new GameEntity(gameScene);
        Transform t1 = new Transform(0,0);
        g1.addComponent(t1);
        g1.addComponent(new HitboxComponent(new Rectangle(0,0,10,10)));

        GameEntity g2 = new GameEntity(gameScene);
        Transform t2 = new Transform(0,0);
        g2.addComponent(t2);
        g2.addComponent(new HitboxComponent(new Rectangle(0,0,10,10)));

        assertTrue(HitboxComponent.checkCollides(g1,g2));

        t1.setPosition(new Point2D(5,5));
        assertTrue(HitboxComponent.checkCollides(g1,g2));

        t1.setPosition(new Point2D(25,13));
        assertFalse(HitboxComponent.checkCollides(g1,g2));

    }

    @Test
    void rectangleMultipleHitboxTest(){
        GameScene gameScene = new GameScene();

        GameEntity g1 = new GameEntity(gameScene);
        Transform t1 = new Transform(0,0);
        g1.addComponent(t1);
        HitboxComponent h1 = new HitboxComponent(new Rectangle(0,0,10,10));
        System.out.println(h1.getHitboxes().size());
        g1.addComponent(h1);
        assertEquals(1, h1.getHitboxes().size());

        GameEntity g2 = new GameEntity(gameScene);
        Transform t2 = new Transform(0,0);
        g2.addComponent(t2);
        HitboxComponent h2 = new HitboxComponent(new Rectangle(50,50,10,10));
        g2.addComponent(h2);
        assertEquals(1, h2.getHitboxes().size());

        assertFalse(HitboxComponent.checkCollides(g1,g2));
        
        //first hitbox in each array is already part of the corresponding hitbox components
        //so should not be added twice
        //only second hitboxes should be added, and they collide
        Rectangle[] hitboxes1 = {new Rectangle(0,0,10,10), new Rectangle(10,10,5,3)};
        h1.addHitboxes(hitboxes1);
        assertEquals(2, h1.getHitboxes().size());
        Rectangle[] hitboxes2 = {new Rectangle(50,50,10,10), new Rectangle(11,11,1,2)};
        h2.addHitboxes(hitboxes2);
        
        assertTrue(HitboxComponent.checkCollides(g1,g2));
        
        h1.removeHitoxes(new Rectangle(0,0,10,10));
        assertEquals(1, h1.getHitboxes().size());
        h2.removeHitoxes(new Rectangle(11,11,1,2));
        assertEquals(1, h2.getHitboxes().size());

        assertFalse(HitboxComponent.checkCollides(g1,g2));
        
        
    }
}
