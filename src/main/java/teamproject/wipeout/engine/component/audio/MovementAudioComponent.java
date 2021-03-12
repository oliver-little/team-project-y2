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

import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;
import teamproject.wipeout.util.resources.ResourceLoader;
import teamproject.wipeout.util.resources.ResourceType;

public class MovementAudioComponent implements GameComponent{
	
	public MovementComponent moveComp;
	public double volume;
	private AudioInputStream audioStream;
	public boolean playing;
	Clip audioClip;
	
	public MovementAudioComponent(MovementComponent m, String fileName) {
		this.moveComp = m;
		this.volume = 1.0f;
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
		} //file read each time to allow for the same sounds to overlap - possibly inefficient
		
	}
	
	public void playSound() {
		
		audioClip.start();
		audioClip.loop(Clip.LOOP_CONTINUOUSLY);
		FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
		float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
		gainControl.setValue(dB);
		playing = true;
		
	}
	
	public void stop() {
		audioClip.stop();
		audioClip.setMicrosecondPosition(0);
		playing = false;
	}
	
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
	
	public String getType()
	{
		return "audio";
	}
	
}