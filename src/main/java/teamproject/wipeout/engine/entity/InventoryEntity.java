package teamproject.wipeout.engine.entity;

import javafx.scene.image.Image;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ItemComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;

public class InventoryEntity extends GameEntity {
	
	public Transform transform;
	public Point2D size;
	public GameScene gameScene;
	
	public static int SIZE = 10;
	private Point2D topLeft;
	
	SpriteManager spriteManager;
	
	public InventoryEntity(GameScene scene, SpriteManager spriteManager) {
		super(scene);
		this.transform = new Transform(65, 500);
		this.spriteManager = spriteManager;
		this.size = new Point2D(65, 650);
		this.addComponent(this.transform);
		this.gameScene = scene;
		this.topLeft = new Point2D(65, 500); //coord of top left of inventory bar
	}
	
	public void showItems(LinkedHashMap<Integer, Integer> items, ItemStore itemStore) {
		for (Integer itemID : items.keySet()) {
			System.out.println("Item ID : " + itemID);
			GameEntity entity = gameScene.createEntity();
	        entity.addComponent(new Transform (topLeft.getX() + ((items.size()-1)*65), topLeft.getY())); //positions plant
	        Item potatoItem2 = itemStore.getItem(itemID);
	        entity.addComponent(new ItemComponent(potatoItem2)); //gives it item component
	        try {
	            spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
	            Image[] frames = spriteManager.getSpriteSet("crops", "potato");
	            entity.addComponent(new RenderComponent(new SpriteRenderable(frames[2]))); //adds sprite image component
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
	}
	
	public Image getSquare() throws IOException {
		this.spriteManager.loadSpriteSheet("tile-descriptor.json", "tile.png");
		return this.spriteManager.getSpriteSet("tile", "tile1")[0];
		
	}
	
}