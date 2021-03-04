package teamproject.wipeout.game.entity;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;

public class FarmEntity extends GameEntity
{


	public FarmEntity(GameScene scene, double x, double y) {
        super(scene);
        this.addComponent(new Transform(x, y));
        this.addComponent(new RenderComponent(new RectRenderable(Color.rgb(115, 64, 54), 200, 100)));
    }
	
	public FarmEntity(GameScene scene, Point2D p) {
        super(scene);
        this.addComponent(new Transform(p.getX(), p.getY()));
        this.addComponent(new RenderComponent(new RectRenderable(Color.rgb(115, 64, 54), 200, 100)));
    }
	
}
