package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Motor;
import at.emini.physics2D.UserData;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;

public class DesignMotor extends Motor implements DesignConstraint {
     
    private static BufferedImage motorIcon = null;
    
    private static int offset = 5;
    private static int width = 20;
    private static int height = 20;
    
    
    public DesignMotor(Body b, int targetRotation2FX, int maxForceFX)
    {
        super(b, targetRotation2FX, maxForceFX);
        mUserData = new StringUserData();
        initIcon();
    }
    
    public DesignMotor(Body b, int targetXFX, int targetYFX, int maxForceFX)
    {
        super(b, targetXFX, targetYFX, maxForceFX);
        mUserData = new StringUserData();
        initIcon();
    }
    
    
    public DesignMotor(Motor motor, Body[] bodyMapping) 
    {
        super(motor, bodyMapping);
        initIcon();
        if (mUserData == null)
        {
            mUserData = new StringUserData();
        }
    }
    
    public DesignMotor(DesignMotor motor, Body[] bodyMapping) 
    {
        super(motor, bodyMapping);
        mUserData = motor.mUserData.copy();
        initIcon();
    }
    
    public Color getColor()
    {
        return Color.gray;
    }

    public void setColor(Color c)
    {
    }
    
    private void initIcon()
    {
        if ( motorIcon == null)
        {
            try 
            {            
                motorIcon = ImageIO.read( getClass().getResourceAsStream("/res/icon_motor.gif") );
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public Constraint copy(Body[] bodyMapping)
    {
        DesignMotor j = new DesignMotor( this, bodyMapping);
        return j;
    }

    
    public void drawObject(GraphicsWrapper g, Color c, Color c2, boolean drawFull) 
    {
        if (drawFull)
        {
            double zoomScale = g.getZoomScale();
        	g.drawImage(motorIcon, body().positionFX().xFX, body().positionFX().yFX + (int) (offset / zoomScale),
                    (int) (width / zoomScale), (int) (height / zoomScale), 0, null);
    	   
        	g.setColor(c);
        	g.drawRect(body().positionFX().xFX, body().positionFX().yFX + (int) (offset / zoomScale),
               (int) (width / zoomScale), (int) (height / zoomScale));
        }
    }
    	
    public void drawInteractives(GraphicsWrapper g, Color c, Color c2) 
    {
        double zoomScale = g.getZoomScale();
        if (c != Color.BLACK)
        {
            g.setColor(c);
            g.fillRect(body().positionFX().xFX, body().positionFX().yFX + (int) (offset / zoomScale),
                (int) (width / zoomScale), (int) (height / zoomScale));
        }
        drawObject(g, c, c2, true);
    }
    
    public void drawSelecting(Graphics g, FXVector pos) 
    {        
    }
    
    public void saveToFile(File file, Vector bodies)
    {
        try 
        {
            MyFileWriter fileWriter = new MyFileWriter( file );
            saveToFile(fileWriter, bodies);
            fileWriter.close();
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }
    }
    
    public void saveToFile(MyFileWriter fileWriter, Vector bodies)
    {
        try 
        {   
            fileWriter.write( Constraint.MOTOR );
            
            fileWriter.write( bodies.indexOf(body()) );
            fileWriter.writeInt( getTargetAFX() );
            fileWriter.writeInt( getTargetBFX() );
            fileWriter.writeInt( getMaxForceFX() );
            int flags = 0;
            flags |= isRotation() ? 0x01 : 0;
            flags |= isRelative() ? 0x02 : 0;
            fileWriter.write( flags );
            
            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }
                
    }

    public static DesignMotor loadFromFile(File file, Vector bodies, UserData userData) 
    {
        PhysicsFileReader reader = new PhysicsFileReader(file);
        
        Motor motor = Motor.loadMotor(reader, bodies, userData);
        
        return new DesignMotor(motor, null);
    }

    public boolean canMovePoint(FXVector point, FXVector mousePos)
    {
       return false;
    }
    
    public int getAction(GraphicsWrapper g, FXVector mousePos) 
    {       
       return Designer.ACTION_NONE;
    }
    
    public boolean isPointInConstraint(GraphicsWrapper g, FXVector mousePos)
    {   
        double zoomScale = g.getZoomScale();        
        return  mousePos.xFX >= body().positionFX().xFX &&
                mousePos.xFX <= body().positionFX().xFX + width / zoomScale &&
                mousePos.yFX >= body().positionFX().yFX + offset / zoomScale &&
                mousePos.yFX <= body().positionFX().yFX + (offset + height) / zoomScale;
    }
    
    public boolean isConstraintInSelection(GraphicsWrapper g, FXVector p1, FXVector p2)
    {
        double zoomScale = g.getZoomScale();        
        return (p1.xFX <= body().positionFX().xFX + width / zoomScale &&
                p1.yFX <= body().positionFX().yFX + (offset + height) / zoomScale &&
                p2.xFX >= body().positionFX().xFX &&
                p2.yFX >= body().positionFX().yFX + offset / zoomScale);
    }

    public FXVector makeMove(FXVector mousepos) {
        return new FXVector();
    }

    public int setAction(GraphicsWrapper g, FXVector mousePos, FXVector gridPos) 
    {  
        return Designer.ACTION_NONE;
    }
    
    public void setCoAction(int action, GraphicsWrapper g, FXVector startPos, FXVector gridPos)
    {
    }

    public boolean unsetAction() 
    {
        return false;
    }

    public boolean hasAction() 
    {
        return false;
    }

    public void setDesignMotorParameter(int targetAFX, int targetBFX, int maxForceFX, boolean rotate, boolean isRelative, boolean fixOrthogonal)
    {
    	setTargetAFX(targetAFX);
    	setTargetBFX(targetBFX);
    	setMaxForceFX(maxForceFX);
    	setRotation(rotate);	
    	setIsRelative(isRelative);
    	setFixOrthogonal(fixOrthogonal);
    }

    public int getDesignTargetAFX()
    {
	return getTargetAFX();
    }
    
    public int getDesignTargetBFX()
    {
	return getTargetBFX();
    }  
    
    private Vector listeners = new Vector();
    public void addListener(DesignObjectChangeListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeListener(DesignObjectChangeListener listener)
    {
        listeners.remove(listener);
    }
    
    public void scale(float scale)
    {
        setTargetAFX( (int) (getTargetAFX() * scale) );           
        setTargetBFX( (int) (getTargetBFX() * scale) );       
        setMaxForceFX( (int) (getMaxForceFX() * scale) );   
    }

    public void notifyListeners()
    {
        for(int i = 0; i < listeners.size(); i++)
        {
            ((DesignObjectChangeListener) listeners.elementAt(i)).designObjectChanged(this);
        }
    }
    
    public String toString()
    {
        return "Motor " + "(" + body().getId() + ")";
    }
}
