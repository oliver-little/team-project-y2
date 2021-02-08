package teamproject.wipeout.engine.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Used for the game's background music.
 */
public final class GameAudio {
	
    private final MediaPlayer player;
    private Boolean _playing;
    
    /**
     * This is a class used to implement the backing track (music).
     * @param audioFileName	name of the audio file
     */
    public GameAudio(String audioFileName) {
    	String filePath = this.getClass().getClassLoader().getResource("audio/" + audioFileName).toString(); // TODO: Handle null value -> throw exception

    	Media media = new Media(filePath);
    	this.player = new MediaPlayer(media);
    	this.player.setOnEndOfMedia(() -> player.seek(Duration.ZERO)); //ensures the track loops continuously
    	this._playing = false;
    }
    
    public void play() {
    	player.play();
    	_playing = true;
    }
    
    /**
     * Method to switch between _playing and pausing
     */
    public void playPause() {
	    if(_playing) {
    		player.pause();
	    	_playing = false;
	    }else {
	    	player.play();
	    	_playing = true;
	    }
    }
    
    public void stop() {
    	player.stop();
    	_playing = false;
    }
    
    /**
     * Method to set the volume.
     * @param volume double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public void setVolume(double volume) {
    	if (volume == 0.0) {
    		player.setMute(true);
		} else {
    		if (player.isMute()) {
				player.setMute(false);
			}
			player.setVolume(volume);
		}
    }
    
    /**
     * Method to get the volume.
     * @return a double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public double getVolume() {
    	return player.getVolume();
    }
    
    public boolean isPlaying() {
    	return _playing;
    }
}
