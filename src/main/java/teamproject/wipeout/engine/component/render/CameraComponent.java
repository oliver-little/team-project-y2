package teamproject.wipeout.engine.component.render;

import teamproject.wipeout.engine.component.GameComponent;

public class CameraComponent implements GameComponent {
    public float zoom;

    public CameraComponent() {
        this.zoom = 1;
    }

    public CameraComponent(float zoom) {
        this.zoom = zoom;
    }

    public String getType() {
        return "camera";
    }
}
