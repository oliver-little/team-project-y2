package teamproject.wipeout.game.task.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RectRenderable;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.game.task.Task;

import java.util.ArrayList;

public class TaskUI extends VBox implements DialogUIComponent {

    public Transform transform;
    public Point2D size;
    public GameScene gameScene;

    private Pane parent;

    private Integer MAX_TASKS = 5;

    private StackPane stack;

    private Rectangle rectangle = new Rectangle();
    private Text[] texts = new Text[MAX_TASKS];
    private ListView<Text> list = new ListView<>();


    public TaskUI(Player player) {
        super();

        this.rectangle.setX(0);
        this.rectangle.setY(0);
        this.rectangle.setWidth(150);
        this.rectangle.setHeight(200);
        this.rectangle.setFill(Color.BLACK);
        this.rectangle.setMouseTransparent(true);

        this.stack = new StackPane();
        list.setMaxWidth(200);
        list.setMaxHeight(200);
        list.setMouseTransparent( true );
        list.setFocusTraversable( false );
        list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        createTextRenderables();

        showTasks(player.tasks);

        this.stack.getChildren().addAll(rectangle, list);
        this.getChildren().add(this.stack);
    }

    public void showTasks(ArrayList<Task> tasks) {
        int i = 0;
        for(Task task: tasks) {
            if (task.completed) {
                continue;
            }
            list.getItems().get(i).setText(task.description);
            i += 1;
        }
        while(i < MAX_TASKS) {
            list.getItems().get(i).setText("");
            i += 1;
        }
    }

    private void createTextRenderables() {
        for(int i = 0; i < MAX_TASKS; i++) {
            texts[i] = new Text("");
            texts[i].setFill(Color.BLACK);
            texts[i].setMouseTransparent(true);
            list.getItems().add(texts[i]);
        }
    }
    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }
}
