package teamproject.wipeout.engine.component;

import teamproject.wipeout.game.farm.Pickables;

public class PickableComponent implements GameComponent {

    public final Pickables.Pickable pickable;

    public PickableComponent(Pickables.Pickable pickable) {
        this.pickable = pickable;
    }

    public String getType() {
        return "pickable";
    }
}
