package teamproject.wipeout.engine.component.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import teamproject.wipeout.engine.component.GameComponent;

public final class AudioComponent implements GameComponent {

	private Boolean play; //set to True when the sound needs to be played
	private final Media media;
	private double volume;
	
	/**
	 * This is a component class for adding sound effects to entities.
	 * The AudioSystem checks the boolean attribute {@link #play} to decide if the sound needs to play.
	 * @param audioFileName	name of the audio file
	 */
	public AudioComponent(String audioFileName) {
		String filePath = this.getClass().getClassLoader().getResource("audio/" + audioFileName).toString(); // TODO: Handle null value -> throw exception

		this.media = new Media(filePath);
		this.play = false;
		this.volume = 1.0; //initially set to full volume
	}
	   
	public String getType()
	{
		return "audio";
	}
	
	/**
	 * called by the AudioSystem to create a MediaPlayer and play sound.
	 */
	public void playSound() {
		MediaPlayer player = new MediaPlayer(media); // TODO: Maybe have only one MediaPlayer per AudioComponent -> if we need better performance later
		player.setVolume(volume);
		player.play();
		play = false; //switched back to false so the sound only plays once.
	}
	
	/**
	 * called by any class when it wants the sound to play.
	 */
	public void play() {
		play = true; //sets the Boolean attribute to true which SoundSystem will pick up on.
	}
	
	public boolean toPlay() {
		return play; //used to see if the sound needs to be played.
	}
	
	/**
     * Method to set the volume, done by AudioSystem.
     * @param volume  double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public void setVolume(double volume) {
    	this.volume = volume;
    }
    
    /**
     * Method to get the volume.
     * @return a double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public double getVolume() {
    	return volume;
    }
}
