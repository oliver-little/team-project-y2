package teamproject.wipeout.engine.entity.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import teamproject.wipeout.engine.entity.GameEntity;
import teamproject.wipeout.engine.component.GameComponent;
import teamproject.wipeout.engine.core.GameScene;

public class MultipleSignatureCollector {

    protected List<Set<Class<? extends GameComponent>>> _signatures;
    protected SignatureEntityCollector[] _signatureCollectors;
    
    public MultipleSignatureCollector(GameScene e, List<Set<Class<? extends GameComponent>>> signatures) {
        this._signatures = signatures;
        this._signatureCollectors = new SignatureEntityCollector[signatures.size()];

        for (int i = 0; i < signatures.size(); i++) {
            this._signatureCollectors[i] = new SignatureEntityCollector(e, signatures.get(i));
        }
    }

    public void cleanup() {
        for (SignatureEntityCollector entityCollector : this._signatureCollectors) {
            entityCollector.cleanup();
        }
    }

    public List<GameEntity> getEntitiesForSignature(Set<Class<? extends GameComponent>> signature) {
        for (int i = 0; i < this._signatures.size(); i++) {
            if (this._signatures.get(i).equals(signature)) {
                return this._signatureCollectors[i].getEntities();
            }
        }
        return null;
    }

    public List<GameEntity> getEntities() {
        ArrayList<GameEntity> out = new ArrayList<GameEntity>();
        for(SignatureEntityCollector s : this._signatureCollectors) {
            out.addAll(s.getEntities());
        }
        return out;
    }
}
