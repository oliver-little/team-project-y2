package teamproject.wipeout.game.UI;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that constructs the game mode selection UI
 */
public class GameModeUI extends GridPane {

	private static final int SECONDS_PER_MIN = 60;

    private final Label valueLabel;
    private final ComboBox<String> gameModeSelector;

    private static Map<String, Object> getGameModeData(GameMode gamemode) {
    	// This could be improved by pulling the data from a JSON instead of hardcoding
        HashMap<String, Object> data = new HashMap<String, Object>();
        if (gamemode == GameMode.TIME_MODE) {
            data.put("desc", "Minutes");
            data.put("min", 5);
            data.put("max", 30);
            data.put("default", 10);

        } else if (gamemode == GameMode.WEALTH_MODE) {
            data.put("desc", "Money Target");
            data.put("min", 50);
            data.put("max", 1000);
            data.put("default", 100);

        } else {
            return null;
        }

        return data;
    }

    public GameModeUI() {
        this.setAlignment(Pos.CENTER);
        this.setHgap(10);
        this.setVgap(20);
        this.setPadding(new Insets(10, 10, 10, 10));

        GameMode[] gameModes = GameMode.values();
        String[] gameModeStrings = new String[gameModes.length];
        for (int i = 0; i < gameModes.length; i++) {
            gameModeStrings[i] = gameModes[i].toString();
        }

        Label valueDesc = new Label();
        valueDesc.setAlignment(Pos.CENTER_RIGHT);
        valueDesc.getStyleClass().add("black-label");

        Label gameModeLabel = new Label("Game Mode:");
        gameModeLabel.setAlignment(Pos.CENTER_RIGHT);
        gameModeLabel.getStyleClass().add("black-label");

        valueLabel = new Label();
        valueLabel.setAlignment(Pos.CENTER);
        valueLabel.getStyleClass().add("black-label");

        gameModeSelector = new ComboBox<String>(FXCollections.observableArrayList(gameModeStrings));
        gameModeSelector.setOnAction((event) -> {
            Map<String, Object> gameModeData = getGameModeData(this.getGameMode());
            valueLabel.setText(Integer.toString((int) gameModeData.get("default")));
            valueDesc.setText((String) gameModeData.get("desc") + ":");
        });
        gameModeSelector.getSelectionModel().selectFirst();

        // trigger event to set value label
        Event.fireEvent(gameModeSelector, new ActionEvent());

        int interval = 5;

        Button decrementButton = new Button("-");
        decrementButton.getStyleClass().add("small-button");
        decrementButton.setOnAction((event) -> {
            int value = Integer.parseInt(valueLabel.getText());
            Map<String, Object> gameModeData = getGameModeData(this.getGameMode());

            if (value - interval >= ((int) gameModeData.get("min"))) {
                valueLabel.setText(Integer.toString(value - interval));
            }
        });

        Button incrementButton = new Button("+");
        incrementButton.getStyleClass().add("small-button");
        incrementButton.setOnAction((event) -> {
            int value = Integer.parseInt(valueLabel.getText());
            Map<String, Object> gamemodeData = getGameModeData(this.getGameMode());
            if (value + interval <= ((int) gamemodeData.get("max"))) {
                valueLabel.setText(Integer.toString(value + interval));
            }

        });

        HBox valueBox = new HBox();
        valueBox.setSpacing(3);
        valueBox.setAlignment(Pos.CENTER_LEFT);
        valueBox.getChildren().addAll(decrementButton, valueLabel, incrementButton);

        this.add(gameModeLabel, 0, 0, 1, 1);
        this.add(valueDesc, 0, 1, 1, 1);
        this.add(gameModeSelector, 1, 0, 2, 1);
        this.add(valueBox, 1, 1, 2, 1);
    }

    public double getValue() {
        double val = Integer.parseInt(valueLabel.getText());

        if (this.getGameMode() == GameMode.TIME_MODE) {
            return val * SECONDS_PER_MIN;
        } else {
            return val;
        }
    }

    public GameMode getGameMode() {
        if (gameModeSelector.getValue().equals(GameMode.TIME_MODE.toString())) {
            return GameMode.TIME_MODE;

        } else if (gameModeSelector.getValue().equals(GameMode.WEALTH_MODE.toString())) {
            return GameMode.WEALTH_MODE;

        } else {
            return null;
        }
    }
}
