package at.emini.physics2DDesigner;

import java.awt.Color;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Event;
import at.emini.physics2D.Shape;
import at.emini.physics2D.UserData;
import at.emini.physics2D.World;
import at.emini.physics2D.util.PhysicsFileReader;

public abstract class DesignPhysicsEvent extends Event implements DesignSelectionObject
{
    protected boolean visible = true;

    protected Color c;
    
    private static int currColor = 0;
    private static final Color defaultColors[] = {
    new Color(120,   0,   0, 80), 
    new Color(120, 120,   0, 80),
    new Color(  0, 120,   0, 80),
    new Color(  0, 120, 120, 80),
    new Color(  0,   0, 120, 80),
    new Color(120,   0, 120, 80) };


    protected DesignPhysicsEvent(Body bodyFilter, Shape shapeFilter, Constraint constraintFilter, int type,
            int target1FX, int target2FX, int target3FX, int target4FX, Object targetObject)
    {
        super(bodyFilter, shapeFilter, constraintFilter, type, target1FX, target2FX, target3FX, target4FX, targetObject);
        mUserData = new StringUserData();
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }
    
    protected DesignPhysicsEvent(Event e, Body[] bodyMapping)
    {    
        super(e, bodyMapping);
        
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
        if (mUserData == null)
        {
            mUserData = new StringUserData();
        }
    }
    
    protected DesignPhysicsEvent(DesignPhysicsEvent e, Body[] bodyMapping)
    {    
        super(e, bodyMapping);
        mUserData = e.mUserData.copy();
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;        
    }
    
    protected DesignPhysicsEvent(int type)
    {
        super(type);
        mUserData = new StringUserData();
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
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
    
    public static DesignPhysicsEvent loadDesignEvent(PhysicsFileReader reader, World world, UserData userData)
    {
        Event event = Event.loadEvent(reader, world, userData);

        //decide depending on event type what to load
        DesignPhysicsEvent areaEvent = new DesignAreaEvent(event, null);
        
        return areaEvent;
    }
    
    
    public abstract void setTargets(int t1FX, int t2FX, int t3FX, int t4FX);
    public abstract int getTargetAFX();
    public abstract int getTargetBFX();
    public abstract int getTargetCFX();
    public abstract int getTargetDFX();
    
    public abstract void scale(double scale);

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
    
    public abstract void saveToFile(MyFileWriter fileWriter, World world);

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }
    
    public boolean isVisible()
    {
       return visible;
    }
    
    public void notifyListeners()
    {
        for(int i = 0; i < listeners.size(); i++)
        {
            ((DesignObjectChangeListener) listeners.elementAt(i)).designObjectChanged(this);
        }
    }
}
