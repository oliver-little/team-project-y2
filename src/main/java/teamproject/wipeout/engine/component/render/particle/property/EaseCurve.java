package teamproject.wipeout.engine.component.render.particle.property;

import java.util.function.Function;

/**
 * Implements various Bezier easing curves
 */
public class EaseCurve implements Function<Double, Double> {

    public static final EaseCurve LINEAR = new EaseCurve(EaseType.LINEAR);
    public static final EaseCurve EASE_IN = new EaseCurve(EaseType.EASE_OUT);
    public static final EaseCurve EASE_OUT = new EaseCurve(EaseType.EASE_OUT);
    public static final EaseCurve EASE_IN_OUT = new EaseCurve(EaseType.EASE_IN_OUT);
    public static final EaseCurve INVERSE_EASE_IN_OUT = new EaseCurve(EaseType.INVERSE_EASE_IN_OUT);
    public static final EaseCurve FADE_IN_OUT = new EaseCurve(EaseType.FADE_IN_OUT);

    /**
     * Represents the various easing types, as well as implementing the functions required to calculate those easing types.
     */
    public enum EaseType {
        LINEAR((x) -> x),
        EASE_IN((x) -> x * x * x),
        EASE_OUT((x) -> (-x) * x * x + 1),
        EASE_IN_OUT((x) -> {
            if (x < 0.5) {
                return 4 * x * x * x;
            }
            else {
                double partial = 2 * x - 2;
                return (x - 1) * partial * partial + 1;
            }
        }),
        INVERSE_EASE_IN_OUT((x) -> {
            if (x < 0.5) {
                return 1 - (4 * x * x * x);
            }
            else {
                double partial = 2 * x - 2;
                return 1 - ((x - 1) * partial * partial + 1);
            }
        }),
        FADE_IN_OUT((x) -> {
            double oneMinus = 1 - x;
            return 3 * x * Math.pow(oneMinus, 2) * 2 + 3 * x * x * oneMinus * 0.5;
        });


        public final Function<Double, Double> easeFunction;

        private EaseType(Function<Double, Double> easeFunction) {
            this.easeFunction = easeFunction;
        }
    }

    private Function<Double, Double> easeFunction;

    /**
     * Creates an instance of EaseCurve
     * 
     * @param type The easing type to use
     */
    public EaseCurve(EaseType type) {
        this.easeFunction = type.easeFunction;
    }

    /**
     * Applies the easing function to a given double value. The value should be between 0 and 1 or unexpected behaviour will occur
     * 
     * @param x The value to apply the function to
     * @return The mapped value
     */
    public Double apply(Double x) {
        return easeFunction.apply(x);
    }
}
