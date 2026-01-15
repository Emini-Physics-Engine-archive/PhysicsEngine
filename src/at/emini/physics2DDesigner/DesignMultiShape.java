package at.emini.physics2DDesigner;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import at.emini.physics2D.MultiShape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.UserData;
import at.emini.physics2D.util.PhysicsFileReader;


public class DesignMultiShape extends MultiShape implements DesignShape 
{
    private String name = "Unnamed Shape";
    protected Color c;
    
    private static int currColor = 0;
    private static final Color defaultColors[] = {
        new Color(180,   0,   0, 50), 
        new Color(180, 180,   0, 50),
        new Color(  0, 180,   0, 50),
        new Color(  0, 180, 180, 50),
        new Color(  0,   0, 180, 50),
        new Color(180,   0, 180, 50) };

    public DesignMultiShape(Vector shapes) 
    {
        super(shapes);
        mUserData = new StringUserData();
        
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }
    
    protected DesignMultiShape(MultiShape shape) 
    {
        super(shape);
        
        if( shape.getUserData() != null)
        {
            mUserData = shape.getUserData().copy();
        }
        else
        {
            mUserData = new StringUserData();
        }
        
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }
        
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    
    public String toString()
    {
        return name;
    }
    
    public Color getColor()
    {
        return c;
    }
    
    public void setColor(Color c)
    {
        this.c = c;
    }
    
    public Color getOpaqueColor()
    {        
        return DesignerUtilities.getGrayBlendColor(c);
    }

    @Override
    public void draw(GraphicsWrapper g, boolean edit)
    {
        for( int i = 0 ; i < getShapeCount(); i++)
        {
            Shape s = getShape(i);
            if (s instanceof DesignShape)
            {
                ((DesignShape) s).draw(g, edit);
            }
        }
    }

    public void saveToFile(File file)
    {
        
    }
    
    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#saveToFile(at.emini.physics2DDesigner.MyFileWriter)
     */
    public void saveToFile(MyFileWriter fileWriter, Vector worldshapes)
    {
        try 
        {   
            fileWriter.write( (byte) mShapes.length );
            for( int i = 0; i < mShapes.length; i++)
            {
                fileWriter.write( (byte) worldshapes.indexOf(mShapes[i]) );
            }
            
            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);            
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }
                
    }

    public int[] getStartIndices()
    {
        return mVertexStartIndices;
    }

    public void addShape(DesignShape shape)
    {
        Vector shapes = new Vector();
        for( int i = 0; i < getShapeCount(); i++)
        {
            shapes.add(getShape(i));
        }
        shapes.add(shape);
        initShapeMembers(shapes); 
    }
    
    public boolean removeShape(DesignShape shape)
    {
        if (getShapeCount() <= 1)
        {
            return false;
        }
            
        Vector shapes = new Vector();
        for( int i = 0; i < getShapeCount(); i++)
        {
            if (getShape(i) != shape)
            {
                shapes.add(getShape(i));
            }
        }        
        initShapeMembers(shapes); 
        
        return true;
    }

    public boolean  isSelected(DesignShape s)
    {
        for( int i = 0; i < mShapes.length; i++)
        {
            if (mShapes[i] == s)
            {
                return true;
            }
        }
        return false;
    }

    
    public static DesignShape loadDesignShape( PhysicsFileReader reader, UserData userData, Vector shapes)
    {
        return new DesignMultiShape( MultiShape.loadShape(reader, userData, shapes));
    }
}
