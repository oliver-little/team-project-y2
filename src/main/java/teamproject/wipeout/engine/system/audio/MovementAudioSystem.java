package teamproject.wipeout.engine.system.audio;

import java.util.Set;
import java.util.List;

import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.component.audio.MovementAudioComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;
import teamproject.wipeout.engine.system.GameSystem;

/**
 * Class which represents the GameSystem to add a sound effect to an entity's movement (if it has a MovementComponent)
 */
public class MovementAudioSystem implements GameSystem {

	protected SignatureEntityCollector entityCollector;
	private double volume;
	private double previousVolume = 0.05f;
	public boolean muted;
	
	/**
	 * System which dictates which sounds to play
	 * @param e  Scene which is searched for entities with MovementAudioComponents
	 */
    public MovementAudioSystem(GameScene e, double volume) {
        this.entityCollector = new SignatureEntityCollector(e, Set.of(MovementAudioComponent.class)); //collects entities with AudioComponents
        this.volume = volume;
        this.muted = false;
    }

	/**
	 * Removes MovementAudioComponent observer and stops all MovementAudioComponents.
	 */
	public void cleanup() {
		this.entityCollector.cleanup();
	}

	/**
     * Checks, for each frame, whether any MovementAudioComponents need playing if velocity is non-zero.
     */
	public void accept(Double timeStep) {
		if (volume == 0.0) { //do nothing if volume is muted
			return;
		}

		List<GameEntity> entities = this.entityCollector.getEntities();
		for (GameEntity entity : entities) { //iterates through all entities with AudioComponents
			MovementAudioComponent s = entity.getComponent(MovementAudioComponent.class);
			if ((s.playing == false) && (s.moveComp.velocity.getX() != 0.0f || s.moveComp.velocity.getY() != 0.0f)) {
				s.setVolume(volume); //applies the spot effects volume to component before playing
				s.playSound();
			} else if (s.playing == true && muted || (s.playing == true) && (s.moveComp.velocity.getX() == 0.0f && s.moveComp.velocity.getY() == 0.0f)) {
				s.stop();
			}
		}
	}
	
	/**
	 * sets the volume for all MovementAudioComponents.
	 * @param volume - double value between 0.0 (inaudible) and 1.0 (full volume).
	 */
	public void setVolume(double volume) {
		this.volume = volume;
	}
	
	/**
	 * returns the volume for all MovementAudioComponents.
	 * @return a double value between 0.0 (inaudible) and 1.0 (full volume).
	 */
	public double getVolume() {
		return volume;
	}
	
	/**
	 * method to flip between muted and unmuted
	 */
	public void muteUnmute() {
		if(muted) {
			unmute();
		}else {
			mute();
		}
	}

	/**
	 * sets state to muted and stops any sound effects currently playing
	 */
	public void mute(){
		muted = true;
		this.previousVolume = this.volume;
		this.setVolume(0.0f);

		List<GameEntity> entities = this.entityCollector.getEntities();
		for (GameEntity entity: entities) {
			entity.getComponent(MovementAudioComponent.class).stop();
		}
	}

	public void unmute(){
		muted = false;
		this.setVolume(this.previousVolume);
	}
}