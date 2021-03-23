package teamproject.wipeout.util;

import java.util.Random;
import java.util.function.Supplier;

import javafx.geometry.Point2D;

public class SupplierGenerator {
    /**
     * Creates a supplier function for a generic type T that always provides some value of T.
     * @return The generated supplier function
     */
    public static <T> Supplier<T> staticSupplier(T value) {
        return () -> {return value;};
    }

    /**
     * Generates a supplier function of random double values within a given range
     * @param min The minimum value
     * @param max The maximum value
     * @return The generated supplier function
     */
    public static Supplier<Double> rangeSupplier(double min, double max) {
        return () -> {
            return new Random().nextDouble() * (max - min) + min;
        };
    }

    /**
     * Generates a supplier function of random integer values within a given range
     * @param min The minimum value
     * @param max The maximum value
     * @return The generated supplier function
     */
    public static Supplier<Integer> rangeSupplier(int min, int max) {
        return () -> {
            return new Random().nextInt() * (max - min) + min;
        };
    }

    /**
     * Generates a supplier function of random Point2D values within a given range
     * @param min The minimum value
     * @param max The maximum value
     * @return The generated supplier function
     */
    public static Supplier<Point2D> rangeSupplier(Point2D min, Point2D max) {
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

    /** 
     * Generates a supplier function of Point2D vectors in a random direction, with a given length
     * @param length The length to generate the vector at
     */
    public static Supplier<Point2D> circlePointSupplier(double length) {
        return () -> {
            Random r = new Random();
            return new Point2D((r.nextDouble() - 0.5) * 2, (r.nextDouble() - 0.5) * 2).normalize().multiply(length);
        };
    }

    /** 
     * Generates a supplier function of Point2D vectors in a random direction, with a length in the range min to max
     * @param minLength The minimum length to generate the vectors at
     * @param maxLength The maximum length to generate the vectors at
     * 
     */
    public static Supplier<Point2D> circlePointSupplier(double min, double max) {
        return () -> {
            Random r = new Random();
            return new Point2D((r.nextDouble() - 0.5) * 2, (r.nextDouble() - 0.5) * 2).normalize().multiply(r.nextDouble() * (max - min) + min);
        };
    }
}
