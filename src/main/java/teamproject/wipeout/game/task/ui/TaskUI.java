package teamproject.wipeout.game.task.ui;

import javafx.scene.control.*;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.task.Task;

import java.util.ArrayList;

public class TaskUI extends ListView<String> {

    private Integer MAX_TASKS = 5;

    public TaskUI(Player player) {
        super();

        this.setMaxWidth(150);
        this.setMaxHeight(200);
        this.setMouseTransparent( true );
        this.setFocusTraversable( false );
        this.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        showTasks(player.tasks);
    }

    public void showTasks(ArrayList<Task> tasks) {
        int i = 0;
        this.getItems().clear();
        for(Task task: tasks) {
            if (task.completed) {
                continue;
            }
            this.getItems().add(task.description);
            i += 1;
        }
        while(i < MAX_TASKS) {
            this.getItems().add("");
            i += 1;
        }
    }
}
