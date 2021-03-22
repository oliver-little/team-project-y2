package teamproject.wipeout.engine.component.render.particle.property;

/**
 * Applies parameters to an easing curve - multiplier and offset
 */
public class ParameterEaseCurve extends EaseCurve {

    public double multiplier;
    public double offset;
    
    /**
     * Creates a new instance of ParameterEaseCurve
     * 
     * @param type The type of easing to use
     * @param multiplier A multiplier to apply to the result of the easing curve
     * @param offset An offset to apply to the result of the easing curve
     */
    public ParameterEaseCurve(EaseType type, double multiplier, double offset) {
        super(type);
        this.multiplier = multiplier;
        this.offset = offset;
    }

    @Override
    public Double apply(Double x) {
        return multiplier * super.apply(x) + offset;
    }
}
