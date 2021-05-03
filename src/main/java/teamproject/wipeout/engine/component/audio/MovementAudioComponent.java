package teamproject.wipeout.engine.component.audio;

import teamproject.wipeout.engine.audio.GameAudio;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.component.physics.MovementComponent;

/**
 * GameComponent which represents audio effects related to an entity's movement
 */
public class MovementAudioComponent implements GameComponent{
	
	public MovementComponent moveComp;
	public boolean playing;
	GameAudio audio;
	
	/**
	 * constructor
	 * @param m - movement component to get velocity from
	 * @param fileName - name of audio file to play when moving
	 */
	public MovementAudioComponent(MovementComponent m, String fileName) {
		this.moveComp = m;
		audio = new GameAudio(fileName, true);
	}
	
	/**
	 * called to play sound
	 */
	public void playSound() {
		audio.play();
		playing = true;
		
	}
	
	/**
	 * called to stop sound
	 */
	public void stop() {
		audio.stop();
		playing = false;
	}
	
	/**
	 * called to set the volume
	 * @param volume - volume to set effect to
	 */
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
	
	/**
	 * gets the component type
	 * @return string of component type
	 */
	public String getType()
	{
		return "audio";
	}
	
}