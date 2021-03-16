package teamproject.wipeout.engine.component.physics;

import org.junit.jupiter.api.Test;
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
        g1.addComponent(new Transform(0,0));
        g1.addComponent(new HitboxComponent(new Rectangle(0,0,10,10)));

        GameEntity g2 = new GameEntity(gameScene);
        g2.addComponent(new Transform(0,0));
        g2.addComponent(new HitboxComponent(new Rectangle(0,0,10,10)));

        assertTrue(HitboxComponent.checkCollides(g1,g2));

    }
}
