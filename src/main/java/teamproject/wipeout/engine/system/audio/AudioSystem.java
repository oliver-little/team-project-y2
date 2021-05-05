package teamproject.wipeout.engine.system.audio;

import java.util.Set;
import java.util.List;

import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

/**
 * Class which represents a GameSystem for all audio effects.
 */
public class AudioSystem implements GameSystem {

	protected SignatureEntityCollector entityCollector;
	private double spotEffectsVolume;
	private double previousVolume = 0.05f;
	public boolean muted;
	
	/**
	 * System which dictates which sounds to play
	 * @param e  Scene which is searched for entities with AudioComponents
	 */
    public AudioSystem(GameScene e, double volume) {
        this.entityCollector = new SignatureEntityCollector(e, Set.of(AudioComponent.class)); //collects entities with AudioComponents
        this.spotEffectsVolume = volume; //initialised to full volume
        this.muted = false;
    }

	/**
	 * Removes AudioComponent observer and stops all AudioComponents.
	 */
	public void cleanup() {
		this.mute();
		this.entityCollector.cleanup();
	}

	/**
     * Checks, for each frame, whether any AudioComponents need playing.
     */
	public void accept(Double timeStep) {
		if (spotEffectsVolume == 0.0) { //do nothing if volume is muted
			return;
		}

		List<GameEntity> entities = this.entityCollector.getEntities();
		for (GameEntity entity : entities) { //iterates through all entities with AudioComponents
			AudioComponent s = entity.getComponent(AudioComponent.class);
			for(String key : s.sounds.keySet()){
				if(s.sounds.get(key)){
					s.setVolume(spotEffectsVolume);
					s.playSound(key);
				}
			}
		}
	}
	
	/**
	 * sets the volume for all AudioComponent spot effects.
	 * @param volume double value between 0.0 (inaudible) and 1.0 (full volume).
	 */
	public void setSpotEffectsVolume(double volume) {
		spotEffectsVolume = volume;
	}
	
	/**
	 * returns the volume for all AudioComponent spot effects.
	 * @return  a double value between 0.0 (inaudible) and 1.0 (full volume).
	 */
	public double getSpotEffectsVolume() {
		return spotEffectsVolume;
	}
	
	public void muteUnmute() {
		if(muted) {
			unmute();
		}else {
			mute();
		}
	}

	/**
	 * method to change the state to muted and stop all sounds being played currently.
	 */
	public void mute(){
		muted = true;
		this.previousVolume = this.spotEffectsVolume;
		this.setSpotEffectsVolume(0.0f);

		List<GameEntity> entities = this.entityCollector.getEntities();
		for (GameEntity entity: entities) {
			entity.getComponent(AudioComponent.class).setVolume(0.0f);
			//this stops sounds being 'queued' while the system is muted
		}
	}

	/**
	 * sets state to unmuted, and sets volume all of entities to before muted volume.
	 */
	public void unmute(){
		muted = false;
		this.setSpotEffectsVolume(this.previousVolume);
		List<GameEntity> entities = this.entityCollector.getEntities();
		for (GameEntity entity: entities) {
			entity.getComponent(AudioComponent.class).setVolume(this.previousVolume);
			//alerts components that sounds can be added again
		}
	}
	
}