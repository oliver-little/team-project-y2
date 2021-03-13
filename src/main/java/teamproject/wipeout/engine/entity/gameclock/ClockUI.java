package teamproject.wipeout.engine.entity.gameclock;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.game.player.Player;

public class ClockUI extends StackPane {

    Group root;

    private Double time;
    private final Double INITIAL_TIME;

    private Rectangle rectangle = new Rectangle();
    private Text timeText;

    public ClockUI(Double time) {
        super();

        this.root = new Group(); //sets the root node of the inventory UI scene graph
        this.getChildren().add(this.root);

        StackPane.setAlignment(root, Pos.TOP_RIGHT);

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
        this.timeText = new Text("Remaining time: " + min + ":" + seconds);
        this.timeText.setX(20);
        this.timeText.setY(25);

        this.root.getChildren().addAll(this.rectangle, this.timeText);
    }

    public void showTime(Double timestep) {
        this.time -= timestep;
        int min = (int)(this.time / 60);
        int seconds = (int)(this.time % 60);
        timeText.setText("Remaining time: " + min + ":" + seconds);
    }

    public void restart(){
        this.time = this.INITIAL_TIME;
    }
}
