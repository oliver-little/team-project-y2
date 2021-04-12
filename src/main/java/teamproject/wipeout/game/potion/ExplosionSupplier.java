package teamproject.wipeout.game.potion;

import java.util.Random;
import java.util.function.Supplier;

import javafx.geometry.Point2D;
import teamproject.wipeout.util.SupplierGenerator;

public class ExplosionSupplier implements Supplier<Point2D> {
    private double min;
    private double max;
    private Point2D vector = new Point2D(1, 0);
    private Random r = new Random();

    public ExplosionSupplier(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public Point2D get() {
        vector = SupplierGenerator.rotateVector(vector, 5);
        double scalar = r.nextDouble() * (max - min) + min;
        return new Point2D(vector.getX() * scalar, vector.getY() * scalar * 0.85);
    }
}
