package at.emini.physics2DDesigner;

import java.awt.geom.Point2D;

public interface ShapeElement
{
    public void draw(GraphicsWrapper g);
    public void setMoving(boolean moving);

    public void setPos(Point2D point);
    public boolean checkPoint(GraphicsWrapper g, Point2D point);

}
