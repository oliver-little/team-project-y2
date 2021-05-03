package teamproject.wipeout.game.UI;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that constructs the game mode selection UI
 */
public class GameModeUI extends VBox {

	private static final int SECONDS_PER_MIN = 60;

    private final Label valueLabel;
    private final ComboBox<String> gameModeSelector;

    private static Map<String, Object> getGameModeData(GameMode gamemode) {
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

        Label gameModeLabel = new Label("Game Mode:");
        gameModeLabel.getStyleClass().add("black-label");

        Label valueDesc = new Label();
        valueDesc.getStyleClass().add("black-label");

        HBox valueBox = new HBox();
        valueBox.getStyleClass().add("hbox");
        valueBox.setSpacing(3);
        valueBox.setAlignment(Pos.CENTER);

        valueLabel = new Label();
        valueLabel.getStyleClass().add("black-label");

        GameMode[] gameModes = GameMode.values();
        String[] gameModeStrings = new String[gameModes.length];
        for (int i = 0; i < gameModes.length; i++) {
            gameModeStrings[i] = gameModes[i].toString();
        }

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

        valueBox.getChildren().addAll(decrementButton, valueLabel, incrementButton);

		HBox gameModeBox = new HBox();
		gameModeBox.setSpacing(5);
		gameModeBox.setAlignment(Pos.CENTER);
		gameModeBox.getChildren().addAll(gameModeLabel, gameModeSelector);

		HBox modeValueBox = new HBox();
		modeValueBox.setSpacing(20);
		modeValueBox.setAlignment(Pos.CENTER);
		modeValueBox.getChildren().addAll(valueDesc, valueBox);

        this.getChildren().addAll(gameModeBox, modeValueBox);
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
