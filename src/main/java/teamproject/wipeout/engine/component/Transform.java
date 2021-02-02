package teamproject.wipeout.engine.component;

import javafx.geometry.Point2D;

public class Transform extends GameComponent {

    public static String type = "transform";

    public Point2D position;
    public double rotation;

    public Transform() {
        this.position = Point2D.ZERO;
        this.rotation = 0;
    }

    public Transform(double x, double y) {
        this.position = new Point2D(x, y);
    }

    public Transform(Point2D position, double rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Transform(double x, double y, double rotation) {
        this.position = new Point2D(x, y);
        this.rotation = rotation;
    }
}

