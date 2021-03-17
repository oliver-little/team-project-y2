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
	
	//items are rearranged only when items are sold to ensure items are arranged efficiently.
	//when items are sold, the removeItem method is called with the item's ID and quantity to be removed
	
	@Test
	void testRemovingItems() {
		for(int i = 1; i <= MAX_SIZE; i++) {
			Assertions.assertTrue(player.acquireItem(i, itemStore.getItem(i).getComponent(InventoryComponent.class).stackSizeLimit));
		}
		for(int i = 0; i < MAX_SIZE; i++) {
			player.selectSlot(i);
			Assertions.assertEquals(player.dropItem(), i+1); //checks itemID of individual item dropped returned
		}
		for(int i = 0; i < MAX_SIZE; i++) {
			player.selectSlot(i);
			Assertions.assertTrue(player.removeItemFromSelectedSlot(i, itemStore.getItem(i+1).getComponent(InventoryComponent.class).stackSizeLimit - 1)); //checks multiple items dropped
		}
		
		Assertions.assertEquals(0, player.occupiedSlots); //checks slots are all unoccupied
		
		ArrayList<invPair> inventory = player.getInventory();
		for(invPair pair : inventory) {
			Assertions.assertNull(pair); //checks slots are all null
		}
		
		
	}
	
	@Test
	void testSlotRearrangement1() {
		//tests when slot to be rearranged is before other slots with the same itemID
		//adds 3 types of item to 9 slots
		Assertions.assertTrue(player.acquireItem(1, itemStore.getItem(1).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(player.acquireItem(2, itemStore.getItem(2).getComponent(InventoryComponent.class).stackSizeLimit*3));
		Assertions.assertTrue(player.acquireItem(3, itemStore.getItem(3).getComponent(InventoryComponent.class).stackSizeLimit*3));
		
	}
	@Test
	void testSlotRearrangement2() {
		//tests when slot to be rearranged is in the middle of other slots with the same itemID
		
		
	}
	@Test
	void testSlotRearrangement3() {
		//tests when slot to be rearranged is after other slots with the same itemID
		
		
	}
	
	
	
}