package teamproject.wipeout.engine.system;

import java.util.Set;
import java.util.List;

import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class AudioSystem implements GameSystem {

	protected SignatureEntityCollector _entityCollector;
	private double spotEffectsVolume;
	
	/**
	 * System which dictates which sounds to play
	 * @param e  Scene which is searched for entities with AudioComponents
	 */
    public AudioSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(AudioComponent.class)); //collects entities with AudioComponents
        this.spotEffectsVolume = 1.0; //initialised to full volume
    }

	/**
	 * Removes AudioComponent observer and stops all AudioComponents.
	 */
	public void cleanup() {
		this._entityCollector.cleanup();
	}

	/**
     * Checks, for each frame, whether any AudioComponents need playing.
     */
	public void accept(Double timeStep) {
		if (spotEffectsVolume == 0.0) { //do nothing if volume is muted
			return;
		}

		List<GameEntity> entities = this._entityCollector.getEntities();
		for (GameEntity entity : entities) { //iterates through all entities with AudioComponents
			AudioComponent s = entity.getComponent(AudioComponent.class);
			if (s.toPlay()) {
				s.setVolume(spotEffectsVolume); //applies the spot effects volume to component before playing
				s.playSound();
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
}