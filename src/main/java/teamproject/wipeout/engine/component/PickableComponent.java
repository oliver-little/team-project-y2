package teamproject.wipeout.engine.component;

import teamproject.wipeout.game.farm.Pickable;

public class PickableComponent implements GameComponent {

    public final Pickable pickable;

    public PickableComponent(Pickable pickable) {
        this.pickable = pickable;
    }

    public String getType() {
        return "pickable";
    }
}
