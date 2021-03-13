package teamproject.wipeout.game.task;

import teamproject.wipeout.game.player.Player;

import java.util.function.Function;

/** Defines a task that a player has to complete to get a certain amount of money **/

public class Task {
    public Integer id;
    public Integer reward;
    public String description;
    public Boolean completed;
    public Integer priceToBuy;
    public Function<Player, Boolean> condition;

    /**
     * Creates a new task
     * @param description - description text of the task
     * @param reward - how much money does the player get when they complete the task
     * @param condition - the condition under which the task is considered completed by the player
     */
    public Task(Integer id, String description, Integer reward, Function<Player, Boolean> condition) {
        this.id = id;
        this.description = description;
        this.reward = reward;
        this.condition = condition;
        this.completed = false;
        this.priceToBuy = reward - 10;
    }
}