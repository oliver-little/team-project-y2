package teamproject.wipeout.game.player.ui;

import javafx.geometry.Pos;
import javafx.scene.Group;
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

public class MoneyUI extends StackPane {

    Group root;

    private Rectangle rectangle = new Rectangle();
    private Text moneyText = new Text();

    public MoneyUI(Player player) {
        super();

        this.root = new Group(); //sets the root node of the inventory UI scene graph
        this.getChildren().add(this.root);

        StackPane.setAlignment(root, Pos.TOP_CENTER);

        this.rectangle.setX(0);
        this.rectangle.setY(0);
        this.rectangle.setWidth(150);
        this.rectangle.setHeight(50);
        this.rectangle.setFill(Color.LIGHTGREY);
        this.rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
        this.rectangle.setMouseTransparent(true);

        this.moneyText.setText("Money: " + player.money.toString());
        this.moneyText.setFill(Color.MAROON);

        this.moneyText.setX(30);
        this.moneyText.setY(25);

        this.root.getChildren().addAll(rectangle, moneyText);
    }

    public void showMoney(Double money) {
        moneyText.setText("Money: " + money.toString());
    }
}
