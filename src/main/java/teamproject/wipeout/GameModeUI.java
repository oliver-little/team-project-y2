package teamproject.wipeout;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Class that constructs the gamemode selection UI 
 *
 */
public class GameModeUI extends VBox
{
	
	private Label valueLabel;
	private ComboBox<String> gamemodeSelector;
	
	public GameModeUI() {
		this.setAlignment(Pos.CENTER);
		
        Label valueDesc = new Label();
        
        HBox valueBox = new HBox();
        valueBox.getStyleClass().add("hbox");
        valueBox.setSpacing(3);
        valueBox.setAlignment(Pos.CENTER);
        
        valueLabel = new Label();
        
        GameMode[] gameModes = GameMode.values();
        String[] gamemodeStrings = new String[gameModes.length];
        for (int i = 0; i< gameModes.length; i++) {
        	gamemodeStrings[i] = gameModes[i].toString();
        }
        
        gamemodeSelector = new ComboBox<String>(FXCollections.observableArrayList(gamemodeStrings));
        gamemodeSelector.setOnAction((event) -> {
        	Map<String, Object> gamemodeData = getGamemodeData(this.getGameMode());
        	valueLabel.setText(Integer.toString((int) gamemodeData.get("default")));
        	valueDesc.setText((String) gamemodeData.get("desc")+ ":");
        });
        gamemodeSelector.getSelectionModel().selectFirst();
        // trigger event to set value label
        Event.fireEvent(gamemodeSelector, new ActionEvent());
        
        
        final int interval = 5;
        
        Button decrementButton = new Button("-");
        decrementButton.getStyleClass().add("small-button");
        decrementButton.setOnAction((event) ->{
        	int value = Integer.parseInt(valueLabel.getText());
        	Map<String, Object> gamemodeData = getGamemodeData(this.getGameMode());
        	if (value-interval>=((int)gamemodeData.get("min"))) {
        		valueLabel.setText(Integer.toString(value-interval));
        	}
        	
        });
        
        Button incrementButton = new Button("+");
        incrementButton.getStyleClass().add("small-button");        
        incrementButton.setOnAction((event) ->{
        	int value = Integer.parseInt(valueLabel.getText());
        	Map<String, Object> gamemodeData = getGamemodeData(this.getGameMode());
        	if (value+interval<=((int) gamemodeData.get("max"))) {
        		valueLabel.setText(Integer.toString(value+interval));
        	}
        	
        });
        
        valueBox.getChildren().addAll(decrementButton, valueLabel, incrementButton);
        this.getChildren().addAll(valueDesc, gamemodeSelector, valueBox);
	}
	
    public static Map<String, Object> getGamemodeData(GameMode gamemode) {
    	Map<String, Object> data = new HashMap();
    	if (gamemode== GameMode.TIME_MODE) {
    		data.put("desc", "Minutes");
    		data.put("min", 5);
    		data.put("max", 30);
    		data.put("default", 10);
    	}
    	else if(gamemode== GameMode.WEALTH_MODE) {
    		data.put("desc", "Money Target");
    		data.put("min", 50);
    		data.put("max", 1000);
    		data.put("default", 100);
    	}
    	else {
    		System.out.println(gamemode + " gamemode does not exist");
    		return null;
    	}
    	
    	return data;
    }
    
    public double getValue() {
    	double val = Integer.parseInt(valueLabel.getText());
    	final int SECONDS_PER_MIN = 60;
    	if (this.getGameMode()== GameMode.TIME_MODE){
    		return val*SECONDS_PER_MIN;
    	}
    	else {
    		return val;
    	}
    }
    
    public GameMode getGameMode() {
    	if(gamemodeSelector.getValue().equals(GameMode.TIME_MODE.toString())) {
    		return GameMode.TIME_MODE;
    	}
    	else if(gamemodeSelector.getValue().equals(GameMode.WEALTH_MODE.toString())) {
    		return GameMode.WEALTH_MODE;
    	}
    	else {
    		return null;
    	}
    }

}
