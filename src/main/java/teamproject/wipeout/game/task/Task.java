package teamproject.wipeout.game.task;

import teamproject.wipeout.game.item.Item;
import teamproject.wipeout.game.player.CurrentPlayer;

import java.util.function.Function;

/** Defines a task that a player has to complete to get a certain amount of money **/

public class Task {
    public Integer id;
    public Integer reward;
    public String description;
    public String descriptionWithoutMoney;
    public Boolean completed;
    public double priceToBuy;
    public Item relatedItem;
    public Function<CurrentPlayer, Boolean> condition;

    /**
     * Creates a new task
     * @param description - description text of the task
     * @param reward - how much money does the player get when they complete the task
     * @param condition - the condition under which the task is considered completed by the player
     */
    public Task(Integer id, String descriptionWithoutMoney, Integer reward, Function<CurrentPlayer, Boolean> condition, Item relatedItem) {
        this.id = id;
        this.descriptionWithoutMoney = descriptionWithoutMoney;
        this.description = descriptionWithoutMoney + " ($" + reward.toString() + ")";
        this.reward = reward;
        this.condition = condition;
        this.completed = false;
        this.priceToBuy = (double) reward / 2;
        this.relatedItem = relatedItem;
    }
}