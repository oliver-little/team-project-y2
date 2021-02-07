package teamproject.wipeout.engine.system;

import java.util.Set;
import java.util.List;

import teamproject.wipeout.engine.component.sound.SoundComponent;
import teamproject.wipeout.engine.core.GameScene;
import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.entity.collector.SignatureEntityCollector;

public class SoundSystem implements GameSystem{

	protected SignatureEntityCollector _entityCollector;
	
	/**
	 * System which dictates which sounds to play
	 * @param e  Scene which is searched for entities with SoundComponents
	 */
    public SoundSystem(GameScene e) {
        this._entityCollector = new SignatureEntityCollector(e, Set.of(SoundComponent.class)); //collects entities with SoundComponents
    }
    
    /**
     * Checks, for each frame, whether any SoundComponents need playing.
     */
	public void accept(Double timeStep)
	{
		List<GameEntity> entities = this._entityCollector.getEntities();
		for (GameEntity entity : entities) { //iterates through all entities with SoundComponents
			SoundComponent s = entity.getComponent(SoundComponent.class);
			if(s.toPlay()) { 
				s.playSound();
			}
		}
	}
}