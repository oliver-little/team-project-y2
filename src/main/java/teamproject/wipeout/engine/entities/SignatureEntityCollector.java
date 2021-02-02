package teamproject.wipeout.engine.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 
 * Uses a component signature to determine what entities to collect
 */
public class SignatureEntityCollector extends BaseEntityCollector {

    protected Set<String> _signature;
    protected List<String> _entityList;

    private EntityManager _entityManager;

    public SignatureEntityCollector(EntityManager e, Set<String> signature) {
        this._entityManager = e;
        this._signature = signature;

        this._entityList = new ArrayList<String>();

        e.entityEvent.addObserver(this);
    }

    public void cleanup() {
        this._entityManager.entityEvent.removeObserver(this);
    }

    public String[] getEntities() {
        return (String[]) this._entityList.toArray();
    }

    protected void _addComponent(String entityID, Set<String> components) {
        if (this._testComponent(entityID, components) && !this._entityList.contains(entityID)) {
            this._entityList.add(entityID);
        }
    }

    protected void _removeComponent(String entityID, Set<String> components) {
        if (!this._testComponent(entityID, components)) {
            this._entityList.remove(entityID);
        }
    }

    protected void _removeEntity(String entityID) {
        this._entityList.remove(entityID);
    }
    

    private boolean _testComponent(String entityID, Set<String> components) {
        for (String componentString : this._signature) {
            if (!components.contains(componentString)) {
                return false;
            }
        }
        return true;
    }
}
