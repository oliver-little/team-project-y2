package teamproject.wipeout.engine.audio;

import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

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

/**
 * Used for the game's background music.
 */
public class GameAudio {
	
	//private MediaPlayer player;
    private Boolean playing;
    private String fileName;
    Clip audioClip;
    AudioInputStream audioStream;
    double volume;
    
    /**
     * This is a class used to implement the backing track (music).
     * @param audioFileName	name of the audio file inside /resources/audio/
     */
    public GameAudio(String audioFileName) {
    	fileName = audioFileName;
    	volume = 1.0f;
    	playing = false;
    	
    }
    
    public void play() {
    	try
		{
    		File audioFile = ResourceLoader.get(ResourceType.AUDIO, fileName);
    		audioStream = AudioSystem.getAudioInputStream(audioFile);
			AudioFormat format = audioStream.getFormat();
	    	DataLine.Info info = new DataLine.Info(Clip.class, format);
	    	audioClip = (Clip) AudioSystem.getLine(info);
			audioClip.open(audioStream);
	    	audioClip.start();
	    	audioClip.loop(Clip.LOOP_CONTINUOUSLY);
	    	setVolume(volume);
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	playing = true;
    	
    }
    
	 /**
	 * Method to switch between playing and stopping
	 */
    public void stopStart() {
	    if(playing) {	
	    	stop();
	    	playing = false;
	    }else {
	    	play();
	    	playing = true;
	    }
    }
    
    public void stop() {
    	audioClip.close();
    	
    	try
		{
			audioStream.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	playing = false;
    }
    
    /**
     * Method to set the volume.
     * @param volume  double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public void setVolume(double volume) {
    	if(playing) {
	    	FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
	        float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
	        gainControl.setValue(dB);
    	}
    	this.volume = volume;
    }
    
    /**
     * Method to get the volume.
     * @return a double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public double getVolume() {
    	return volume;
    	
    }
    
    public boolean isPlaying() {
    	return playing;
    }
}
