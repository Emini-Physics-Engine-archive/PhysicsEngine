package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class ShapeEdge implements ShapeElement
{
    private ShapeVertex start;
    private ShapeVertex end;
    
    private boolean isMoving = false;
    private static final int size = 4; // << FXUtil.DECIMAL;
    
    private Point2D referenceMovePoint = null;
    
    public ShapeEdge(ShapeVertex start, ShapeVertex end)
    {
        this.start = start;
        this.end = end;
    }
    
    public ShapeVertex getStart() {
        return start;
    }
    
    public void setStart( ShapeVertex start) {
        this.start = start;
    }
    
    public ShapeVertex getEnd() {
        return end;
    }
    
    public void setEnd( ShapeVertex end) {
        this.end = end;
    }
    
    public void draw(GraphicsWrapper g)
    {
        g.setColor(isMoving ? Color.red : Color.black);
        int x1FX = (int) start.corner.xFX;
        int y1FX = (int) start.corner.yFX;
        int x2FX = (int) end.corner.xFX;
        int y2FX = (int) end.corner.yFX;

        g.drawLine(x1FX, y1FX, x2FX, y2FX);
        
    }
    
    public void setPos(Point2D point) 
    {
        if ( referenceMovePoint != null)
        {
            float x = (float) (point.getX() - referenceMovePoint.getX());
            float y = (float) (point.getY() - referenceMovePoint.getY());
            
            start.move(x, y);
            end.move(x, y);
            
            referenceMovePoint = point;
        }
    } 
    
    public void setMoving(boolean moving)
    {
        isMoving = moving;
    }
    
    public boolean checkPoint(GraphicsWrapper g, Point2D point) 
    {
        boolean isClose = Line2D.ptLineDist(start.xpos, start.ypos, end.xpos, end.ypos, point.getX(), point.getY()) < size / g.getZoomScale();
        if (isClose)
        {
            referenceMovePoint = point;
        }
        return isClose;
    }

}