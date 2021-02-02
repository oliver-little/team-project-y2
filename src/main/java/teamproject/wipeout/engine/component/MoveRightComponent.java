package teamproject.wipeout.engine.component;

public class MoveRightComponent extends GameComponent {
    public float speed;

    public MoveRightComponent() {
        this.speed = 5;
    }

    public MoveRightComponent(float speed) {
        this.speed = speed;
    }
}
