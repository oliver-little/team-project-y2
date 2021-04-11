package teamproject.wipeout.engine.system.ai;

import javafx.geometry.Point2D;
import teamproject.wipeout.engine.component.ai.NavigationEdge;
import teamproject.wipeout.engine.component.ai.NavigationSquare;

public class FunnelData {
    public Point2D apex;
    public Point2D negApex;
    public Point2D left;
    public int leftIndex;
    public Point2D right;
    public int rightIndex;
    public Point2D leftVector;
    public Point2D rightVector;
    public double angle;
    public boolean startIsLeft;

    private static final double doubleCompare = 0.00001f;

    public FunnelData() {
        apex = Point2D.ZERO;
        left = Point2D.ZERO;
        right = Point2D.ZERO;
        leftVector = Point2D.ZERO;
        leftIndex = 0;
        rightVector = Point2D.ZERO;
        leftIndex = 0;
    }

    public FunnelData(Point2D apex, NavigationEdge edge, int index) {
        this.setData(apex, edge, index);
    }

    public Point2D getLeft() {
        return this.left;
    }

    public void setLeft(Point2D newLeft, int index) {
        this.left = newLeft;
        this.leftIndex = index;
        this.leftVector = left.add(negApex);
        this.angle = leftVector.angle(rightVector);
    }

    public Point2D getRight() {
        return this.left;
    }

    public void setRight(Point2D newRight, int index) {
        this.right = newRight;
        this.rightIndex = index;
        this.rightVector = right.add(negApex);
        this.angle = leftVector.angle(rightVector);
    }

    public void setData(Point2D apex, NavigationEdge edge, int index) {
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
        this.leftIndex = index;
        this.rightIndex = index;
        this.leftVector = left.add(negApex);
        this.rightVector = right.add(negApex);
        this.angle = leftVector.angle(rightVector);
    }

    private boolean isStartLeftPoint(Point2D apex, NavigationEdge edge) {
        // This function relies on the fact that NavigationEdges are joined along one common axis value
        NavigationSquare square = edge.adjacentSquare;
        Point2D midPoint = square.bottomRight.add(square.topLeft).multiply(0.5);
        // Matching X values means this edge is joining horizontally stacked squares
        if (Math.abs(edge.start.getX() -edge.end.getX()) < doubleCompare) {
            // If the apex is to the right of the midpoint of the adjacent square, the end point is on the left 
            if (apex.getX() > midPoint.getX()) {
                return false;
            }
            else {
                return true;
            }
        }
        else if (Math.abs(edge.start.getY() - edge.end.getY()) < doubleCompare) {
            // If the apex is below the midpoint of the adjacent square (y coordinates go down), the start point is on the left
            if (apex.getY() > midPoint.getY()) {
                return true;
            }
            else {
                return false;
            }
        }
        
        throw new IllegalArgumentException("NavigationEdge points are not aligned on one axis.");
    }
}
