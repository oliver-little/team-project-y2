package teamproject.wipeout.engine.entity;

import javafx.scene.image.Image;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javafx.geometry.Point2D;
//import teamproject.wipeout.engine.component.ItemComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;

public class InventoryEntity extends GameEntity {
	
	public Transform transform;
	public Point2D size;
	public GameScene gameScene;
	
	public static int SIZE = 10;
	private Point2D topLeft;
	
	SpriteManager spriteManager;
	
	//Temporarily placed
	private GameEntity textEntity;
	private TextRenderable textRenderable;
	private TextRenderable[] textRenderables = new TextRenderable[SIZE];

	public InventoryEntity(GameScene scene, SpriteManager spriteManager) {
		super(scene);
		this.transform = new Transform(65, 500);
		this.spriteManager = spriteManager;
		this.size = new Point2D(65, 500);
		this.addComponent(this.transform);
		this.gameScene = scene;
		this.topLeft = new Point2D(65, 500); //coord of top left of inventory bar
		createTextRenderables();
		
	}
	
	public void showItems(LinkedHashMap<Integer, Integer> items, ItemStore itemStore) {
		int i = 0;
		for (Integer itemID : items.keySet()) {
			System.out.println("Item ID : " + itemID);
			GameEntity entity = gameScene.createEntity();
	        //entity.addComponent(new Transform (topLeft.getX() + 15 + (i*65), topLeft.getY() + 20, 1)); //positions plant
	        Item item = itemStore.getItem(itemID);
	        InventoryComponent inv = item.getComponent(InventoryComponent.class);
	        textRenderables[i].setText(items.get(itemID).toString());
	        try {
	            Image[] frames = spriteManager.getSpriteSet(inv.spriteSheetName, inv.spriteSetName);
	            RenderComponent rc = new RenderComponent(new SpriteRenderable(frames[0]));
	            entity.addComponent(rc); //adds sprite image component
	            double xpos = topLeft.getX() + (65*i) + ((65-rc.getWidth())/2); //x-offset from top left of tile
	            double ypos = topLeft.getY() + ((65-rc.getHeight())/2); //y-offset from top left of tile
	            entity.addComponent(new Transform (xpos, ypos, 1)); 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        i++;
		}
	}
	
	private void createTextRenderables() {
		for(int i = 0; i < SIZE; i++) {
			textEntity = gameScene.createEntity();
	        textEntity.addComponent(new Transform (topLeft.getX() + 8 + (65*i), topLeft.getY() + 13, 1));
	        textRenderable = new TextRenderable("");
	        textEntity.addComponent(new RenderComponent(textRenderable));
	        textRenderables[i] = textRenderable;
		}
	}
	
	public Image getSquare() throws IOException {
		this.spriteManager.loadSpriteSheet("gameworld/tile-descriptor.json", "gameworld/tile.png");
		return this.spriteManager.getSpriteSet("tile", "tile1")[0];
		
	}
	
}