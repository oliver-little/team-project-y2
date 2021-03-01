package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
		double windowWidth = 800;
        double windowHeight = 600;
		StartMenu menu = new StartMenu();
		scene = new Scene(menu.getContent(), windowWidth, windowHeight);
		
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