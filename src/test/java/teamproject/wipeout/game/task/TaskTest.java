package teamproject.wipeout.game.task;

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
import teamproject.wipeout.game.item.components.PlantComponent;
import teamproject.wipeout.game.market.Market;
import teamproject.wipeout.game.market.ui.MarketUI;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.game.task.ui.TaskUI;
import teamproject.wipeout.util.resources.PlayerSpriteSheetManager;
import teamproject.wipeout.util.resources.ResourceLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TaskTest {

    private static GameScene scene = new GameScene();
    private static CurrentPlayer currentPlayer;
    private static ItemStore itemStore;
    private static ArrayList<Task> allTasks;
    private static Market market;

    private static ArrayList<Task> createAllTasks(ItemStore itemStore) {
        ArrayList<Task> tasks = new ArrayList<>();
        ArrayList<Integer> itemIds  = new ArrayList<>();
        for(int i = 1; i < 25; i++) {
            if(itemStore.getItem(i) != null) {
                itemIds.add(i);
            }
        }

        int nrOfTask = 0;
        // Collect tasks
        int reward = 7;
        for(Integer itemId : itemIds) {
            String name = itemStore.getItem(itemId).name;
            int quantityCollected = 1;
            Task currentTask =  new Task(nrOfTask, "Collect " + quantityCollected + " " + name, reward * quantityCollected,
                    (CurrentPlayer inputPlayer) ->
                    {
                        ArrayList<InventoryItem> inventoryList = inputPlayer.getInventory();
                        int index = inputPlayer.containsItem(itemId);
                        if(index >= 0 && inventoryList.get(index).quantity >= quantityCollected) {
                            return true;
                        }
                        return false;
                    },
                    itemStore.getItem(itemId)
            );
            tasks.add(currentTask);
            nrOfTask += 1;
        }

        // Sell tasks
        reward = 4;
        for(Integer itemId : itemIds) {
            String name = itemStore.getItem(itemId).name;
            int quantitySold = 1;
            Task currentTask = new Task(nrOfTask, "Sell " + quantitySold + " " + name, reward * quantitySold,
                    (CurrentPlayer inputPlayer) -> inputPlayer.getSoldItems().containsKey(itemId),
                    itemStore.getItem(itemId)
            );
            tasks.add(currentTask);
            nrOfTask += 1;
        }

        Collections.shuffle(tasks);
        return tasks;
    }

    private static CurrentPlayer setUpCurrentPlayer() throws IOException  {
        SpriteManager spriteManager = new SpriteManager();
        //spriteManager.loadSpriteSheet("player/player-red-descriptor.json", "player/player-red.png");
        spriteManager.loadSpriteSheet("crops/crops-descriptor.json", "crops/crops.png");
        spriteManager.loadSpriteSheet("crops/fruit-tree-descriptor.json", "crops/FruitTrees.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        spriteManager.loadSpriteSheet("inventory/inventory-tools-descriptor.json", "inventory/Tools.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-and-vegetable-descriptor.json", "inventory/FruitsAndVeg.png");
        spriteManager.loadSpriteSheet("inventory/inventory-vegetables-descriptor.json", "inventory/Vegetables.png");
        spriteManager.loadSpriteSheet("inventory/inventory-fruit-descriptor.json", "inventory/Fruits.png");
        PlayerSpriteSheetManager.loadPlayerSpriteSheets(spriteManager);
        int playerID = new Random().nextInt(1024);
        InventoryUI invUI = new InventoryUI(spriteManager, itemStore);
        Pair<Integer, String> playerInfo = new Pair<Integer, String>(playerID, "testPlayer");
        currentPlayer = new CurrentPlayer(scene, playerInfo, null, spriteManager, itemStore);
        currentPlayer.setInventoryUI(invUI);
        currentPlayer.debug = true; //disables the error messages, which require JavaFX toolkit to be initialised
        market = assertDoesNotThrow(() -> new Market(itemStore, false));
        return currentPlayer;
    }

    @BeforeAll
    static void initialization() throws FileNotFoundException, ReflectiveOperationException {
        itemStore = new ItemStore("items.json");
        allTasks = createAllTasks(itemStore);
        try {
            currentPlayer = setUpCurrentPlayer();
        } catch(IOException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @BeforeEach
    void clearTasks() {
        currentPlayer.clearTasks();
    }

    @Test
    void testNoSeedsRelatedTasks() {
        // There should not be any collect seeds or sell seeds tasks
        for(Task task: allTasks) {
            Assertions.assertFalse(task.relatedItem.hasComponent(PlantComponent.class));
        }
    }

    @Test
    void testNotAvailableForPurhcaseTask() {
        TasksHelper.createTasks(itemStore, currentPlayer);
        // The player shouldn't be able to buy a task already in their list of tasks, or already completed
        for(Task task: allTasks) {
            if(currentPlayer.getTasks().contains(task)) {
                Assertions.assertFalse(currentPlayer.buyTask(task));
            } else {
                if(currentPlayer.getTasks().size() < 5) {
                    Assertions.assertTrue(currentPlayer.buyTask(task));
                    Assertions.assertFalse(currentPlayer.buyTask(task));
                }
            }
        }
    }

    @Test
    void testCannotBuyMoreThanTenTasks() {
        TasksHelper.createTasks(itemStore, currentPlayer);
        currentPlayer.setMoney(500);
        // The player shouldn't be able to buy a task already in their list of tasks, or already completed
        for (Task task : allTasks) {
            if (currentPlayer.getTasks().size() >= 10) {
                Assertions.assertFalse(currentPlayer.buyTask(task));
            } else {
                currentPlayer.buyTask(task);
            }
        }
    }

    @Test
    void testNotEnoughMoneyToBuyTask() {
        TasksHelper.createTasks(itemStore, currentPlayer);
        currentPlayer.setMoney(0);
        // The player shouldn't be able to buy a task already in their list of tasks, or already completed
        for (Task task : allTasks) {
            Assertions.assertFalse(currentPlayer.buyTask(task));
        }
    }

}
