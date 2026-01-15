package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class Grid {

    private static final int decimal = FXUtil.DECIMAL;
    private static final int koeff = 1 << decimal;      //trick to avoid preprocessing
    private int spacingFX = FXUtil.ONE_FX * 10;
   
    private boolean active = false;
    
    public Grid()
    {        
    }
    
    public void setParameter(int spacingFX, boolean active)
    {
        this.spacingFX = spacingFX;   
        this.active = active;        
    }
    
    public void draw(GraphicsWrapper g)
    {
        if (active)
        {
            Rectangle clip = g.getClipBounds();
            
            if (clip != null)
            {
                for( int x = 0, i = 0; x < clip.x + clip.width; x += spacingFX, i++)
                {
                    g.setColor( i % 10 == 0 ? Color.gray : Color.lightGray);
                    g.drawLine(x, clip.y, x, clip.y + clip.height);
                }
                for( int x = 0, i = 0; x > clip.x; x -= spacingFX, i++)
                {
                    g.setColor( i % 10 == 0 ? Color.gray : Color.lightGray);
                    g.drawLine(x, clip.y, x, clip.y + clip.height);
                }
                
                for( int y = 0, i = 0; y < clip.y + clip.height; y += spacingFX, i++)
                {
                    g.setColor( i % 10 == 0 ? Color.gray : Color.lightGray);
                    g.drawLine(clip.x, y, clip.x + clip.width, y);
                }
                for( int y = 0, i = 0; y > clip.y; y -= spacingFX, i++)
                {
                    g.setColor( i % 10 == 0 ? Color.gray : Color.lightGray);
                    g.drawLine(clip.x, y, clip.x + clip.width, y);
                }
            }            
        }
    }
    
    public FXVector snapToGrid(FXVector v)
    {
        if (active)
        {
            FXVector gridVector = new FXVector(v);
            
            gridVector.xFX = ((v.xFX + (int) Math.signum(v.xFX) * (spacingFX / 2 - 1))/ spacingFX) * spacingFX;
            gridVector.yFX = ((v.yFX + (int) Math.signum(v.yFX) * (spacingFX / 2 - 1))/ spacingFX) * spacingFX;
            return gridVector;
        }
        return v;
    }
    
    public Point2D snapToGrid(Point2D v)
    {
        if (active)
        {
            Point2D gridVector = new Point2D.Double(((int)(v.getX() + Math.signum(v.getX()) *(spacingFX / 2 - 1))/ spacingFX) * spacingFX, 
                                                    ((int)(v.getY() + Math.signum(v.getY()) *(spacingFX / 2 - 1))/ spacingFX) * spacingFX);
            return gridVector;
        }
        return v;
    }
}
