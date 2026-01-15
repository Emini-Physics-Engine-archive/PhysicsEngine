package at.emini.physics2DDesigner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

/**
 * used to draw both FX and floats to screen correctly
 * @author Alexander Adensamer
 *
 */
public class GraphicsWrapper
{
    private Graphics2D g;
    private AffineTransform transform = new AffineTransform(); 
    
    private static final int decimal = FXUtil.DECIMAL;
    private static final int koeff = 1 << decimal;      //trick to avoid preprocessing
    
    public GraphicsWrapper(Graphics2D g)
    {
        this.g = g;
    }
    
    public static Polygon createPolygon(FXVector[] positions, int startIndex, int endIndex)
    {
        Polygon polygon = new Polygon();
        for( int i = startIndex; i < endIndex; i++)
        {
            polygon.addPoint( (positions[i].xFX),  (positions[i].yFX));   //#FX2F polygon.addPoint( (int) (positions[i].xFX * koeff),  (int) (positions[i].yFX * koeff)); 
        }
        return polygon;
    }
        
    public double getZoomScale()
    {
        return transform.getScaleX();
    }
    
    public void setColor(Color c)
    {
        g.setColor(c);        
    }
    
    public void setXORMode(Color c)
    {
        g.setXORMode(c);        
    }
    
    public void setPaintMode()
    {
        g.setPaintMode();        
    }
    
    public void setStroke(Stroke s)
    {
        g.setStroke(s);        
    }
    
    public void setClip(int i, int j, int width, int height)
    {
        g.setClip(i, j, width, height);        
    }

    public Rectangle getClipBounds()
    {
        return g.getClipBounds();
    }
    
    public void transform(AffineTransform transform)
    {
        this.transform.concatenate(transform);
        g.transform(transform); 
    }    

    public void translate(int x, int y)
    {
        g.translate(x, y);
    }

    public void setRenderingHint(Key hintKey, Object hintValue)
    {
        g.setRenderingHint(hintKey, hintValue);
    }

    public void setStroke(BasicStroke s)
    {
        g.setStroke(s);
    }

    public void setPaint(Paint paint)
    {
        g.setPaint(paint);
    }
    
    public void fillPolygon(Polygon polygon)
    {
        g.fillPolygon(polygon);
    }
    public void drawPolygon(Polygon polygon)
    {
        g.drawPolygon(polygon);
    }
    
    public void drawLine(int a, int b, int c, int d)
    {
        g.drawLine(a, b, c, d);
    }
    
    public void drawLine(double a, double b, double c, double d)
    {
        //#FX2F g.drawLine((int) (a * koeff), (int) (b * koeff), (int) (c * koeff), (int) (d * koeff));
    }
        
    public void fillRect(int a, int b, int c, int d)
    {
        g.fillRect(a, b, c, d);
    }
    
    public void fillRect(double a, double b, double c, double d)
    {
      //#FX2F g.fillRect((int) (a * koeff), (int) (b * koeff), (int) (c * koeff), (int) (d * koeff));
    }

    public void drawRect(int a, int b, int c, int d)
    {
        g.drawRect(a, b, c, d);
    }
    
    public void drawRect(double a, double b, double c, double d)
    {
      //#FX2F g.drawRect((int) (a * koeff), (int) (b * koeff), (int) (c * koeff), (int) (d * koeff));
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle)
    {
        g.fillArc(x, y, width, height, startAngle, arcAngle);
    }
    
    public void fillArc(double x, double y, double width, double height, int startAngle, int arcAngle)
    {
      //#FX2F g.fillArc((int) (x * koeff), (int) (y * koeff), (int) (width * koeff), (int) (height * koeff), startAngle, arcAngle);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle)
    {
        g.drawArc(x, y, width, height, startAngle, arcAngle);
    }
    
    public void drawArc(double x, double y, double width, double height, int startAngle, int arcAngle)
    {
      //#FX2F g.drawArc((int) (x * koeff), (int) (y * koeff), (int) (width * koeff), (int) (height * koeff), startAngle, arcAngle);
    }

    public void drawImage(BufferedImage img, int x, int y, int width,
            int height, int rotation, ImageObserver observer)
    {
        AffineTransform at = new AffineTransform();
        at.rotate((rotation * Math.PI) / (double) FXUtil.PI_2FX, ( img.getWidth() / 2.0), (img.getHeight()/ 2.0));

        /*int xoffset = (int) (img.getWidth() / Math.sqrt(2.0));
        int yoffset = (int) (img.getHeight() / Math.sqrt(2.0));
        
        AffineTransform translationTransform;
        //translationTransform = findTranslation(at, img);
        translationTransform = AffineTransform.getTranslateInstance( -xoffset, -yoffset);
        translationTransform.scale(Math.sqrt(0.5), Math.sqrt(0.5));
        at.preConcatenate(translationTransform);
*/
        
        BufferedImageOp bio = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage target = bio.filter(img, null);
        
        g.drawImage(target, x, // - xoffset, 
                            y, // - yoffset, 
                            width, height, observer);
    }
    
    public void drawImage(BufferedImage img, double x, double y, double width,
            double height, double rotation, ImageObserver observer)
    {
      //#FX2F g.drawImage(img, (int) (x * koeff), (int) (y * koeff), (int) (width * koeff), (int) (height * koeff), observer);
    }

    public void drawImage(BufferedImage img, int x, int y, ImageObserver observer)
    {
        g.drawImage(img, x, y, observer);
    }

    public void drawImage(BufferedImage img, double x, double y, ImageObserver observer)
    {
      //#FX2F g.drawImage(img, (int) (x * koeff), (int) (y * koeff), observer);
    }


    public void drawString(String label, int x, int y)
    {
        g.drawString(label, x, y);        
    }

    
}
