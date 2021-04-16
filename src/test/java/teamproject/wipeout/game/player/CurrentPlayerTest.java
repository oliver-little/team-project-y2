package teamproject.wipeout.game.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.inventory.*;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;

public class CurrentPlayerTest {
	private static CurrentPlayer currentPlayer;
	private static GameScene scene = new GameScene();
	private static ItemStore itemStore;
	private static int MAX_SIZE; //no. of inventory slots
	private static SpriteManager spriteManager;
	
	@BeforeAll
	static void initialization() throws ReflectiveOperationException, IOException {
		itemStore = new ItemStore("items.json");
		spriteManager = new SpriteManager();
		spriteManager.loadSpriteSheet("player/player-red-descriptor.json", "player/player-red.png");
		spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
        spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        spriteManager.loadSpriteSheet("inventory/inventory-tools-descriptor.json", "inventory/Tools.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
        spriteManager.loadSpriteSheet("inventory/inventory-vegetables-descriptor.json", "inventory/Vegetables.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");

		int playerID = new Random().nextInt(1024);
		InventoryUI invUI = new InventoryUI(spriteManager, itemStore);
		Pair<Integer, String> playerInfo = new Pair<Integer, String>(playerID, "testPlayer");
		currentPlayer = new CurrentPlayer(scene, playerInfo, spriteManager, itemStore);
		currentPlayer.setInventoryUI(invUI);
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
		Assertions.assertEquals(-1, currentPlayer.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1));
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
		Assertions.assertEquals(-1, currentPlayer.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1));
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
		Assertions.assertEquals(-1, currentPlayer.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1));
	}
	
}