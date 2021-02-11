package teamproject.wipeout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

	@Override
	public void start(Stage stage) throws Exception
	{
		double windowWidth = 800;
        double windowHeight = 600;
		StartMenu menu = new StartMenu();
		Scene scene = new Scene(menu.getContent(), windowWidth, windowHeight);
		
		stage.setScene(scene);
		stage.show();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}