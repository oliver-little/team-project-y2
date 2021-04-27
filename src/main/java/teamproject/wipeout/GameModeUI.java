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
        
        gamemodeSelector = new ComboBox<String>(FXCollections.observableArrayList("Time Mode","Wealth Mode"));
        gamemodeSelector.setOnAction((event) -> {
        	Map<String, Object> gamemodeData = getGamemodeData(this.getGamemode());
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
        	Map<String, Object> gamemodeData = getGamemodeData(this.getGamemode());
        	if (value-interval>=((int)gamemodeData.get("min"))) {
        		valueLabel.setText(Integer.toString(value-interval));
        	}
        	
        });
        
        Button incrementButton = new Button("+");
        incrementButton.getStyleClass().add("small-button");        
        incrementButton.setOnAction((event) ->{
        	int value = Integer.parseInt(valueLabel.getText());
        	Map<String, Object> gamemodeData = getGamemodeData(this.getGamemode());
        	if (value+interval<=((int) gamemodeData.get("max"))) {
        		valueLabel.setText(Integer.toString(value+interval));
        	}
        	
        });
        
        valueBox.getChildren().addAll(decrementButton, valueLabel, incrementButton);
        this.getChildren().addAll(valueDesc, gamemodeSelector, valueBox);
	}
	
    public static Map<String, Object> getGamemodeData(Gamemode gamemode) {
    	Map<String, Object> data = new HashMap();
    	if (gamemode==Gamemode.TIME_MODE) {
    		data.put("desc", "Minutes");
    		data.put("min", 5);
    		data.put("max", 30);
    		data.put("default", 10);
    	}
    	else if(gamemode==Gamemode.WEALTH_MODE) {
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
    	if (this.getGamemode()==Gamemode.TIME_MODE){
    		return val*SECONDS_PER_MIN;
    	}
    	else {
    		return val;
    	}
    }
    
    public Gamemode getGamemode() {
    	if(gamemodeSelector.getValue().equals(Gamemode.TIME_MODE.toString())) {
    		return Gamemode.TIME_MODE;
    	}
    	else if(gamemodeSelector.getValue().equals(Gamemode.WEALTH_MODE.toString())) {
    		return Gamemode.WEALTH_MODE;
    	}
    	else {
    		return null;
    	}
    }

}
