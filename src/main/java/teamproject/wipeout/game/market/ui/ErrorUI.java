package teamproject.wipeout.game.market.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ErrorUI {
    
    public static final int FADE_DURATION = 2;
    public static final int MESSAGE_DURATION = 3;

    public enum ERROR_TYPE {
        MONEY,
        TASKS_FULL,
        TASK_EXISTS,
        INVENTORY_FULL,
        INVENTORY_EMPTY
    }

    private Label errorMessageLabel;

    public ErrorUI(StackPane errorPane, ERROR_TYPE errorType) {
        if (errorType == ERROR_TYPE.MONEY) {
            this.errorMessageLabel = new Label("Cannot purchase: Insufficient Funds!");
        } else if (errorType == ERROR_TYPE.TASKS_FULL) {
            this.errorMessageLabel = new Label("Cannot purchase: Task List Full!"); 
        } else if (errorType == ERROR_TYPE.TASK_EXISTS) {
            this.errorMessageLabel = new Label("Cannot purchase: Task Already Owned!"); 
        } else if (errorType == ERROR_TYPE.INVENTORY_FULL) {
            this.errorMessageLabel = new Label("Cannot purchase: Inventory Full!"); 
        } else if (errorType == ERROR_TYPE.INVENTORY_EMPTY) {
            this.errorMessageLabel = new Label("Cannot sell: Item Not Owned!"); 
        } else {
            throw new IllegalArgumentException("Invalid error type passed to ErrorUI.");
        }

        errorMessageLabel.setStyle("-fx-font-family: 'Kalam'; -fx-font-size: 20pt; -fx-text-fill: rgba(255, 0, 0, 1); -fx-font-weight: bold;");

        HBox errorBackground = new HBox();

        StackPane.setAlignment(errorBackground, Pos.BOTTOM_CENTER);
        StackPane.setMargin(errorBackground, new Insets(0, 0, 68, 0));

        errorBackground.setAlignment(Pos.CENTER);
        errorBackground.setMaxHeight(10);
        errorBackground.setMaxWidth(500);
        errorBackground.setStyle("-fx-background-color: rgba(200, 200, 200, 1); -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: rgba(0, 0, 0, 1);");
        errorBackground.getChildren().addAll(errorMessageLabel);

        errorPane.getChildren().addAll(errorBackground);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(FADE_DURATION), errorBackground);
        PauseTransition pause = new PauseTransition(Duration.seconds(MESSAGE_DURATION));
        
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition sequentialTransition = new SequentialTransition(pause, fadeOut);
        
        sequentialTransition.setOnFinished((finish) -> {
            errorPane.getChildren().remove(errorBackground);
        });               
        sequentialTransition.play();
    }
}
