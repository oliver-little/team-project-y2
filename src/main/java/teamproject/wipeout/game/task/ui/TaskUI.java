package teamproject.wipeout.game.task.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import teamproject.wipeout.engine.component.Transform;
import teamproject.wipeout.engine.component.render.RenderComponent;
import teamproject.wipeout.engine.component.render.TextRenderable;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;

public class TaskUI extends VBox implements DialogUIComponent {
    public String description;
    private Pane parent;

    public TaskUI(String description) {
        super();

        this.setAlignment(Pos.CENTER);

//        TabPane tabPane = new TabPane();
//        Tab buy = new Tab("Buy", new Label("Buy some items."));
//        Tab sell = new Tab("Sell", new Label("Sell some items."));
//        tabPane.getTabs().addAll(buy, sell);
//        Button close = new Button("X");
//        close.setOnAction(actionEvent -> this.parent.getChildren().remove(this));
//
//        this.getChildren().addAll(tabPane, close);
    }

    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }
}
