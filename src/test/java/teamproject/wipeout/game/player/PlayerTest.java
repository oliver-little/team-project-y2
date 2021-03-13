package teamproject.wipeout.game.player;



import java.io.FileNotFoundException;
import java.io.IOException;
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
	
	
	
}