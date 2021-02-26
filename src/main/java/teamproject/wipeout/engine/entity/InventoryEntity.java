package teamproject.wipeout.engine.entity;

import javafx.scene.image.Image;

import java.io.IOException;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

public class InventoryEntity extends GameEntity {
	
	public Transform transform;
	public Point2D size;
	
	public static int SIZE = 10;
	
	SpriteManager spriteManager;
	
	public InventoryEntity(GameScene scene, SpriteManager spriteManager) {
		super(scene);
		this.transform = new Transform(65, 500);
		this.spriteManager = spriteManager;
		this.size = new Point2D(150, 150);
		this.addComponent(this.transform);
	}
	
	public void addItem(Integer itemID, double index) {
		//TODO
		
	}
	
	public Image getSquare() throws IOException {
		this.spriteManager.loadSpriteSheet("tile-descriptor.json", "tile.png");
		return this.spriteManager.getSpriteSet("tile", "tile1")[0];
		
	}
	
}