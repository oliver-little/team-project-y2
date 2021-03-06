package teamproject.wipeout.engine.component;

public class ScriptComponent implements GameComponent {
     
    public Runnable onStep;

    public ScriptComponent(Runnable onStep) {
        this.onStep = onStep;
    }

    public String getType() {
        return "script";
    }
}
