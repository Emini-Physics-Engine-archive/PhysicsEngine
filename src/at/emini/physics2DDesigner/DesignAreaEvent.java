package at.emini.physics2DDesigner;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.Event;
import at.emini.physics2D.Shape;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXVector;

public class DesignAreaEvent extends DesignPhysicsEvent
{
    private static final int interactiveDistance = 5;

    private FXVector ul = new FXVector();
    private FXVector ur = new FXVector();
    private FXVector bl = new FXVector();
    private FXVector br = new FXVector();

    private FXVector lastPos = new FXVector();
    private int currentAction = 0;
    private boolean actionperformed = false;

    private static final int MOVE_UPPER = 1;
    private static final int MOVE_RIGHT = 2;
    private static final int MOVE_LOWER = 3;
    private static final int MOVE_LEFT = 4;
    private static final int MOVE = 5;

    public DesignAreaEvent(Body bodyFilter, Shape shapeFilter,
            int target1FX, int target2FX, int target3FX, int target4FX)
    {
        super(bodyFilter, shapeFilter, null, Event.TYPE_BODY_POSITION,
                target1FX, target2FX, target3FX, target4FX, null);
        setTargets(getTargetAFX(), getTargetBFX(), getTargetCFX(), getTargetDFX());
    }

    //Copy constructor
    protected DesignAreaEvent(Event e, Body[] bodyMapping)
    {
        super(e, bodyMapping);
        setTargets(getTargetAFX(), getTargetBFX(), getTargetCFX(), getTargetDFX());
    }

    //Copy constructor
    protected DesignAreaEvent(DesignAreaEvent e, Body[] bodyMapping)
    {
        super(e, bodyMapping);
        setTargets(getTargetAFX(), getTargetBFX(), getTargetCFX(), getTargetDFX());
    }

    protected DesignAreaEvent()
    {
        super(Event.TYPE_BODY_POSITION);
    }

    public void setTargets(int ulXFX, int ulYFX, int brXFX, int brYFX)
    {
        ul.assignFX(ulXFX, ulYFX);
        ur.assignFX(brXFX, ulYFX);
        bl.assignFX(ulXFX, brYFX);
        br.assignFX(brXFX, brYFX);
        super.setTargetsFX(ulXFX, ulYFX, brXFX, brYFX);
        notifyListeners();
    }

    public int getTargetAFX()
    {
        return targetAFX();
    }

    public int getTargetBFX()
    {
        return targetBFX();
    }

    public int getTargetCFX()
    {
        return targetCFX();
    }

    public int getTargetDFX()
    {
        return targetDFX();
    }


    public void scale(double scale)
    {
        setTargets(
                (int) (getTargetAFX() * scale),     //#FX2F (int) (getTargetAFX() * scale),
                (int) (getTargetBFX() * scale),     //#FX2F (int) (getTargetBFX() * scale),
                (int) (getTargetCFX() * scale),     //#FX2F (int) (getTargetCFX() * scale),
                (int) (getTargetDFX() * scale));    //#FX2F (int) (getTargetDFX() * scale));
    }

    public void saveToFile(File file, World world)
    {
        try
        {
            MyFileWriter fileWriter = new MyFileWriter( file );
            saveToFile(fileWriter, world);
            fileWriter.close();
        }
        catch( IOException e)
        {
            System.out.print("Error while writing file!\n");
        }
    }

