package teamproject.wipeout.engine.component.audio;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

public class AudioComponent implements GameComponent {

	private Boolean play; //set to True when the sound needs to be played
    private String fileName;
    private AudioInputStream audioStream;
    private double volume;
	
	/**
	 * This is a component class for adding sound effects to entities.
	 * The AudioSystem checks the boolean attribute {@link #play} to decide if the sound needs to play.
	 * @param audioFileName	name of the audio file inside /resources/audio/
	 */
	public AudioComponent(String audioFileName) {
		fileName = audioFileName;
    	//volume = 1.0f;
    	play = false;
	}
	   
	public String getType()
	{
		return "audio";
	}
	
	/**
	 * called by the AudioSystem to play sound.
	 */
	public void playSound() {
		try {
			File audioFile = ResourceLoader.get(ResourceType.AUDIO, fileName); //file read each time to allow for the same sounds to overlap - possibly inefficient
    		audioStream = AudioSystem.getAudioInputStream(audioFile); 
    		AudioFormat format = audioStream.getFormat();			  
    		DataLine.Info info = new DataLine.Info(Clip.class, format);
			Clip audioClip = (Clip) AudioSystem.getLine(info);
			audioClip.open(audioStream);
			audioClip.start();
			FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(20f * (float) Math.log10(volume)); //converts volume to decibels
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	play = false;
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
     * @param volume double value between 0.0 (inaudible) and 1.0 (full volume).
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
