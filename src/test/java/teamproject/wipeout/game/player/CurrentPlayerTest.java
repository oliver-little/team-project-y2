package teamproject.wipeout.game.player;

import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.inventory.InventoryItem;
import teamproject.wipeout.game.inventory.InventoryUI;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;
import teamproject.wipeout.util.resources.PlayerSpriteSheetManager;
import teamproject.wipeout.util.resources.ResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * This tests the player’s inventory, specifically whether items can be added and removed correctly.
 * For example, it tests the inventory blocks items being added if it is full; 
 * blocks items being removed that are not already in the inventory; 
 * correctly overflows a slot into another when the quantity of an item exceeds its stack limit, and correctly rearranges items when fewer slots can be used by items of the same ID to free up as many inventory slots as possible.
 */
public class CurrentPlayerTest {
	private static CurrentPlayer currentPlayer;
	private static GameScene scene = new GameScene();
	private static ItemStore itemStore;
	private static int MAX_SIZE; //no. of inventory slots

	@BeforeAll
	static void initialization() throws ReflectiveOperationException, IOException {
		ResourceLoader.setTargetClass(ResourceLoader.class);
		itemStore = new ItemStore("items.json");
		SpriteManager spriteManager = new SpriteManager();
		spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
        spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        spriteManager.loadSpriteSheet("inventory/inventory-tools-descriptor.json", "inventory/Tools.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
        spriteManager.loadSpriteSheet("inventory/inventory-vegetables-descriptor.json", "inventory/Vegetables.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        spriteManager.loadSpriteSheet("inventory/inventory-animals-and-food-descriptor.json", "inventory/AnimalsAndFood.png");
        spriteManager.loadSpriteSheet("inventory/inventory-potions-descriptor.json", "inventory/Potions.png");
        spriteManager.loadSpriteSheet("ai/mouse-descriptor.json", "ai/mouse.png");
        spriteManager.loadSpriteSheet("ai/rat-descriptor.json", "ai/rat.png");
        spriteManager.loadSpriteSheet("gameworld/arrow-descriptor.json", "gameworld/Arrow.png");
		PlayerSpriteSheetManager.loadPlayerSpriteSheets(spriteManager);
		int playerID = new Random().nextInt(1024);
		InventoryUI invUI = new InventoryUI(spriteManager, itemStore);
		Pair<Integer, String> playerInfo = new Pair<Integer, String>(playerID, "testPlayer");
		currentPlayer = new CurrentPlayer(scene, playerInfo, null, spriteManager, itemStore);
		currentPlayer.setInventoryUI(invUI);
		currentPlayer.debug = true; //disables the error messages, which require JavaFX toolkit to be initialised
		MAX_SIZE = currentPlayer.MAX_SIZE;
	}
	
	@BeforeEach
	void clearInventory() {
		currentPlayer.clearInventory();
	}
	
	
	@Test
	void testFullSlotsCapacity() {
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(currentPlayer.acquireItem(i, 1));
		}
		for(int i = (MAX_SIZE + 1); i <= (2*MAX_SIZE); i++) {
			Assertions.assertFalse(currentPlayer.acquireItem(i, 1));
		}
		Assertions.assertEquals(MAX_SIZE, currentPlayer.countOccupiedSlots());
		
		ArrayList<InventoryItem> inventory = currentPlayer.getInventory();
		for(InventoryItem pair : inventory) {
			Assertions.assertNotNull(pair); //checks slots are all not null
		}
	}
	
	@Test
	void testFullStackCapacities() {
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(currentPlayer.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
		}
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertFalse(currentPlayer.acquireItem(i, 1));
		}
	}
	
