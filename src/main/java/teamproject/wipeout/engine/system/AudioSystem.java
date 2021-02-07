package teamproject.wipeout.engine.system;

import java.util.Set;
import java.util.List;

import teamproject.wipeout.engine.component.audio.AudioComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class AudioSystem implements GameSystem {

	protected SignatureEntityCollector _entityCollector;
	
	/**
	 * System which dictates which sounds to play
	 * @param e  Scene which is searched for entities with AudioComponents
	 */
    public AudioSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(AudioComponent.class)); //collects entities with AudioComponents
    }
    
    /**
     * Checks, for each frame, whether any AudioComponents need playing.
     */
	public void accept(Double timeStep) {
		List<GameEntity> entities = this._entityCollector.getEntities();
		for (GameEntity entity : entities) { //iterates through all entities with AudioComponents
			AudioComponent s = entity.getComponent(AudioComponent.class);
			if (s.toPlay()) {
				s.playSound();
			}
		}
	}
}