package teamproject.wipeout.util;

import java.util.Random;
import java.util.function.Supplier;

import javafx.geometry.Point2D;

public class SupplierGenerator {
    public static <T> Supplier<T> staticSupplier(T value) {
        return () -> {return value;};
    }

    public static Supplier<Double> doubleRangeSupplier(double min, double max) {
        return () -> {
            return new Random().nextDouble() * (max - min) + min;
        };
    }

    public static Supplier<Point2D> pointRangeSupplier(Point2D min, Point2D max) {
        return () -> {
            Random r = new Random();
            double x = min.getX();
            if (min.getX() < max.getX()) {
                x = r.nextDouble() * (max.getX() - min.getX()) + min.getX();
            }
            double y = min.getY();
            if (min.getY() < max.getY()) {
                y = r.nextDouble() * (max.getY() - min.getY()) + min.getY();
            }
            return new Point2D(x, y);
        };
    }
}
