package teamproject.wipeout.engine.component.ai;

//This class is currently not yet used, potentially useful for collisons, obsticles and boundaries for AI characters later on.

public class AI {

    private double xMax;
    private double xMin;
    private double yMax;
    private double yMin;

    public AI() {
        //default boundary values here
    }

    public AI(double xMax, double xMin, double yMax, double yMin) {
        this.xMax = xMax;
        this.xMin = xMin;
        this.yMax = yMax;
        this.yMin = yMin;
    }

    public double getXMax() {
        return xMax;
    }

    public double getXMin() {
        return xMin;
    }

    public double getYMax() {
        return yMax;
    }

    public double getYMin() {
        return yMin;
    }

    public boolean inRange(double x, double y) {
        if ((x >= this.getXMin() && x <= this.getXMax()) && (x >= this.getYMin() && y <= this.getYMax())) {
            return true;
        }
        else {
            return false;
        }
    }
    
}
