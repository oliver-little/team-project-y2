package teamproject.wipeout.engine.component;

import java.util.function.Consumer;

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
