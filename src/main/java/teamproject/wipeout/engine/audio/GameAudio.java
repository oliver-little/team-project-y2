package teamproject.wipeout.engine.audio;

import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

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

/**
 * Used for the game's background music.
 */
public class GameAudio {
	
	//private MediaPlayer player;
    private Boolean playing;
    private String fileName;
    private Clip audioClip;
    private AudioInputStream audioStream;
    public double volume;
    public boolean toLoop; //whether or not to loop sound continuosly (until stop)
    public boolean muted;
    
    /**
     * This is a class used to implement one-off sounds and the backing track.
     * @param audioFileName	name of the audio file inside /resources/audio/
     * @param loop - whether or not the sound effect needs to loop continously
     */
    public GameAudio(String audioFileName, boolean loop) {
    	fileName = audioFileName;
    	this.volume = 0.05f; //initialised to 5% volume
    	playing = false;
    	this.toLoop = loop;
    	this.muted = false;
    	File audioFile;
		try
		{
			audioFile = ResourceLoader.get(ResourceType.AUDIO, fileName);
			audioStream = AudioSystem.getAudioInputStream(audioFile);
			AudioFormat format = audioStream.getFormat();
	    	DataLine.Info info = new DataLine.Info(Clip.class, format);
	    	audioClip = (Clip) AudioSystem.getLine(info);
			audioClip.open(audioStream);
		}
		catch (IOException | UnsupportedAudioFileException | LineUnavailableException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	
    }
    
    public void play() {
    	if (muted) {
    		return;
		}

    	audioClip.start();
    	if(toLoop) {
    		audioClip.loop(Clip.LOOP_CONTINUOUSLY);
    	}
    	playing = true;
    	setVolume(volume);
    	
    }
    
	 /**
	 * Method to switch between playing and stopping
	 */
    public void stopStart() {
	    if(playing) {	
	    	stop();
	    	playing = false;
	    } else if (!muted) {
	    	play();
	    	playing = true;
	    }
    }
    
    public void stop() {
    	audioClip.stop();
		audioClip.setMicrosecondPosition(0);
		playing = false;
    }
    
    /**
     * Method to set the volume.
     * @param volume  double value between 0.0 (inaudible) and 1.0 (full volume).
     */
    public void setVolume(double volume) {
    	if(volume < 0) {
    		volume = 0;
    	}else if(volume > 1) {
    		volume = 1.0f;
    	}
    	if(playing) {
	    	FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
	        gainControl.setValue(20f * (float) Math.log10(volume)); //converts volume to decibels
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

	public void mute(){
		audioClip.stop();
		muted = true;
	}

	public void unmute(){
		this.play();
		muted = false;
	}
    
    public void muteUnmute() {
		if(muted) {
			muted = false;
			this.setVolume(0.05f);
			// Restart the clip as if it was stopped
			if (this.playing) {
				audioClip.start();
			}
		}else {
			muted = true;
			this.setVolume(0f);
			// Also stop the clip playing so sound cuts immediately
			audioClip.stop();
		}
	}
}
