package teamproject.wipeout.engine.component;

import java.util.function.Consumer;

/**
 * ScriptComponent allows a function to be associated with an entity and the function to be called every update step of the game.
 */
public class ScriptComponent implements GameComponent {
     
    public Consumer<Double>  onStep;
    public boolean requestDeletion = false;

    public ScriptComponent(Consumer<Double> onStep) {
        this.onStep = onStep;
    }

    public String getType() {
        return "script";
    }
}
