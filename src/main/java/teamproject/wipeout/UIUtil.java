package teamproject.wipeout;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class UIUtil {

    /**
     * A method that creates a title with the specified string
     * @param t The text to be displayed in the title
     * @return the created title
     */
    public static Text createTitle(String t) {
        Text title = new Text(t);

        try {
            InputStream path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
            Font.loadFont(path, 12);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        title.setFont(Font.font("Kalam", 40));
        title.setFill(Color.WHITE);

        return title;
    }

    public static VBox createMenu(List<Pair<String, Runnable>> menuData) {
        VBox buttonBox = new VBox(10);
        //menuBox = new VBox(30);
        buttonBox.setAlignment(Pos.CENTER);

        menuData.forEach(data -> {
            Button button = new Button(data.getKey());
            button.setOnAction(((event) -> data.getValue().run()));
            buttonBox.getChildren().add(button);
        });

        return buttonBox;
    }

    /**
     * creates blurred background image from image file
     * @param imgFile image file to use for background
     * @param pane the pane that the background will cover
     * @return the imageview object that can be added to the pane
     */
    public static ImageView createBackground(FileInputStream imgFile, Pane pane) {
        ImageView imageView = new ImageView(new Image(imgFile));
        ColorAdjust brightness = new ColorAdjust();
        brightness.setBrightness(-0.2);
        brightness.setInput(new GaussianBlur(30));
        imageView.setEffect(brightness);
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(Bindings.add(pane.widthProperty(), 50));

        return imageView;

    }


}
