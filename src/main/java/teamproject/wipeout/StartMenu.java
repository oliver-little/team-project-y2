package teamproject.wipeout;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.beans.binding.Bindings;
import teamproject.wipeout.App;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class StartMenu implements Controller{
	
	private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private Pane root = new StackPane();
    private VBox menuBox = new VBox(-5);
    private Line line;
    //private Stage stage;
    
    private List<Pair<String, Runnable>> menuData = Arrays.asList(
            new Pair<String, Runnable>("Play", () -> {App app = new App();
				            							root.getScene().setRoot(app.getContent());
				            							app.createContent();}),
            new Pair<String, Runnable>("TODO", () -> {}),
            new Pair<String, Runnable>("TODO", () -> {}),
            new Pair<String, Runnable>("TODO", () -> {}),
            new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
    );

    private void createContent() {
        addBackground();
        addTitle();

        double menuPosX = WIDTH / 2 - 100;
        double menuPosY = HEIGHT / 3 + 50;

        addMenu(menuPosX + 5, menuPosY + 5);

        startAnimation();
    }

    private void addBackground() {
        ImageView imageView;
		try
		{
			imageView = new ImageView(new Image(new FileInputStream("src\\main\\resources\\background.png")));
			imageView.setFitWidth(WIDTH);
	        imageView.setFitHeight(HEIGHT);

	        root.getChildren().add(imageView);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
        
    }

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
    

    private void addMenu(double x, double y) {
        menuBox.setTranslateX(x);
        menuBox.setTranslateY(y);
        menuData.forEach(data -> {
            Pane pane = new Pane();
            Polygon bg = new Polygon(
                    0, 0,
                    200, 0,
                    215, 15,
                    200, 30,
                    0, 30
            );
            bg.setStroke(Color.color(1, 1, 1, 0.75));
            bg.setEffect(new GaussianBlur());

            bg.fillProperty().bind(
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

            pane.getChildren().addAll(bg, text);
            pane.setOnMouseClicked(e -> data.getValue().run());
            pane.setTranslateX(-300);

            Rectangle clip = new Rectangle(300, 30);
            clip.translateXProperty().bind(pane.translateXProperty().negate());

            pane.setClip(clip);

            menuBox.getChildren().addAll(pane);
        });

        root.getChildren().add(menuBox);
    }
	
	@Override
	public Parent getContent()
	{
		createContent();
		return root;
	}
	
	
}