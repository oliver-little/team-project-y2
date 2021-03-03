package teamproject.wipeout.game.farm;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import teamproject.wipeout.engine.component.ui.DialogUIComponent;
import teamproject.wipeout.game.assetmanagement.SpriteManager;

import java.util.ArrayList;

public class FarmUI extends VBox implements DialogUIComponent {

    protected FarmData data;

    private Pane parent;

    public FarmUI(FarmData farmData, SpriteManager spriteManager) {
        super();

        this.data = farmData;

        this.setAlignment(Pos.CENTER);

        ListView<FarmItem> listView = new ListView<FarmItem>();
        listView.setCellFactory(new Callback<ListView<FarmItem>, ListCell<FarmItem>>() {
            @Override
            public ListCell<FarmItem> call(ListView<FarmItem> param) {
                return new FarmItemCell((event) -> data.pickItemAt(0, 0), spriteManager);
            }
        });
        listView.setOrientation(Orientation.VERTICAL);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setPadding(new Insets(0, 10, 10, 10));
        listView.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));

        ObservableList<FarmItem> items = this.getList(this.data.items);
        listView.setItems(items);
        Label emptyLabel = new Label("Your farm is empty");
        listView.setPlaceholder(emptyLabel);

        listView.setOnMouseClicked((mouseEvent) -> {
            String selected = listView.getSelectionModel().getSelectedItem().get().name;
            items.remove(selected);
            this.data.pickItem(selected);
        });

        Button closeButton = new Button("X");
        closeButton.setCancelButton(true);
        closeButton.setOnAction((event) -> this.parent.getChildren().remove(this));

        HBox hbox = new HBox(closeButton);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(10, 10, 10, 10));
        hbox.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));

        this.getChildren().addAll(hbox, listView);
        this.setPadding(new Insets(50, 50, 50, 50));
    }

    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public Parent getContent() {
        return this;
    }

    protected ObservableList<FarmItem> getList(ArrayList<ArrayList<FarmItem>> items) {
        ObservableList<FarmItem> itemList = FXCollections.observableArrayList();
        for (ArrayList<FarmItem> row : items) {
            for (FarmItem farmItem : row) {
                if (farmItem != null && farmItem.get() != null) {
                    itemList.add(farmItem);
                }
            }
        }

        this.data.setGrowthCallback((farmItem) -> {
            Platform.runLater(() -> {
                int itemIndex = itemList.indexOf(farmItem);
                if (itemIndex >= 0) {
                    itemList.set(itemIndex, farmItem);
                }
            });
        });

        return itemList;
    }

}
