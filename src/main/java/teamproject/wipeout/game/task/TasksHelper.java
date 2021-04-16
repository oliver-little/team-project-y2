package teamproject.wipeout.game.task;

import teamproject.wipeout.game.inventory.InventoryItem;
import teamproject.wipeout.game.item.ItemStore;
import teamproject.wipeout.game.player.CurrentPlayer;

import java.util.ArrayList;
import java.util.Collections;

public class TasksHelper {

    public static ArrayList<Task> createTasks(ItemStore itemStore, CurrentPlayer currentPlayer) {
        ArrayList<Task> allTasks = createAllTasks(itemStore);

        ArrayList<Task> playerTasks = new ArrayList<Task>();
        for(int t = 0; t < 7; t++) {
            playerTasks.add(allTasks.get(t));
        }
        currentPlayer.setTasks(playerTasks);

        return allTasks;
    }

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
        int reward = 5;
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
        reward = 2;
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

}
