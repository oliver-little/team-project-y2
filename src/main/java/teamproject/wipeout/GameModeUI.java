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
	private ComboBox<String> gamemodeBox;
	
	public GameModeUI() {
		this.setAlignment(Pos.CENTER);
		
        Label valueDesc = new Label();
        
        HBox valueBox = new HBox();
        valueBox.getStyleClass().add("hbox");
        valueBox.setSpacing(3);
        valueBox.setAlignment(Pos.CENTER);
        
        valueLabel = new Label();
        
        gamemodeBox = new ComboBox<String>(FXCollections.observableArrayList("Time Mode","Wealth Mode"));
        gamemodeBox.setOnAction((event) -> {
        	Map<String, Object> gamemodeData = getGamemodeData((String) gamemodeBox.getValue());
        	valueLabel.setText(Integer.toString((int) gamemodeData.get("default")));
        	valueDesc.setText((String) gamemodeData.get("desc")+ ":");
        });
        gamemodeBox.getSelectionModel().selectFirst();
        // trigger event to set value label
        Event.fireEvent(gamemodeBox, new ActionEvent());
        
        
        final int interval = 5;
        
        Button decrementButton = new Button("-");
        decrementButton.getStyleClass().add("small-button");
        decrementButton.setOnAction((event) ->{
        	int value = Integer.parseInt(valueLabel.getText());
        	Map<String, Object> gamemodeData = getGamemodeData((String) gamemodeBox.getValue());
        	if (value-interval>=((int)gamemodeData.get("min"))) {
        		valueLabel.setText(Integer.toString(value-interval));
        	}
        	
        });
        
        Button incrementButton = new Button("+");
        incrementButton.getStyleClass().add("small-button");        
        incrementButton.setOnAction((event) ->{
        	int value = Integer.parseInt(valueLabel.getText());
        	Map<String, Object> gamemodeData = getGamemodeData((String) gamemodeBox.getValue());
        	if (value+interval<=((int) gamemodeData.get("max"))) {
        		valueLabel.setText(Integer.toString(value+interval));
        	}
        	
        });
        
        valueBox.getChildren().addAll(decrementButton, valueLabel, incrementButton);
        this.getChildren().addAll(valueDesc, gamemodeBox, valueBox);
	}
	
    public static Map<String, Object> getGamemodeData(String gamemode) {
    	Map<String, Object> data = new HashMap();
    	if (gamemode.equals("Time Mode")) {
    		data.put("desc", "Minutes");
    		data.put("min", 5);
    		data.put("max", 30);
    		data.put("default", 10);
    	}
    	else if(gamemode.equals("Wealth Mode")) {
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
    
    public int getValue() {
    	return Integer.parseInt(valueLabel.getText());
    }
    
    public String getGamemode() {
    	return gamemodeBox.getValue();
    }

}
