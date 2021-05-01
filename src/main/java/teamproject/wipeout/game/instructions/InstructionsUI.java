package teamproject.wipeout.game.instructions;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class InstructionsUI extends VBox {

    private boolean opened = false;
    private Button openCloseButton = new Button();
    private ScrollPane scrollPane = new ScrollPane();
    private ListView<String> list;

    public InstructionsUI(Map<String, KeyCode> keyBindings) {
        super();

        this.getStylesheets().add(ResourceType.STYLESHEET.path + "settings-ui.css");
        try {
            InputStream path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
            Font.loadFont(path, 12);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        VBox box = new VBox();

        HBox buttonBox = new HBox();

        ImageView cog = null;
        try{
            InputStream s = new FileInputStream(ResourceLoader.get(ResourceType.UI, "cog.png"));
            Image img = new Image(s);
            cog = new ImageView(img);
            cog.setSmooth(false);
        } catch(Exception e){
            e.printStackTrace();
        }

        openCloseButton.setAlignment(Pos.BASELINE_RIGHT);
        openCloseButton.setGraphic(cog);
        openCloseButton.setOnAction(e -> {
            opened = !opened;
            this.setMenuVisible(opened);
            if(opened) {
                list.setMaxWidth(180);
                list.setMaxHeight(240);
            } else {
                list.setMaxWidth(0);
                list.setMaxHeight(0);
            }
        });
        buttonBox.getChildren().add(openCloseButton);
        buttonBox.setAlignment(Pos.BASELINE_RIGHT);

        this.getChildren().add(buttonBox);

        box.setSpacing(7);

        list = new ListView<>();

//        list.setMaxWidth(180);
//        list.setMaxHeight(240);
        list.setMouseTransparent( true );
        list.setFocusTraversable( false );
        list.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");

        list.getItems().addAll("one", "two", "three", "four");

        box.getChildren().add(list);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(box);
        this.getChildren().add(scrollPane);
        setMenuVisible(false); //hides menu by default on game start-up



    }

    /**
     * Shows/hides the menu using animation
     * @param visible
     */
    private void setMenuVisible(boolean visible){
        KeyValue goalWidth = null;
        if (visible) {
            Rectangle clipRect = new Rectangle(0, 0, scrollPane.getWidth(), 0);
            scrollPane.setClip(clipRect);
            goalWidth = new KeyValue(clipRect.heightProperty(), scrollPane.getHeight(), Interpolator.EASE_OUT);
        }
        else {
            Rectangle clipRect = new Rectangle(0, 0, scrollPane.getWidth(), scrollPane.getHeight());
            scrollPane.setClip(clipRect);
            goalWidth = new KeyValue(clipRect.heightProperty(), 0, Interpolator.EASE_IN);
        }

        KeyFrame frame = new KeyFrame(Duration.seconds(0.25), goalWidth);
        Timeline timeline = new Timeline(frame);
        timeline.play();
    }
}
