package teamproject.wipeout.game.task.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.task.Task;
import teamproject.wipeout.util.resources.ResourceType;

import java.util.ArrayList;

public class TaskUI extends VBox {

    private ListView<String> list;
    private Button openCloseButton;

    private Integer MAX_TASKS = 10;

    private boolean opened = true;

    public TaskUI(Player player) {
        super();

        this.setSpacing(2);

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "task-ui.css");

        openCloseButton = new Button("Hide Tasks");
        openCloseButton.setOnAction(e -> {
            opened = !opened;
            this.setListVisible(opened);
        });

        list = new ListView<>();

        list.setMaxWidth(180);
        list.setMaxHeight(240);
        list.setMouseTransparent( true );
        list.setFocusTraversable( false );
        list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        this.getChildren().addAll(openCloseButton, list);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        showTasks(player.tasks);
    }

    public void showTasks(ArrayList<Task> tasks) {
        int i = 0;
        list.getItems().clear();
        for(Task task: tasks) {
            if (task.completed) {
                continue;
            }
            list.getItems().add(task.description);
            i += 1;
        }
        while(i < MAX_TASKS) {
            list.getItems().add("");
            i += 1;
        }
    }

    private void setListVisible(boolean visible) {
        KeyValue goalWidth = null;
        if (visible) {
            openCloseButton.setText("Hide Tasks");

            Rectangle clipRect = new Rectangle(0, 0, list.getWidth(), 0);
            list.setClip(clipRect);
            goalWidth = new KeyValue(clipRect.heightProperty(), list.getHeight(), Interpolator.EASE_OUT);
        }
        else {
            openCloseButton.setText("Show Tasks");

            Rectangle clipRect = new Rectangle(0, 0, list.getWidth(), list.getHeight());
            list.setClip(clipRect);
            goalWidth = new KeyValue(clipRect.heightProperty(), 0, Interpolator.EASE_IN);
        }

        KeyFrame frame = new KeyFrame(Duration.seconds(0.25), goalWidth);
        Timeline timeline = new Timeline(frame);
        timeline.play();
    }
}
