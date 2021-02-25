package teamproject.wipeout.engine.entity;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.IOException;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.InventoryData;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

public class InventoryEntity extends GameEntity {
	
	public InventoryData data;
	public Transform transform;
	public Point2D size;
	
	public static int SIZE = 10;
	
	SpriteManager spriteManager;
	
	public InventoryEntity(GameScene scene, int xpos, int ypos) {
		super(scene);

		this.transform = new Transform(xpos, ypos);
		this.addComponent(this.transform);
		this.size = new Point2D(70, 70);
		this.addComponent(new RenderComponent(new RectRenderable(Color.LIGHTGREY, (float) size.getX(), (float) size.getY())));
	}
	
	public void addItem(Integer itemID, double index) {
		//TODO
		
	}
	
	public Image getSquare() throws IOException {
		
		return null;
		
	}
	
}