package teamproject.wipeout.engine.component.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.media.jfxmedia.AudioClip;

import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

public class MovementAudioComponent implements GameComponent{
	
	public MovementComponent moveComp;
	public boolean playing;
	GameAudio audio;
	
	/**
	 * 
	 * @param m - movement component to get velocity from
	 * @param fileName - name of audio file to play when moving
	 */
	public MovementAudioComponent(MovementComponent m, String fileName) {
		this.moveComp = m;
		
		audio = new GameAudio(fileName, true);
		audio.setVolume(0.1f);
	}
	
	public void playSound() {
		audio.play();
		playing = true;
		
	}
	
	public void stop() {
		audio.stop();
		playing = false;
	}
	
	public void setVolume(double volume) {
    	audio.setVolume(volume);
    }
    
    /**
     * Method to get the volume.
     * @return a double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public double getVolume() {
    	return audio.getVolume();
    }
	
	public String getType()
	{
		return "audio";
	}
	
}