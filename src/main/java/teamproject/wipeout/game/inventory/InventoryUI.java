package teamproject.wipeout.game.inventory;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.input.InputKeyAction;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.entity.WorldEntity;
import teamproject.wipeout.game.farm.Pickables;
import teamproject.wipeout.game.farm.entity.FarmEntity;
import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.item.components.SabotageComponent;
import teamproject.wipeout.game.item.components.SabotageComponent.SabotageType;
import teamproject.wipeout.game.market.ui.ErrorUI;
import teamproject.wipeout.game.market.ui.ErrorUI.ERROR_TYPE;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.potion.PotionThrowEntity;
import teamproject.wipeout.util.ImageUtil;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

/**
 * Creates the player's inventory bar as a StackPane
 */
public class InventoryUI extends StackPane {

	public static final int IMAGE_SIZE = 48;
	public static final int MAX_SIZE = 10;

	public enum InventoryState {
		NONE,
		PLANTING,
		THROWING
	}

	public Point2D size;
	public Group root;

	private int currentSelection = 0;

	private InventoryState state = InventoryState.NONE;

	private PotionThrowEntity currentPotion;

	private final ItemStore itemStore;
	private final SpriteManager spriteManager;
	
	private final Rectangle[] rectangles = new Rectangle[MAX_SIZE];
	private final ImageView[] spriteViews = new ImageView[MAX_SIZE];
	private final Text[] quantityTexts = new Text[MAX_SIZE];

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
	 * Used to update the UI when a change is made at a specific index
	 * @param items updated inventory arraylist
	 * @param index slot where change happened so only one slot needs to be updated.
	 */
	public void updateUI(ArrayList<InventoryItem> items, Integer index) {
		if (items.get(index) != null) {
			Item item = itemStore.getItem(items.get(index).itemID);
			InventoryComponent inv = item.getComponent(InventoryComponent.class);
			quantityTexts[index].setText("" + items.get(index).quantity);
			Image sprite = getScaledImage(inv.spriteSheetName, inv.spriteSetName);
			spriteViews[index].setImage(sprite);

			spriteViews[index].setX(67*index + (32 - Math.min(IMAGE_SIZE, sprite.getWidth())/2));
			spriteViews[index].setY(32 - Math.min(IMAGE_SIZE, sprite.getHeight()) / 2);

		} else {
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
			final int count = i;
			rectangles[i].addEventFilter(MouseEvent.MOUSE_CLICKED, (event) -> {
				event.consume();
				this.useSlot(count, world).performKeyAction();
			});
		}
	}
	
	/**
	 * Selects a slot and then starts/stops placing item
	 * @param slot Index of the slot selected
	 * @param world
	 */
	public InputKeyAction useSlot(int slot, WorldEntity world) {
		return () -> {
			this.selectSlot(slot);

			CurrentPlayer myCurrentPlayer = world.myCurrentPlayer;
			FarmEntity myFarm = world.myCurrentPlayer.getMyFarm();

			if (state == InventoryState.PLANTING) {
				if (myFarm.isPlacingItem()) {
					myFarm.stopPlacingItem(false);
				}
			} else if (state == InventoryState.THROWING) {
				currentPotion.abortThrowing();
			}

			int selectedItemID = myCurrentPlayer.selectSlot(currentSelection);
			if (selectedItemID < 0) {
				return;
			}

			try {
				Item selectedItem = itemStore.getItem(selectedItemID);
				if (selectedItem.hasComponent(PlantComponent.class)) {
					myCurrentPlayer.dropItem();
					state = InventoryState.PLANTING;
					myFarm.startPlacingItem(selectedItem, new Point2D(0, 0), (item) -> {
						myCurrentPlayer.acquireItem(item.id);
						state = InventoryState.NONE;
					});
				} else if (selectedItem.hasComponent(SabotageComponent.class)) {
					SabotageComponent sc = selectedItem.getComponent(SabotageComponent.class);

					List<GameEntity> possibleEffectEntities = null;

					if (sc.type == SabotageType.SPEED) {
						possibleEffectEntities = List.of(world.myCurrentPlayer, world.myAnimal);
					} else if (sc.type == SabotageType.GROWTHRATE || sc.type == SabotageType.AI) {
						possibleEffectEntities = List.copyOf(world.farms.values());
					}

					Runnable onComplete = () -> {
						state = InventoryState.NONE;
						currentPotion = null;
					};
					Runnable onAbort = () -> {
						state = InventoryState.NONE;
						currentPotion = null;
						myCurrentPlayer.acquireItem(selectedItem.id);
					};

					state = InventoryState.THROWING;
					myCurrentPlayer.dropItem();
					this.currentPotion = new PotionThrowEntity(world.getScene(), spriteManager, myCurrentPlayer, myCurrentPlayer, selectedItem, possibleEffectEntities, onComplete, onAbort);
				}

			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
			}
		};
	}