    public void saveToFile(MyFileWriter fileWriter, World world)
    {
        try
        {
            fileWriter.write( type() );
            fileWriter.write( getTriggerOnce() ? 1 : 0 );

            fileWriter.write( getBodyFilter() != null ? world.bodyIndexOf(getBodyFilter().getId()) : -1 );
            fileWriter.write( getShapeFilter() != null ? getShapeFilter().getId() : -1);
            fileWriter.write( world.indexOf(getConstraintFilter()) );
            fileWriter.writeInt( targetAFX() );
            fileWriter.writeInt( targetBFX() );
            fileWriter.writeInt( targetCFX() );
            fileWriter.writeInt( targetDFX() );

            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);

        }
        catch( IOException e)
        {
            System.out.print("Error while writing file!\n");
        }
    }

    public void drawInteractives(GraphicsWrapper g, Color color, Color c2)
    {
        if (visible)
        {
            g.setColor(Color.black);
            g.drawRect(getTargetAFX(), getTargetBFX(), getTargetCFX() - getTargetAFX(), getTargetDFX() - getTargetBFX());
        }
    }

    public void drawObject(GraphicsWrapper g, Color color, Color c2, boolean full)
    {
        if (visible && full)
        {
            g.setColor(c);
            g.fillRect(getTargetAFX(), getTargetBFX(), getTargetCFX() - getTargetAFX(), getTargetDFX() - getTargetBFX());
        }
    }

    public int getAction(GraphicsWrapper g, FXVector mousepos)
    {
        if (visible)
        {
            double zoomScale = g.getZoomScale();
            if ( mousepos.distanceFX(ul, ur) < interactiveDistance / zoomScale ||
                 mousepos.distanceFX(bl, br) < interactiveDistance / zoomScale)
                return Designer.ACTION_RESIZE_VERTICAL;
            if ( mousepos.distanceFX(ul, bl) < interactiveDistance / zoomScale ||
                 mousepos.distanceFX(ur, br) < interactiveDistance / zoomScale)
                return Designer.ACTION_RESIZE_HORIZONTAL;
            if ( ul.xFX < mousepos.xFX && ul.yFX < mousepos.yFX &&
                 br.xFX > mousepos.xFX && br.yFX > mousepos.yFX )
               return Designer.ACTION_MOVE;
        }
        return Designer.ACTION_NONE;
    }

    public boolean hasAction()
    {
        return currentAction > 0;
    }

    public FXVector makeMove(FXVector newPos)
    {
        switch (currentAction)
        {
        case MOVE_UPPER:
            setTargets(getTargetAFX(), newPos.yFX, getTargetCFX(), getTargetDFX());
            break;
        case MOVE_LOWER:
            setTargets(getTargetAFX(), getTargetBFX(), getTargetCFX(), newPos.yFX);
            break;
        case MOVE_LEFT:
            setTargets(newPos.xFX, getTargetBFX(), getTargetCFX(), getTargetDFX());
            break;
        case MOVE_RIGHT:
            setTargets(getTargetAFX(), getTargetBFX(), newPos.xFX, getTargetDFX());
            break;
        case MOVE:
            FXVector diff = new FXVector();
            diff.assignDiff(lastPos, newPos);
            setTargets(getTargetAFX() - diff.xFX, getTargetBFX() - diff.yFX,
                       getTargetCFX() - diff.xFX, getTargetDFX() - diff.yFX);

            break;
        default: break;
        }

        actionperformed = true;
        lastPos = newPos;
        return null;
    }

    public int setAction(GraphicsWrapper g, FXVector mousepos, FXVector gridPos)
    {
        int action = Designer.ACTION_NONE;
        if (visible)
        {
            double zoomScale = g.getZoomScale();
            if ( mousepos.distanceFX(ul, ur) < interactiveDistance / zoomScale)
            {
                currentAction = MOVE_UPPER;
                action = Designer.ACTION_RESIZE_VERTICAL;
            }
            else if ( mousepos.distanceFX(bl, br) < interactiveDistance / zoomScale)
            {
                currentAction = MOVE_LOWER;
                action = Designer.ACTION_RESIZE_VERTICAL;
            }
            else if ( mousepos.distanceFX(ul, bl) < interactiveDistance / zoomScale  )
            {
                currentAction = MOVE_LEFT;
                action = Designer.ACTION_RESIZE_HORIZONTAL;
            }
            else if ( mousepos.distanceFX(ur, br) < interactiveDistance / zoomScale )
            {
                currentAction = MOVE_RIGHT;
                action = Designer.ACTION_RESIZE_HORIZONTAL;
            }
            else if ( ul.xFX < mousepos.xFX && ul.yFX < mousepos.yFX &&
                      br.xFX > mousepos.xFX && br.yFX > mousepos.yFX )
            {
                currentAction = MOVE;
                action = Designer.ACTION_MOVE;
            }

            if (action >= 0)
            {
                lastPos = gridPos;
            }
        }
        return action;
    }

    public void setCoAction(int action, GraphicsWrapper g, FXVector startPos,
            FXVector gridPos)
    {
        //no coactions here
        return;
    }

    public boolean unsetAction()
    {
        boolean rv = currentAction != 0 && actionperformed;
        currentAction = 0;
        actionperformed = false;
        return rv;
    }

    public String toString()
    {
        return "Area Event " + getIdentifier();
    }
}
