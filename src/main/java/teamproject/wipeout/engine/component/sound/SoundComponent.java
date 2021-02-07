package teamproject.wipeout.engine.component.sound;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

import teamproject.wipeout.engine.component.GameComponent;

public class SoundComponent implements GameComponent{

	private Boolean _play; //set to True when the sound needs to be played
	private Media media;
	
	/**
	 * This is a component class for adding sound effects to entities.
	 * The SoundSystem checks the boolean attribute {@link #_play} to decide if the sound needs to play.
	 * @param path  location of the sound file
	 */
	public SoundComponent(String path) {
		this.media = new Media(new File(path).toURI().toString());
		this._play = false;
	}
	   
	public String getType()
	{
		return "sound";
	}
	
	/**
	 * called by the SoundSystem to create a MediaPlayer and play sound.
	 */
	public void playSound() {
		MediaPlayer player = new MediaPlayer(media);
		player.play();
		_play = false; //switched back to false so the sound only plays once.
	}
	
	/**
	 * called by any class when it wants the sound to play.
	 */
	public void play() {
		_play = true; //sets the Boolean attribute to true which SoundSystem will pick up on.
	}
	
	public boolean toPlay() {
		return _play; //used to see if the sound needs to be played.
	}
}