	/**
	 * Sets up the inventory key input.
	 *
	 * @param currentPlayer {@link CurrentPlayer} who owns the inventory
	 * @param pickables {@link Pickables} class in the {@link WorldEntity}
	 * @return {@link InputKeyAction} executed on a specified key event.
	 */
	public InputKeyAction dropOnKeyRelease(CurrentPlayer currentPlayer, Pickables pickables) {
		return () -> {
			int id = currentPlayer.dropItem();
			System.out.println("***itemID: " + id);
			if (id != -1) {
				Transform transform = currentPlayer.getComponent(Transform.class);
				RenderComponent renderComponent = currentPlayer.getComponent(RenderComponent.class);
				double centreX = transform.getPosition().getX() + (renderComponent.getWidth() / 2);
				double centreY = transform.getPosition().getY() + (renderComponent.getHeight() / 2);
				pickables.createPickablesFor(this.itemStore.getItem(id), centreX, centreY, 1);
				currentPlayer.playSound("thud.wav");
			}
		};
	}

	/**
	 * Displays an onscreen message.
	 * @param errorType The type of error/message to display.
	 */
	public void displayMessage(ERROR_TYPE errorType) {
		new ErrorUI(this, errorType);
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
			spriteView.setFitWidth(IMAGE_SIZE);
			spriteView.setFitHeight(IMAGE_SIZE);
			spriteView.setPreserveRatio(true);
			spriteView.setMouseTransparent(true);
			spriteView.setStyle("-fx-stroke: cyan; -fx-stroke-width: 1;");
			root.getChildren().add(spriteView);
			spriteViews[i] = spriteView;
		}
	}
	
	/**
	 * Creates text nodes ready to display quantities in the inventory
	 */
	private void createTexts() {
		InputStream path;
		try {
			path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
			Font font = Font.loadFont(path, 13);
			for(int i = 0; i < MAX_SIZE; i++) {
				Text text = new Text("");
				text.setX(i*67 + 5);
				text.setY(15);
				text.setFill(Color.MAROON);
				text.setFont(font);
				text.setMouseTransparent(true);
				root.getChildren().add(text);
				quantityTexts[i] = text;
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Gets an image from the spriteManager, scaled to fit the inventory image sizes
	 * 
	 * @param spriteSheetName The spriteSheet to retrieve an image from
	 * @param spriteSetName The spriteSet to retrieve an image from
	 * @return The requested image, scaled to IMAGE_SIZE
	 */
	private Image getScaledImage(String spriteSheetName, String spriteSetName) {
		Image sprite = null;
		try {
			sprite = spriteManager.getSpriteSet(spriteSheetName, spriteSetName)[0];
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (sprite.getWidth() > sprite.getHeight() && sprite.getWidth() < IMAGE_SIZE) {
			sprite = ImageUtil.scaleImage(sprite, ((double) IMAGE_SIZE)/sprite.getWidth());
		}
		else if (sprite.getHeight() < IMAGE_SIZE) {
			sprite = ImageUtil.scaleImage(sprite, ((double) IMAGE_SIZE)/sprite.getHeight());
		}

		return sprite;
	}
}