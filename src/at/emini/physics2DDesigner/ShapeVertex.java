package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.geom.Point2D;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class ShapeVertex implements ShapeElement
{
    protected float xpos = 0;
    protected float ypos = 0;
    
    private boolean isMoving = false;
    
    protected FXVector corner;
    
    private static final int size = 10; // << FXUtil.DECIMAL;
    
    public ShapeVertex(FXVector vector)
    {
        xpos = vector.xFX;
        ypos = vector.yFX;
        this.corner = vector;
    }
    
    public ShapeVertex(Point2D point)
    {
        xpos = (float) point.getX();
        ypos = (float) point.getY();
        this.corner = new FXVector((int) (xpos), (int) (ypos));
    }
    
    public ShapeVertex(ShapeVertex v1, ShapeVertex v2)
    {
        xpos = (v1.xpos + v2.xpos) / 2;
        ypos = (v1.ypos + v2.ypos) / 2;
        corner = new FXVector((int) (xpos * FXUtil.ONE_FX), (int) (ypos * FXUtil.ONE_FX));
    }

    public double length()
    {
        return Point2D.distance(xpos, ypos, 0, 0);
    }
    
    public void draw(GraphicsWrapper g)
    {
        double zoomScale = g.getZoomScale();
        g.setColor(isMoving ? Color.red : Color.black);
        g.drawArc((int) (corner.xFX - size / 2 / zoomScale), (int) (corner.yFX - size / 2  /zoomScale), 
                (int) (size / zoomScale), (int) (size / zoomScale), 0, 360);
    }
    
    public void setMoving(boolean moving)
    {
        isMoving = moving;
    }

    public void setPos(Point2D point) 
    {
        xpos = (float) point.getX();
        ypos = (float) point.getY();
        
        corner.assignFX((int) (xpos), (int) (ypos));
    } 
    
    public void move(float x, float y) 
    {
        xpos += x;
        ypos += y;
        corner.assignFX((int) (xpos), (int) (ypos));
    }
    
    public void setPosFromCorner() 
    {
        xpos = corner.xFX;
        ypos = corner.yFX;                
    }
    
    public void setPosFromVec(FXVector vec) 
    {
        corner.assign(vec);
        setPosFromCorner();           
    }
    
    public boolean checkPoint(GraphicsWrapper g, Point2D point) 
    {
        return point.distance(xpos, ypos) < size / 2 / g.getZoomScale();
    }

    public FXVector getFXVector() 
    {
        return corner;
    }

    public void scale(double factor) {
        xpos *= factor;        
        ypos *= factor;
        corner.assignFX((int) (xpos ), (int) (ypos));
    }   
    
}