	@Test
	void testSlotsOverflow() {
		for(int i = 1; i <= MAX_SIZE/2; i++) {
			Assertions.assertTrue(currentPlayer.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
			Assertions.assertTrue(currentPlayer.acquireItem(i, 1));
		}
		Assertions.assertFalse(currentPlayer.acquireItem(MAX_SIZE/2 + 1, 1));
	}
	
	@Test
	void testOccupiedSlotsCounter() {
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(currentPlayer.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
			Assertions.assertEquals(i, currentPlayer.countOccupiedSlots());
		}
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertEquals(i, currentPlayer.removeItem(i, 1)[0]);
			Assertions.assertEquals(MAX_SIZE, currentPlayer.countOccupiedSlots());
		}
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertEquals(i, currentPlayer.removeItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit - 1)[0]);
			Assertions.assertEquals(MAX_SIZE - i, currentPlayer.countOccupiedSlots());
		}
	}
	
	@Test
	void testRemovingItemsFromSelectedSlot() {
		//fill inventory
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(currentPlayer.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
		}
		//remove individual items
		for(int i = 0; i < MAX_SIZE; i++) {
			currentPlayer.selectSlot(i);
			Assertions.assertEquals(i+1, currentPlayer.dropItem()); //checks itemID of individual item dropped returned
		}
		//remove rest of items
		for(int i = 0; i < MAX_SIZE; i++) {
			currentPlayer.selectSlot(i);
			Assertions.assertTrue(currentPlayer.removeItemFromSelectedSlot(itemStore.getItem(i+1).getComponent(InventoryComponent.class).stackSizeLimit - 1)); //checks multiple items dropped
		}
		
		//check no more items can be removed
		for(int i = 0; i < MAX_SIZE; i++) {
			currentPlayer.selectSlot(i);
			Assertions.assertFalse(currentPlayer.removeItemFromSelectedSlot(1));
		}
		
		Assertions.assertEquals(0, currentPlayer.countOccupiedSlots()); //checks slots are all unoccupied
		
		ArrayList<InventoryItem> inventory = currentPlayer.getInventory();
		for(InventoryItem pair : inventory) {
			Assertions.assertNull(pair); //checks slots are all null
		}
	}

	//items are rearranged only when items are sold to ensure items are arranged efficiently.
	//when items are sold, the removeItem method is called with the item's ID and quantity to be removed
	@Test
	void testRemovingItems1() {
		//tests when slot to be merged from is after other slots with the same itemID
		Assertions.assertTrue(currentPlayer.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(currentPlayer.acquireItem(1, 2));
		
		Assertions.assertTrue(currentPlayer.acquireItem(2, itemStore.getItem(2).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(currentPlayer.acquireItem(3, itemStore.getItem(3).getComponent(InventoryComponent.class).stackSizeLimit*3));
		
		Assertions.assertEquals(1, currentPlayer.removeItem(1, 2)[0]); //will remove from first slot -> next check this moves items from 4th slot into the first
		Assertions.assertEquals(3, currentPlayer.countSlotsOccupiedBy(1));
		ArrayList<InventoryItem> inventory = currentPlayer.getInventory();
		Assertions.assertNull(inventory.get(3)); //ensures slot merged from is emptied
		Assertions.assertEquals(0, currentPlayer.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1).length); //ensure empty list returned
	}
	@Test
	void testRemovingItems2() {
		//tests when slot to be merged from is in the middle of other slots with the same itemID
		Assertions.assertTrue(currentPlayer.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit));
		Assertions.assertTrue(currentPlayer.acquireItem(2, 2));
		Assertions.assertTrue(currentPlayer.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit * 2));
		Assertions.assertTrue(currentPlayer.acquireItem(3, itemStore.getItem(2).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(currentPlayer.acquireItem(4, itemStore.getItem(3).getComponent(InventoryComponent.class).stackSizeLimit*3));
		
		Assertions.assertEquals(2, currentPlayer.removeItem(2, 2)[0]);
		Assertions.assertTrue(currentPlayer.acquireItem(1, 2));
		Assertions.assertEquals(1, currentPlayer.removeItem(1, 2)[0]);
		
		Assertions.assertEquals(3, currentPlayer.countSlotsOccupiedBy(1));
		ArrayList<InventoryItem> inventory = currentPlayer.getInventory();
		Assertions.assertNull(inventory.get(1)); //ensures slot merged from is emptied
		Assertions.assertEquals(0, currentPlayer.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1).length);
	}
	@Test
	void testRemovingItems3() {
		//tests when slot to be merged from is before other slots with the same itemID
		Assertions.assertTrue(currentPlayer.acquireItem(2, 2));
		Assertions.assertTrue(currentPlayer.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(currentPlayer.acquireItem(3, itemStore.getItem(2).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(currentPlayer.acquireItem(4, itemStore.getItem(3).getComponent(InventoryComponent.class).stackSizeLimit*3));
		
		Assertions.assertEquals(2, currentPlayer.removeItem(2, 2)[0]);
		Assertions.assertTrue(currentPlayer.acquireItem(1, 2));
		Assertions.assertEquals(1, currentPlayer.removeItem(1, 2)[0]);
		
		Assertions.assertEquals(3, currentPlayer.countSlotsOccupiedBy(1));
		ArrayList<InventoryItem> inventory = currentPlayer.getInventory();
		Assertions.assertNull(inventory.get(0)); //ensures slot merged from is emptied
		Assertions.assertEquals(0, currentPlayer.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1).length);
	}
	
}