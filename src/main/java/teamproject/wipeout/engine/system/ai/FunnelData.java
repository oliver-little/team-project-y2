package teamproject.wipeout.engine.system.ai;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;

public class FunnelData {
    public Point2D apex;
    public Point2D negApex;
    public Point2D left;
    public Point2D right;
    public Point2D leftVector;
    public Point2D rightVector;
    public double angle;
    public double crossProduct;
    public boolean startIsLeft;

    private static final double doubleCompare = 0.00001f;

    public FunnelData() {
        apex = Point2D.ZERO;
        left = Point2D.ZERO;
        right = Point2D.ZERO;
        leftVector = Point2D.ZERO;
        rightVector = Point2D.ZERO;
        crossProduct = 0;
    }

    public FunnelData(Point2D apex, NavigationEdge edge) {
        this.setData(apex, edge);
    }

    public Point2D getLeft() {
        return this.left;
    }

    public void setLeft(Point2D newLeft) {
        this.left = newLeft;
        this.leftVector = left.add(negApex);
        this.angle = leftVector.angle(rightVector);
        this.crossProduct = leftVector.crossProduct(rightVector).getZ();
    }

    public Point2D getRight() {
        return this.left;
    }

    public void setRight(Point2D newRight) {
        this.right = newRight;
        this.rightVector = right.add(negApex);
        this.angle = leftVector.angle(rightVector);
        this.crossProduct = leftVector.crossProduct(rightVector).getZ();
    }

    public void setData(Point2D apex, NavigationEdge edge) {
        this.apex = apex;
        this.negApex = apex.multiply(-1);
        
        this.startIsLeft = isStartLeftPoint(apex, edge);
        if (startIsLeft) {
            this.left = edge.start;
            this.right = edge.end;
        }
        else {
            this.left = edge.end;
            this.right = edge.start;
        }
        this.leftVector = left.add(negApex);
        this.rightVector = right.add(negApex);
        this.angle = leftVector.angle(rightVector);
        this.crossProduct = leftVector.crossProduct(rightVector).getZ();
    }

    private boolean isStartLeftPoint(Point2D apex, NavigationEdge edge) {
        // This function relies on the fact that NavigationEdges are joined along one common axis value
        
        // Matching X values means this edge is joining horizontally stacked squares
        if (Math.abs(edge.start.getX() -edge.end.getX()) < doubleCompare) {
            // If the apex is to the right of the edge line, the end point is on the left
            if (apex.getX() > edge.start.getX()) {
                return false;
            }
            else {
                return true;
            }
        }
        else if (Math.abs(edge.start.getY() - edge.end.getY()) < doubleCompare) {
            // If the apex is below the edge line (y coordinates go down), the end point is on the left
            if (apex.getY() > edge.start.getY()) {
                return false;
            }
            else {
                return true;
            }
        }
        
        throw new IllegalArgumentException("NavigationEdge points are not aligned on one axis.");
    }
}
