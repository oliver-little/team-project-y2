package teamproject.wipeout.game.farm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.game.item.Item;

import java.util.ArrayList;

public class FarmUI extends VBox implements DialogUIComponent {

    protected FarmData data;

    private Pane parent;

    public FarmUI(FarmData farmData) {
        super();

        this.data = farmData;

        this.setAlignment(Pos.CENTER);

        ListView<String> listView = new ListView<String>();
        listView.setOrientation(Orientation.VERTICAL);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setPadding(new Insets(0, 10, 10, 10));
        listView.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));

        ObservableList<String> items = this.getList(this.data.items);
        listView.setItems(items);
        Label emptyLabel = new Label("Your farm is empty");
        listView.setPlaceholder(emptyLabel);

        listView.setOnMouseClicked((mouseEvent) -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            items.remove(selected);
            this.data.pickItem(selected);
        });

        Button closeButton = new Button("X");
        closeButton.setOnAction((actionEvent) -> this.parent.getChildren().remove(this));

        HBox hbox = new HBox(closeButton);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(10, 10, 10, 10));
        hbox.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));

        this.getChildren().addAll(hbox, listView);
    }

    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }

    protected ObservableList<String> getList(ArrayList<ArrayList<Pair<Item, Double>>> items) {
        ObservableList<String> itemList = FXCollections.observableArrayList();
        for (ArrayList<Pair<Item, Double>> row : items) {
            for (Pair<Item, Double> pair : row) {
                if (pair != null) {
                    Item item = pair.getKey();
                    if (item != null) {
                        itemList.add(item.name);
                    }
                }
            }
        }
        return itemList;
    }

}
