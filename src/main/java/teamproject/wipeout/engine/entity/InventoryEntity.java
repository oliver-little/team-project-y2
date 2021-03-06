package teamproject.wipeout.engine.entity;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
//import teamproject.wipeout.engine.component.physics.Rectangle;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.player.invPair;

public class InventoryEntity{

	public Point2D size;
	Group root;
	
	public static int MAX_SIZE = 10;
	private ItemStore itemStore;
	
	SpriteManager spriteManager;
	
	//Temporarily placed
	private Rectangle[] rectangles = new Rectangle[MAX_SIZE];
	private ImageView[] spriteViews = new ImageView[MAX_SIZE];
	private Text[] quantityTexts = new Text[MAX_SIZE];
	private int currentSelection = 0;

	public InventoryEntity(Group root, SpriteManager spriteManager, ItemStore itemStore) {
		this.root = root; //sets the root node of the inventory UI scene graph
		this.itemStore = itemStore;
		this.spriteManager = spriteManager;
		createSquares();
		createTexts();
		
	}
	
	public void updateUI(ArrayList<invPair> items, Integer index) {
		if(items.get(index) != null) {
			Item item = itemStore.getItem(items.get(index).itemID);
			InventoryComponent inv = item.getComponent(InventoryComponent.class);
			Image frame;
			quantityTexts[index].setText("" + items.get(index).quantity);
			try
			{
				frame = spriteManager.getSpriteSet(inv.spriteSheetName, inv.spriteSetName)[0];
				spriteViews[index].setImage(frame);
				spriteViews[index].setX(64*index + (32 - frame.getWidth()/2));
				spriteViews[index].setY(32 - frame.getHeight()/2);
				
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			quantityTexts[index].setText("");
			spriteViews[index].setImage(null);
		}
		
	}
	public Rectangle[] getRectangles() {
		return rectangles;
	}
	
	public void selectSlot(int slot) {
		rectangles[this.currentSelection].setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
		this.currentSelection = slot;
		rectangles[slot].setStyle("-fx-stroke: red; -fx-stroke-width: 3;");
	}
	
	private void createSquares() {
		for(int i = 0; i < MAX_SIZE ; i++) {
			
			Rectangle r = new Rectangle();
			r.setWidth(62);
			r.setHeight(62);
			r.setFill(Color.LIGHTGREY);
			r.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
			r.setX((67*i));
			root.getChildren().add(r);
			rectangles[i] = r;
			
			ImageView spriteView = new ImageView();
			root.getChildren().add(spriteView);
			spriteViews[i] = spriteView;
		}
	}
	
	private void createTexts() {
		for(int i = 0; i < MAX_SIZE; i++) {
			Text text = new Text("");
			text.setX(i*67 + 5);
			text.setY(13);
			text.setFill(Color.MAROON);
			root.getChildren().add(text);
			quantityTexts[i] = text;
		}
	}
	
	public Image getSquare() throws IOException {
		this.spriteManager.loadSpriteSheet("tile-descriptor.json", "tile.png");
		return this.spriteManager.getSpriteSet("tile", "tile1")[0];
		
	}
	
}