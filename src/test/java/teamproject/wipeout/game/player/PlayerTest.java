package teamproject.wipeout.game.player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.game.assetmanagement.SpriteManager;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.item.components.InventoryComponent;

public class PlayerTest {
	private static Player player;
	private static GameScene scene = new GameScene();
	private static ItemStore itemStore;
	private static int MAX_SIZE; //no. of inventory slots
	private static SpriteManager spriteManager;
	
	@BeforeAll
	static void initialization() throws ReflectiveOperationException, IOException {
		itemStore = new ItemStore("items.json");
		spriteManager = new SpriteManager();
		spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
        spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        spriteManager.loadSpriteSheet("inventory/inventory-tools-descriptor.json", "inventory/Tools.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
        spriteManager.loadSpriteSheet("inventory/inventory-vegetables-descriptor.json", "inventory/Vegetables.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
		
		InventoryUI invUI = new InventoryUI(spriteManager, itemStore);
		//player = scene.createPlayer(new Random().nextInt(1024), "testPlayer", new Point2D(0,0), invUI);
		player = scene.createPlayer(new Random().nextInt(1024), "testPlayer", new Point2D(0, 0), invUI);
		MAX_SIZE = player.MAX_SIZE;
	}
	
	@BeforeEach
	void clearInventory() {
		player.clearInventory();
	}
	
	
	@Test
	void testFullSlotsCapacity() {
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(player.acquireItem(i, 1));
		}
		for(int i = (MAX_SIZE + 1); i <= (2*MAX_SIZE); i++) {
			Assertions.assertFalse(player.acquireItem(i, 1));
		}
		Assertions.assertEquals(MAX_SIZE, player.occupiedSlots);
		
		ArrayList<invPair> inventory = player.getInventory();
		for(invPair pair : inventory) {
			Assertions.assertNotNull(pair); //checks slots are all not null
		}
	}
	
	@Test
	void testFullStackCapacities() {
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(player.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
		}
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertFalse(player.acquireItem(i, 1));
		}
	}
	
	@Test
	void testSlotsOverflow() {
		for(int i = 1; i <= MAX_SIZE/2; i++) {
			Assertions.assertTrue(player.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
			Assertions.assertTrue(player.acquireItem(i, 1));
		}
		Assertions.assertFalse(player.acquireItem(MAX_SIZE/2 + 1, 1));
	}
	
	@Test
	void testOccupiedSlotsCounter() {
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(player.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
			Assertions.assertEquals(i, player.occupiedSlots);
		}
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertEquals(i, player.removeItem(i, 1));
			Assertions.assertEquals(MAX_SIZE, player.occupiedSlots);
		}
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertEquals(i, player.removeItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit - 1));
			Assertions.assertEquals(MAX_SIZE - i, player.occupiedSlots);
		}
	}
	
	@Test
	void testRemovingItemsFromSelectedSlot() {
		//fill inventory
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(player.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
		}
		//remove individual items
		for(int i = 0; i < MAX_SIZE; i++) {
			player.selectSlot(i);
			Assertions.assertEquals(i+1, player.dropItem()); //checks itemID of individual item dropped returned
		}
		//remove rest of items
		for(int i = 0; i < MAX_SIZE; i++) {
			player.selectSlot(i);
			Assertions.assertTrue(player.removeItemFromSelectedSlot(itemStore.getItem(i+1).getComponent(InventoryComponent.class).stackSizeLimit - 1)); //checks multiple items dropped
		}
		
		//check no more items can be removed
		for(int i = 0; i < MAX_SIZE; i++) {
			player.selectSlot(i);
			Assertions.assertFalse(player.removeItemFromSelectedSlot(1));
		}
		
		Assertions.assertEquals(0, player.occupiedSlots); //checks slots are all unoccupied
		
		ArrayList<invPair> inventory = player.getInventory();
		for(invPair pair : inventory) {
			Assertions.assertNull(pair); //checks slots are all null
		}
	}

	//items are rearranged only when items are sold to ensure items are arranged efficiently.
	//when items are sold, the removeItem method is called with the item's ID and quantity to be removed
	@Test
	void testRemovingItems1() {
		//tests when slot to be merged from is after other slots with the same itemID
		Assertions.assertTrue(player.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(player.acquireItem(1, 2));
		
		Assertions.assertTrue(player.acquireItem(2, itemStore.getItem(2).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(player.acquireItem(3, itemStore.getItem(3).getComponent(InventoryComponent.class).stackSizeLimit*3));
		
		Assertions.assertEquals(1, player.removeItem(1, 2)); //will remove from first slot -> next check this moves items from 4th slot into the first
		Assertions.assertEquals(3, player.countSlotsOccupiedBy(1));
		ArrayList<invPair> inventory = player.getInventory();
		Assertions.assertNull(inventory.get(3)); //ensures slot merged from is emptied
		Assertions.assertEquals(-1, player.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1));
	}
	@Test
	void testRemovingItems2() {
		//tests when slot to be merged from is in the middle of other slots with the same itemID
		Assertions.assertTrue(player.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit));
		Assertions.assertTrue(player.acquireItem(2, 2));
		Assertions.assertTrue(player.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit * 2));
		Assertions.assertTrue(player.acquireItem(3, itemStore.getItem(2).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(player.acquireItem(4, itemStore.getItem(3).getComponent(InventoryComponent.class).stackSizeLimit*3));
		
		Assertions.assertEquals(2, player.removeItem(2, 2)); 
		Assertions.assertTrue(player.acquireItem(1, 2));
		Assertions.assertEquals(1, player.removeItem(1, 2));
		
		Assertions.assertEquals(3, player.countSlotsOccupiedBy(1));
		ArrayList<invPair> inventory = player.getInventory();
		Assertions.assertNull(inventory.get(1)); //ensures slot merged from is emptied
		Assertions.assertEquals(-1, player.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1));
	}
	@Test
	void testRemovingItems3() {
		//tests when slot to be merged from is before other slots with the same itemID
		Assertions.assertTrue(player.acquireItem(2, 2));
		Assertions.assertTrue(player.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(player.acquireItem(3, itemStore.getItem(2).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(player.acquireItem(4, itemStore.getItem(3).getComponent(InventoryComponent.class).stackSizeLimit*3));
		
		Assertions.assertEquals(2, player.removeItem(2, 2)); 
		Assertions.assertTrue(player.acquireItem(1, 2));
		Assertions.assertEquals(1, player.removeItem(1, 2));
		
		Assertions.assertEquals(3, player.countSlotsOccupiedBy(1));
		ArrayList<invPair> inventory = player.getInventory();
		Assertions.assertNull(inventory.get(0)); //ensures slot merged from is emptied
		Assertions.assertEquals(-1, player.removeItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3 + 1));
	}
	
}