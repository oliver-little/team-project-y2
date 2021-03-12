package teamproject.wipeout;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.beans.binding.Bindings;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * StartMenu is a class which is used for creating and setting up the start menu of the game.
 * It implements the Controller Interface.
 */
public class StartMenu implements Controller {
	
	private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private Pane root = new StackPane();
    private VBox menuBox = new VBox(-5);
    private Line line;
    
    private List<Pair<String, Runnable>> menuData = Arrays.asList(
            new Pair<String, Runnable>("Play", () -> {
                App app = new App();
                Window window = root.getScene().getWindow();
                Parent content = app.init(window.widthProperty(), window.heightProperty());
                root.getScene().setRoot(content);
                app.createContent();}), // (creating content is called separately after so InputHandler has a scene to add listeners to.)
            new Pair<String, Runnable>("TODO", () -> {}),
            new Pair<String, Runnable>("TODO", () -> {}),
            new Pair<String, Runnable>("TODO", () -> {}),
            new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
    );

    public void cleanup() {
        
    }

    /**
     * Creates the content to be rendered onto the canvas.
     */
    private void createContent() {
        addBackground();
        addTitle();

        double menuPosX = WIDTH / 2 - 100;
        double menuPosY = HEIGHT / 3 + 50;

        addMenu(menuPosX + 5, menuPosY + 5);

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
			imageView.setFitWidth(WIDTH);
	        imageView.setFitHeight(HEIGHT);

	        root.getChildren().add(imageView);

		} catch (FileNotFoundException exception) {
            exception.printStackTrace();
		}
    }

    /**
     * A method to add the title to the menu.
     */
    private void addTitle() {
    	Pane pane = new Pane();
    	Text text = new Text("Game Name");
    	text.setFont(Font.font("Arial", 40));
    	text.setFill(Color.WHITE);
        pane.setTranslateX(WIDTH / 2 - text.getLayoutBounds().getWidth() / 2);
        pane.setTranslateY(HEIGHT / 4);
        
        pane.getChildren().addAll(text);
        root.getChildren().add(pane);
    }

    /**
     * A method to animate the menu items.
     */
    private void startAnimation() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), line);
        st.setToY(1);
        st.setOnFinished(e -> {

            for (int i = 0; i < menuBox.getChildren().size(); i++) {
                Node n = menuBox.getChildren().get(i);

                TranslateTransition tt = new TranslateTransition(Duration.seconds(1 + i * 0.15), n);
                tt.setToX(0);
                tt.setOnFinished(e2 -> n.setClip(null));
                tt.play();
            }
        });
        st.play();
    }
    
    /**
     * A method to add the menu items to the menu.
     * @param x x-position of the menu.
     * @param y y-position of the menu.
     */
    private void addMenu(double x, double y) {
        menuBox.setTranslateX(x);
        menuBox.setTranslateY(y);
        menuData.forEach(data -> {
            Pane pane = new Pane();
            Polygon pgon = new Polygon(
                    0, 0,
                    200, 0,
                    215, 15,
                    200, 30,
                    0, 30
            );
            pgon.setStroke(Color.color(1, 1, 1, 0.75));
            pgon.setEffect(new GaussianBlur());

            pgon.fillProperty().bind(
                    Bindings.when(pane.pressedProperty())
                            .then(Color.color(0, 0, 0, 0.75))
                            .otherwise(Color.color(0, 0, 0, 0.25))
            );
            
            Text text = new Text(data.getKey());
            text.setTranslateX(5);
            text.setTranslateY(20);
            text.setFont(Font.font("Arial"));
            text.setFill(Color.WHITE);
            Effect shadow = new DropShadow(5, Color.WHITE);
            Effect blur = new BoxBlur(1, 1, 2);
            text.effectProperty().bind(
                    Bindings.when(pane.hoverProperty())
                            .then(shadow)
                            .otherwise(blur)
            );

            pane.getChildren().addAll(pgon, text); //polygon shape and text added to a pane.
            pane.setOnMouseClicked(e -> data.getValue().run()); //executes the runnable corresponding to the menu item.
            pane.setTranslateX(-300);

            Rectangle clip = new Rectangle(300, 30);
            clip.translateXProperty().bind(pane.translateXProperty().negate());

            pane.setClip(clip);

            menuBox.getChildren().addAll(pane); //pane added to the menu box.
        });

        root.getChildren().add(menuBox); //menu box added to the root node.
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