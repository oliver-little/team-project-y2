package teamproject.wipeout.sound;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;  

/**
 * Used for the game's background music.
 */
public class GameSound {
	
	//private Media media;
    private MediaPlayer player;
    private Boolean playing;
    
    /**
     * This is a class used to implement the backing track (music).
     * @param path  location of track/music file
     */
    public GameSound(String path) {
    	Media media = new Media(new File(path).toURI().toString());
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
