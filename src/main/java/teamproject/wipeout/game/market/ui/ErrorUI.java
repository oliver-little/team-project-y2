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

/**
 * Used for displaying error messages to the screen, by creating a new instance of "ErrorUI",
 * with the corresponding ENUM error code will display a fadable message on screen.
 */
public class ErrorUI {
    
    public static final float FADE_IN_DURATION = 0.2f;
    public static final int FADE_OUT_DURATION = 2;
    public static final int MESSAGE_DURATION = 3;

    public enum ERROR_TYPE {
        MONEY,
        TASKS_FULL,
        TASK_EXISTS,
        INVENTORY_FULL,
        INVENTORY_EMPTY,
        TASK_COMPLETED
    }
    
    /**
     * Constructor for error message. Displays a passed in error to the screen and fades out after a few seconds.
     * @param errorPane The pane to display the message to.
     * @param errorMessage The message to display.
     * @param onFadeOut The action to run on fade out.
     */
    public ErrorUI(StackPane errorPane, String errorMessage, Runnable onFadeOut) {
    	Label errorMessageLabel;
    	errorMessageLabel = new Label(errorMessage);
    	errorMessageLabel.setStyle("-fx-font-family: 'Kalam'; -fx-font-size: 20pt; -fx-text-fill: rgba(255, 0, 0, 1); -fx-font-weight: bold;");
    	createErrorBox(errorMessageLabel, errorPane, onFadeOut);
    	
    }

    /**
     * List of displayable error messages.
     * @param errorPane Pane that you want to display the error to.
     * @param errorType ENUM error code.
     */
    public ErrorUI(StackPane errorPane, ERROR_TYPE errorType) {
        Label errorMessageLabel;

        switch (errorType) {
            case MONEY:
                errorMessageLabel = new Label("Cannot Purchase: Insufficient Funds!");
                break;
            case TASKS_FULL:
                errorMessageLabel = new Label("Cannot Purchase: Task List Full!");
                break;
            case TASK_EXISTS:
                errorMessageLabel = new Label("Cannot Purchase: Task Already Owned!");
                break;
            case INVENTORY_FULL:
                errorMessageLabel = new Label("Cannot Obtain Item(s): Inventory Full!");
                break;
            case INVENTORY_EMPTY:
                errorMessageLabel = new Label("Cannot Sell: Item(s) Not Owned!");
                break;
            case TASK_COMPLETED:
                errorMessageLabel = new Label("Task Completed!");
                break;
            default:
                throw new IllegalArgumentException("Invalid error type passed to ErrorUI.");
        }

        if (errorType == ERROR_TYPE.TASK_COMPLETED) {
            errorMessageLabel.setStyle("-fx-font-family: 'Kalam'; -fx-font-size: 20pt; -fx-text-fill: rgba(50, 168, 82, 1); -fx-font-weight: bold;");
        } else {
            errorMessageLabel.setStyle("-fx-font-family: 'Kalam'; -fx-font-size: 20pt; -fx-text-fill: rgba(255, 0, 0, 1); -fx-font-weight: bold;");
        }

        createErrorBox(errorMessageLabel, errorPane, null);
    }
    
    /**
     * JavaFX UI code to display the message on screen, displays for a few seconds then fades out.
     * @param errorMessageLabel Text to display.
     * @param errorPane Pane to display error message to.
     * @param onFadeOut Runs on fade out.
     */
    private void createErrorBox(Label errorMessageLabel, StackPane errorPane, Runnable onFadeOut) {
        HBox errorBackground = new HBox();

        StackPane.setAlignment(errorBackground, Pos.BOTTOM_CENTER);
        StackPane.setMargin(errorBackground, new Insets(0, 0, 68, 0));

        errorBackground.setAlignment(Pos.CENTER);
        errorBackground.setMaxHeight(10);
        errorBackground.setMaxWidth(500);
        errorBackground.setStyle("-fx-background-color: rgba(200, 200, 200, 1); -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: rgba(0, 0, 0, 1);");
        errorBackground.getChildren().addAll(errorMessageLabel);

        errorPane.getChildren().addAll(errorBackground);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(FADE_IN_DURATION), errorBackground);
        PauseTransition pause = new PauseTransition(Duration.seconds(MESSAGE_DURATION));
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(FADE_OUT_DURATION), errorBackground);
        
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition sequentialTransition = new SequentialTransition(fadeIn, pause, fadeOut);
        
        sequentialTransition.setOnFinished((finish) -> {
            errorPane.getChildren().remove(errorBackground);
            if (onFadeOut != null) {
                onFadeOut.run();
            }
        });               
        sequentialTransition.play();
    }
}
