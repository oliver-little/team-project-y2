package teamproject.wipeout.game.player.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.game.player.Player;

public class MoneyUI extends VBox implements DialogUIComponent {

    private Pane parent;
    private StackPane stack;

    private Rectangle rectangle = new Rectangle();
    private Text moneyText = new Text();

    public MoneyUI(Player player) {
        super();

        this.rectangle.setX(0);
        this.rectangle.setY(0);
        this.rectangle.setWidth(150);
        this.rectangle.setHeight(50);
        this.rectangle.setFill(Color.LIGHTGREY);
        this.rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
        this.rectangle.setMouseTransparent(true);

        this.moneyText.setText("Money: " + player.money.toString());
        this.moneyText.setFill(Color.MAROON);

        this.stack = new StackPane();

        this.stack.getChildren().addAll(rectangle, moneyText);
        this.getChildren().add(this.stack);
    }

    public void showMoney(Double money) {
        moneyText.setText("Money: " + money.toString());
    }

    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }
}
