package at.emini.physics2DDesigner;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.MultiShape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.ShapeSet;

public class DesignShapeSet extends ShapeSet
{
    private Vector images = new Vector();
    
    private int stdShapeCount = 0;
    
    public DesignShapeSet()
    {
        super();
    }
    
    public DesignShapeSet(ShapeSet set)
    {
        super(set);
        if (set instanceof DesignShapeSet)
        {
            for( int i = 0; i < ((DesignShapeSet) set).images.size(); i++)
            {
                images.addElement(((DesignShapeSet) set).images.elementAt(i));
            }
        }
        else
        {
            for( int i = 0; i < mShapes.size(); i++)
            {
                images.addElement(null);
            }
        }
    }
    
    public ShapeSet copy()
    {
        return new DesignShapeSet(this);
    }
    
    public void registerShape(Shape s)
    {
        registerShape(s, null);
    }
    
    public void registerShapes(Vector newShapes)
    {
        for( int i = 0; i < newShapes.size(); i++)
        {
            registerShape((Shape) newShapes.get(i), null);
        }
    }
    
    public void registerShape(Shape s, Image i)
    {
        super.registerShape(s);
        if (! (s instanceof MultiShape)) 
        {
            stdShapeCount ++;
        }
        
        images.addElement(i);        
    }
    
    public void registerImage(BufferedImage  image, int index)
    {
        if (index < images.size())
        {
            images.setElementAt(image, index);
        }
    }
    
    public void registerImageForShape(BufferedImage image, Shape s)
    {
        int index = s.getId();
        if (index < images.size())
        {
            images.setElementAt(image, index);
        }
    }

    /**
     * return the corresponding image to a body
     * @param b the body
     * @return
     */
    public BufferedImage getImage(Body b)
    {  
        Object image = images.elementAt(b.shape().getId());
        if (image == null) return null;
        return (BufferedImage) image;
    }
    
    
    /**
     * return the corresponding image to a shape
     * @param s the shape
     * @return
     */
    public Image getImage(Shape s)
    {  
        Object image = images.elementAt(s.getId());
        if (image == null) return null;
        return (Image) image;
    }
    

    public void removeShape(Shape s)
    {
        //first find shape
        int index = -1;
        for( int i = 0; i < mShapes.size(); i++)
        {
            if (((Shape) mShapes.elementAt(i)) == s) 
            {
                index = i;
                break;
            }
        }
        
        if (!(s instanceof MultiShape) )
        {
            stdShapeCount--;
        }
        
        mShapes.remove(index);
        images.remove(index);
        
        for( int i = index; i < mShapes.size(); i++)
        {
            correctShapeId(i);
        }
    }
    
    public int getStdShapeCount()
    {
        return stdShapeCount;
    }
}
