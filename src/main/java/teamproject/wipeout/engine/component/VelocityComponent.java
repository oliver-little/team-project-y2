package teamproject.wipeout.engine.component;

public class VelocityComponent implements GameComponent {
    public float xspeed;
    public float yspeed;

    public VelocityComponent() {
        this.xspeed = 5;
        this.yspeed = 5;
    }

    public VelocityComponent(float x, float y) {
        this.xspeed = x;
        this.yspeed = y;
    }

    public String getType() {
        return "velocity";
    }
}
