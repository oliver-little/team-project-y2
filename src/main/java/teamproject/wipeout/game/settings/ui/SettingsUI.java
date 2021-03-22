package teamproject.wipeout.game.settings.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.system.audio.AudioSystem;
import teamproject.wipeout.engine.system.audio.MovementAudioSystem;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

public class SettingsUI extends VBox{
    
    private MovementAudioSystem mas;
    private AudioSystem as;
    private GameAudio backingTrack;
    private ScrollPane scrollPane = new ScrollPane();
    private boolean opened = false;
    private Button openCloseButton = new Button();

    public SettingsUI(AudioSystem audioSys, MovementAudioSystem mas, GameAudio backingTrack){
        super();

        this.mas = mas;
        this.as = audioSys;
        this.backingTrack = backingTrack;
        scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        this.getStylesheets().add(ResourceType.STYLESHEET.path + "settings-ui.css");
        try {
            InputStream path = new FileInputStream(ResourceLoader.get(ResourceType.STYLESHEET, "fonts/Kalam-Regular.ttf"));
            Font.loadFont(path, 12);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        VBox box = new VBox();

        HBox buttonBox = new HBox();
        
        ImageView cog = null;
        try{
            InputStream s = new FileInputStream(ResourceLoader.get(ResourceType.UI, "cog.png"));
            Image img = new Image(s);
            cog = new ImageView(img);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        openCloseButton.setAlignment(Pos.BASELINE_RIGHT);
        openCloseButton.setGraphic(cog);
        openCloseButton.setOnAction(e -> {
            opened = !opened;
            this.setMenuVisible(opened);
            
        });
        buttonBox.getChildren().add(openCloseButton);
        buttonBox.setAlignment(Pos.BASELINE_RIGHT);
        
        this.getChildren().add(buttonBox);

        box.setSpacing(7);

        HBox backingBox = new HBox();
        Slider backingSlider = new Slider();
        backingSlider.setValue(5);
        backingSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(
            ObservableValue<? extends Number> observableValue, 
            Number oldValue, 
            Number newValue) { 
                backingTrack.setVolume((double) newValue/100.0f);
            }
        });

        CheckBox muteMusic = new CheckBox("Mute");
        muteMusic.selectedProperty().addListener(
            (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                if(new_val){
                    backingTrack.mute();
                    backingSlider.setDisable(true);
                }else{
                    backingTrack.unmute();
                    backingSlider.setDisable(false);
                }
        });

        /// MUSIC IS SET TO MUTE BY DEFAULT HERE
        muteMusic.setSelected(true);
        backingTrack.mute();
        backingSlider.setDisable(true);
        ///

        muteMusic.addEventFilter(KeyEvent.ANY, KeyEvent::consume);
        backingBox.getChildren().addAll(backingSlider, muteMusic);
        
        
        HBox effectsBox = new HBox();
        Slider effectsSlider = new Slider();
        effectsSlider.setValue(as.getSpotEffectsVolume() * 100);
        effectsSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(
            ObservableValue<? extends Number> observableValue, 
            Number oldValue, 
            Number newValue) { 
                mas.setVolume((double) newValue/100.0f);
                as.setSpotEffectsVolume((double) newValue/100.0f);
            }
        });
        CheckBox muteEffects = new CheckBox("Mute");
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
        
        
        Label title1 = new Label("Music Volume");
        Label title2 = new Label("Effects Volume");
        box.getChildren().addAll(title1, backingBox, title2, effectsBox);
        scrollPane.setContent(box);
        this.getChildren().add(scrollPane);
        setMenuVisible(false);
    }

    private void setMenuVisible(boolean visible){
        KeyValue goalWidth = null;
        if (visible) {
            Rectangle clipRect = new Rectangle(0, 0, scrollPane.getWidth(), 0);
            scrollPane.setClip(clipRect);
            goalWidth = new KeyValue(clipRect.heightProperty(), scrollPane.getHeight(), Interpolator.EASE_OUT);
        }
        else {
            Rectangle clipRect = new Rectangle(0, 0, scrollPane.getWidth(), scrollPane.getHeight());
            scrollPane.setClip(clipRect);
            goalWidth = new KeyValue(clipRect.heightProperty(), 0, Interpolator.EASE_IN);
        }

        KeyFrame frame = new KeyFrame(Duration.seconds(0.25), goalWidth);
        Timeline timeline = new Timeline(frame);
        timeline.play();
    }
    
}
