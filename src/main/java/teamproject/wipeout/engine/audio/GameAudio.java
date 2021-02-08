package teamproject.wipeout.engine.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Used for the game's background music.
 */
public class GameAudio {
	
    private MediaPlayer player;
    private Boolean playing;
    
    /**
     * This is a class used to implement the backing track (music).
     * @param audioFileName	name of the audio file
     */
    public GameAudio(String audioFileName) {
    	String filePath = this.getClass().getClassLoader().getResource("audio/" + audioFileName).toString(); // TODO: Handle null value -> throw exception

    	Media media = new Media(filePath);
    	this.player = new MediaPlayer(media);
    	player.setOnEndOfMedia(new Runnable() { //ensures song loops continuously
    		public void run() {
    			player.seek(Duration.ZERO); 
    		}
    	});
    	playing = false;
    }
    
    public void play() {
    	player.play();
    	playing = true;
    }
    
    /**
     * Method to switch between playing and pausing
     */
    public void playPause() {
	    if(playing) {	
    		player.pause();
	    	playing = false;
	    }else {
	    	player.play();
	    	playing = true;
	    }
    }
    
    public void stop() {
    	player.stop();
    	playing = false;
    }
    
    /**
     * Method to set the volume.
     * @param volume  double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public void setVolume(double volume) {
    	player.setVolume(volume);
    }
    
    /**
     * Method to get the volume.
     * @return a double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public double getVolume() {
    	return player.getVolume();
    }
    
    public boolean isPlaying() {
    	return playing;
    }
}
