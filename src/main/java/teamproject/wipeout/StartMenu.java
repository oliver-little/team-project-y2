package teamproject.wipeout;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * StartMenu is a class which is used for creating and setting up the start menu of the game.
 * It implements the Controller Interface.
 */
public class StartMenu implements Controller {
    
    private Pane root = new StackPane();
    private VBox menuBox = new VBox(30);
    private VBox buttonBox;
    private Text title;
    
    private List<Pair<String, Runnable>> menuData = Arrays.asList(
            new Pair<String, Runnable>("Play", () -> {
                App app = new App();
                Window window = root.getScene().getWindow();
                Parent content = app.init(window.widthProperty(), window.heightProperty());
                root.getScene().setRoot(content);
                app.createContent();}), // (creating content is called separately after so InputHandler has a scene to add listeners to.)
            //new Pair<String, Runnable>("TODO", () -> {}), // Commented out until implemented
            //new Pair<String, Runnable>("TODO", () -> {}),
            //new Pair<String, Runnable>("TODO", () -> {}),
            new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
    );

    public void cleanup() {
        
    }

    /**
     * Creates the content to be rendered onto the canvas.
     */
    private void createContent() {
        root.setPrefSize(800, 600);

        root.getStylesheets().add(ResourceType.STYLESHEET.path + "start-menu.css");

        menuBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(menuBox, Pos.CENTER);

        addBackground();

        addTitle();
        addMenu();

        root.getChildren().add(menuBox);

        startAnimation();
    }

    /**
     * A method to add the background to the menu.
     */
    private void addBackground() {
        ImageView imageView;
		try {
		    FileInputStream imgFile = new FileInputStream(ResourceLoader.get(ResourceType.UI, "background.png"));
			imageView = new ImageView(new Image(imgFile));
            ColorAdjust brightness = new ColorAdjust();
            brightness.setBrightness(-0.2);
            brightness.setInput(new GaussianBlur(30));
            imageView.setEffect(brightness);
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(Bindings.add(root.widthProperty(), 50));

	        root.getChildren().add(imageView);

		} catch (FileNotFoundException exception) {
            exception.printStackTrace();
		}
    }

    /**
     * A method to add the title to the menu.
     */
    private void addTitle() {
    	title = new Text("Farmageddon");

        try {
            InputStream path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
            Font.loadFont(path, 12);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    	title.setFont(Font.font("Kalam", 40));
    	title.setFill(Color.WHITE);

        menuBox.getChildren().addAll(title);
    }

    /**
     * A method to animate the menu items.
     */
    private void startAnimation() {
        double titleDuration = 0.5;
        double buttonDuration = 0.5;

        // Set initial values
        title.setScaleX(2);
        title.setScaleY(2);
        buttonBox.setOpacity(0);

        ScaleTransition st = new ScaleTransition(Duration.seconds(titleDuration), title);
        st.setFromX(2);
        st.setFromY(2);
        st.setToX(1);
        st.setToY(1);

        FadeTransition ft = new FadeTransition(Duration.seconds(buttonDuration), buttonBox);
        ft.setFromValue(0);
        ft.setToValue(1);

        SequentialTransition full = new SequentialTransition(st, ft);
        full.setDelay(Duration.seconds(1.25));
        full.setInterpolator(Interpolator.EASE_BOTH);
        full.play();
    }
    
    /**
     * A method to add the menu items to the menu.
     * @param x x-position of the menu.
     * @param y y-position of the menu.
     */
    private void addMenu() {
        buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        menuData.forEach(data -> {
            Button button = new Button(data.getKey());
            button.setOnAction(((event) -> data.getValue().run()));
            buttonBox.getChildren().add(button);
        });
        menuBox.getChildren().add(buttonBox); //menu box added to the root node.
    }
	
    /**
     * Creates the content of the menu and then gets the root node of this class.
     * @return StackPane (root) which contains all the menu components in the scene graph.
     */
	@Override
	public Parent getContent()
	{
		createContent();
		return root;
	}
}