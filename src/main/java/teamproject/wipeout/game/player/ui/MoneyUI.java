package teamproject.wipeout.game.player.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import teamproject.wipeout.game.player.CurrentPlayer;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

/**
 * Creates the MoneyUI that extends the Label, and it will be shown top center
 */
public class MoneyUI extends Label {

    /**
     * Creates the UI for showing the amount of money the currentPlayer has
     * @param currentPlayer - the current Player who holds the amount of money we will display
     */
    public MoneyUI(CurrentPlayer currentPlayer) {
        super();

        try {
            InputStream path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
            Font.loadFont(path, 12);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "game-ui.css");
        this.setId("money");

        this.textProperty().bind(Bindings.concat("Money: $").concat(Bindings.createStringBinding(() -> {
            return String.format("%.2f",  currentPlayer.moneyProperty().getValue());
        }, currentPlayer.moneyProperty())));
    }
}
