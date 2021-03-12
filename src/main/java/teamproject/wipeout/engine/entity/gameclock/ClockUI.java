package teamproject.wipeout.engine.entity.gameclock;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.game.player.Player;

public class ClockUI extends VBox implements DialogUIComponent {

    private Pane parent;
    private StackPane stack;

    private Double time;
    private final Double INITIAL_TIME;

    private Rectangle rectangle = new Rectangle();
    private Text timeText = new Text();

    public ClockUI(Double time) {
        super();
        this.rectangle.setX(0);
        this.rectangle.setY(0);
        this.rectangle.setWidth(150);
        this.rectangle.setHeight(50);
        this.rectangle.setFill(Color.LIGHTGREY);
        this.rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
        this.rectangle.setMouseTransparent(true);


        this.time = time;
        this.INITIAL_TIME = time;
        int min = (int)(this.time / 60);
        int seconds = (int)(this.time % 60);
        timeText = new Text("Remaining time: " + min + ":" + seconds);

        this.stack = new StackPane();

        this.stack.getChildren().addAll(rectangle, timeText);
        this.getChildren().add(this.stack);
    }

    public void showTime(Double timestep) {
        this.time -= timestep;
        int min = (int)(this.time / 60);
        int seconds = (int)(this.time % 60);
        timeText.setText("Remaining time: " + min + ":" + seconds);
    }

    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }
    public void restart(){
        this.time = this.INITIAL_TIME;
    }
}
