package teamproject.wipeout.game.player;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.FileNotFoundException;
import java.util.ArrayList;


import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import teamproject.wipeout.engine.component.PickableComponent;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.physics.HitboxComponent;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.SpriteRenderable;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputKeyAction;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.item.components.PlantComponent;

public class InventoryUI extends StackPane {

	public Point2D size;
	Group root;
	
	public static int MAX_SIZE = 10;
	public ItemStore itemStore;
	
	SpriteManager spriteManager;
	
	//Temporarily placed
	private Rectangle[] rectangles = new Rectangle[MAX_SIZE];
	private ImageView[] spriteViews = new ImageView[MAX_SIZE];
	private Text[] quantityTexts = new Text[MAX_SIZE];
	private int currentSelection = 0;

	public InventoryUI(SpriteManager spriteManager, ItemStore itemStore) {
		super();
		this.root = new Group(); //sets the root node of the inventory UI scene graph
		this.getChildren().add(this.root);
		
		StackPane.setAlignment(root, Pos.BOTTOM_CENTER);

		this.itemStore = itemStore;
		this.spriteManager = spriteManager;
		createSquares();
		createTexts();
	}
	
	/**
	 * Used to update the UI when a change is made (called by Player class).
	 * @param items updated inventory arraylist
	 * @param index slot where change happened so only one slot needs to be updated.
	 */
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
				spriteViews[index].setX(67*index + (32 - frame.getWidth()/2));
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
	
	/**
	 * highlights selected slot (changes border colour)
	 * @param slot index of slot to be selected
	 */
	public void selectSlot(int slot) {
		rectangles[this.currentSelection].setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
		this.currentSelection = slot;
		rectangles[slot].setStyle("-fx-stroke: red; -fx-stroke-width: 3;");
	}

	/**
	 * Sets up the inventory mouse input.
	 *
	 * @param world {@link WorldEntity} of the {@code GameScene}
	 * @return {@link EventHandler} handling the mouse click events.
	 */
	public void onMouseClick(WorldEntity world) {
		for(int i = 0; i < MAX_SIZE; i++) {
			int hold = i;
			rectangles[i].setOnMouseClicked((event) -> {
				FarmEntity myFarm = world.getMyFarm();
				Player myPlayer = world.getMyPlayer();

				if (myFarm.isPlacingItem()) {
					myFarm.stopPlacingItem(false);
					if (hold == myPlayer.selectedSlot) {
						return;
					}
				}

				int selectedItemID = myPlayer.selectSlot(hold);
				if (selectedItemID < 0) {
					return;
				}

				try {
					Item selectedItem = itemStore.getItem(selectedItemID);
					if (!selectedItem.hasComponent(PlantComponent.class)) {
						return;
					}
					myPlayer.dropItem();
					myFarm.startPlacingItem(selectedItem, new Point2D(event.getSceneX(), event.getSceneY()), (item) -> {
						myPlayer.acquireItem(item.id);
					});

				} catch (FileNotFoundException exception) {
					exception.printStackTrace();
				}
			});
		}
	}

	/**
	 * Sets up the inventory key input.
	 *
	 * @param gameScene {@link GameScene} of the {@code InventoryUI}
	 * @param player {@link Player} who owns the inventory
	 * @return {@link InputKeyAction} executed on a specified key event.
	 */
	public InputKeyAction dropOnKeyRelease(GameScene gameScene, Player player) {
		return () -> {
			int id = player.dropItem();
			System.out.println("***itemID: " + id);
			if(id != -1) {
				GameEntity e = gameScene.createEntity();
				Transform tr = player.getComponent(Transform.class);
				e.addComponent(new Transform (tr.getPosition().getX(), tr.getPosition().getY()));
				e.addComponent(new HitboxComponent(new teamproject.wipeout.engine.component.physics.Rectangle(0, -20, 20, 20)));
				Item eItem = itemStore.getItem(id);
				e.addComponent(new PickableComponent(eItem));
				InventoryComponent invComponent = eItem.getComponent(InventoryComponent.class);

				try {
					Image[] images = spriteManager.getSpriteSet(invComponent.spriteSheetName, invComponent.spriteSetName);
					e.addComponent(new RenderComponent(new SpriteRenderable(images[0])));

				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};
	}
	
	/**
	 * creates squares for inventory slots, and adds ImageViews ready for sprite frames
	 */
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
			spriteView.setMouseTransparent(true);
			root.getChildren().add(spriteView);
			spriteViews[i] = spriteView;
		}
	}
	
	/**
	 * Creates text nodes ready to display quantities in the inventory
	 */
	private void createTexts() {
		for(int i = 0; i < MAX_SIZE; i++) {
			Text text = new Text("");
			text.setX(i*67 + 5);
			text.setY(13);
			text.setFill(Color.MAROON);
			text.setMouseTransparent(true);
			root.getChildren().add(text);
			quantityTexts[i] = text;
		}
	}
	
	/*
	public Image getSquare() throws IOException {
		this.spriteManager.loadSpriteSheet("tile-descriptor.json", "tile.png");
		return this.spriteManager.getSpriteSet("tile", "tile1")[0];
		
	}
	*/
	
}