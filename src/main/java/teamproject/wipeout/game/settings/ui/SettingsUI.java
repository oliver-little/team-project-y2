package teamproject.wipeout.game.settings.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.system.audio.AudioSystem;
import teamproject.wipeout.engine.system.audio.MovementAudioSystem;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;


public class SettingsUI extends VBox {
    
    private MovementAudioSystem mas;
    private AudioSystem as;
    private GameAudio backingTrack;
    private boolean openedSettings = false;
    private boolean openedInstructions = false;
    private Button openCloseButtonSettings = new Button();
    private Button openCloseButtonInstructions = new Button();

    private Pane menuRegion;
    private VBox settingsBox;
    private VBox instructionsBox;

    /**
     * Creates the UI for the in-game settings menu
     * @param audioSys - audio system for volume changes
     * @param mas - movement-audio system for volume changes
     * @param doReturnToMenu - called when the return to menu button is clicked
     * @param keyBindings - a Map of the keybindings for the current game
     * @param backingTrack - GameAudio object for backing track volume
     */
    public SettingsUI(AudioSystem audioSys, MovementAudioSystem mas, Runnable doReturnToMenu, Map<String, KeyCode> keyBindings, GameAudio backingTrack){
        super();

        this.mas = mas;
        this.as = audioSys;
        this.backingTrack = backingTrack;
        this.getStylesheets().add(ResourceType.STYLESHEET.path + "settings-ui.css");
        try {
            InputStream path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
            Font.loadFont(path, 12);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        
        menuRegion = new Pane();
        menuRegion.getStyleClass().add("pane");
        settingsBox = new VBox();

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

        ImageView instructions = null;
        try{
            InputStream s = new FileInputStream(ResourceLoader.get(ResourceType.UI, "instructions.png"));
            Image tmpImg = new Image(s);
            instructions = new ImageView(tmpImg);
            instructions.setSmooth(false);

        } catch(Exception e){
            e.printStackTrace();
        }

        openCloseButtonSettings.setGraphic(cog);
        openCloseButtonSettings.setOnAction(e -> {
            openedSettings = !openedSettings;
            if(openedSettings) {
                openedInstructions = false;
            }
            this.setMenuVisible(openedSettings, settingsBox);
        });

        openCloseButtonInstructions.setGraphic(instructions);
        openCloseButtonInstructions.setOnAction(e -> {
            openedInstructions = !openedInstructions;
            if(openedInstructions) {
                openedSettings = false;
            }
            this.setMenuVisible(openedInstructions, instructionsBox);
        });

        buttonBox.getChildren().addAll(openCloseButtonSettings, openCloseButtonInstructions);
        buttonBox.setAlignment(Pos.TOP_RIGHT);

        settingsBox.setSpacing(7);

        HBox backingBox = new HBox();
        Slider backingSlider = new Slider(); //slider for music volume
        backingSlider.setValue(5); //default set to volume 5
        backingSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) { 
                backingTrack.setVolume((double) newValue/100.0f);
            }
        });
        CheckBox muteMusic = new CheckBox("Mute"); //checkbox for muting music
        muteMusic.selectedProperty().addListener(
            (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                if(new_val) {
                    backingTrack.mute();
                    backingSlider.setDisable(true);
                }
                else {
                    backingTrack.unmute();
                    backingSlider.setDisable(false);
                }
        });

        /// MUSIC IS SET TO MUTE BY DEFAULT HERE
        muteMusic.setSelected(true);
        backingSlider.setDisable(true);

        muteMusic.addEventFilter(KeyEvent.ANY, KeyEvent::consume);
        backingBox.getChildren().addAll(backingSlider, muteMusic);
        
        HBox effectsBox = new HBox();
        Slider effectsSlider = new Slider(); //slider for effects volume
        effectsSlider.setValue(as.getSpotEffectsVolume() * 100);
        effectsSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) { 
                mas.setVolume((double) newValue/100.0f);
                as.setSpotEffectsVolume((double) newValue/100.0f);
            }
        });
        CheckBox muteEffects = new CheckBox("Mute"); //checkbox for muting effects volume
        muteEffects.selectedProperty().addListener(
            (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                if(new_val){
                    as.mute();
                    mas.mute();
                    effectsSlider.setDisable(true);
                }else{
                    as.unmute();
                    mas.unmute();
                    effectsSlider.setDisable(false);
                }
        });
        muteEffects.addEventFilter(KeyEvent.ANY, KeyEvent::consume);
        effectsBox.getChildren().addAll(effectsSlider, muteEffects);

        effectsSlider.addEventFilter(KeyEvent.ANY, KeyEvent::consume);
        backingSlider.addEventFilter(KeyEvent.ANY, KeyEvent::consume);
        

        Button closeButton = new Button("Return to Menu");
        closeButton.setId("close");
        closeButton.setOnAction((e) ->  {doReturnToMenu.run();});
        HBox closeButtonContainer = new HBox();
        closeButtonContainer.getChildren().addAll(closeButton);
        closeButtonContainer.setAlignment(Pos.CENTER);

        Label title1 = new Label("Music Volume");
        title1.getStyleClass().add("settings-label");
        Label title2 = new Label("Effects Volume");
        title2.getStyleClass().add("settings-label");
        settingsBox.getChildren().addAll(title1, backingBox, title2, effectsBox, closeButtonContainer);
        settingsBox.setPadding(new Insets(0, 5, 15, 5));

        instructionsBox = new VBox();

        Label toMove = new Label("To move use: " + keyBindings.get("Move left") + ", " + keyBindings.get("Move right") + ", " +
                keyBindings.get("Move up")  + ", " + keyBindings.get("Move down"));

        Label toPlantItem = new Label("To plant: click on the inventory item, \n then click on an empty slot ");

        Label toHarvestItem = new Label("To harvest use: " + keyBindings.get("Harvest"));

        Label toCollectItem = new Label("To collect use: " + keyBindings.get("Pick-up"));

        Label toDropItem = new Label("To drop use: " + keyBindings.get("Drop"));

        Label toDestroyItem = new Label("To destroy use: " + keyBindings.get("Destroy"));

        instructionsBox.setSpacing(0);
        instructionsBox.setPadding(new Insets(0, 5, 5, 5));
        instructionsBox.getChildren().addAll(toMove, toPlantItem, toHarvestItem, toCollectItem, toDropItem, toDestroyItem);

        this.getChildren().addAll(buttonBox, menuRegion);
        // Add both boxes initially so they are rendered correctly and width and height properties work properly
        menuRegion.getChildren().addAll(settingsBox, instructionsBox);
        menuRegion.setClip(new Rectangle(0, 0, 0, 0));
    }

    /**
     * Shows/hides the menu using animation
     * @param visible
     */
    private void setMenuVisible(boolean visible, VBox menu){
        menuRegion.getChildren().clear();
        menuRegion.getChildren().add(menu);

        KeyValue goalWidth = null;
        if (visible) {
            Rectangle clipRect = new Rectangle(0, 0, menu.getWidth() + 5, 0);
            menuRegion.setClip(clipRect);
            goalWidth = new KeyValue(clipRect.heightProperty(), menu.getHeight() + 5, Interpolator.EASE_OUT);
        }
        else {
            Rectangle clipRect = new Rectangle(0, 0, menu.getWidth(), menu.getHeight());
            menuRegion.setClip(clipRect);
            goalWidth = new KeyValue(clipRect.heightProperty(), 0, Interpolator.EASE_IN);
        }

        KeyFrame frame = new KeyFrame(Duration.seconds(0.25), goalWidth);
        Timeline timeline = new Timeline(frame);
        timeline.play();
    }
}
