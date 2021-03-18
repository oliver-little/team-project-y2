package teamproject.wipeout.game.player.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import teamproject.wipeout.game.player.Player;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

public class MoneyUI extends Label {

    public MoneyUI(Player player) {
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
            return String.format("%.2f",  player.moneyProperty().getValue());
        }, player.moneyProperty())));
    }
}
