package teamproject.wipeout;

import java.io.FileInputStream;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

/**
 * Main is a class which contains the main method, from which the GUI is launched, starting at the start menu.
 */
public class Main extends Application{

	private Scene scene;

	/**
	 * Method which starts the application, setting the stage's scene to the start menu first.
	 */
	@Override
	public void start(Stage stage) throws Exception
	{
		StartMenu menu = new StartMenu();
		scene = new Scene(menu.getContent());
		
		FileInputStream imgFile = new FileInputStream(ResourceLoader.get(ResourceType.UI, "Icon.png"));

		stage.getIcons().add(new Image(imgFile));
		stage.setTitle("Farmageddon");
		stage.setScene(scene);
		stage.show();
		
	}

	@Override
	public void stop() {
		if (scene.getRoot() instanceof Controller) {
			Controller view = (Controller) scene.getRoot();
			view.cleanup();
		}
	}
	
	/**
	 * Method from which the app is launched.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
}