package at.emini.physics2DDesigner;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Joint;
import at.emini.physics2D.UserData;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;

public class DesignJoint extends Joint implements DesignConstraint {


    private boolean body1Fixed = false;
    private boolean body2Fixed = false;

    private boolean move = false;
    private FXVector lastPosition;
    private boolean actionperformed = false;
    
    private static final int jointRadius = 5; // << FXUtil.DECIMAL;

    protected Color color;

    private static int currColor = 0;
    private static final Color defaultColors[] = {
        new Color(40,  0,  0), 
        new Color(40, 40,  0),
        new Color( 0, 40,  0),
        new Color( 0, 40, 40),
        new Color( 0,  0, 40),
        new Color(40,  0, 40) };

    public DesignJoint(Body b1, Body b2, FXVector p1, FXVector p2, boolean fixed) {
        super(b1, b2, p1, p2, fixed);
        color = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
        
        mUserData = new StringUserData();
    }

    public DesignJoint(boolean fixed) {
        super(null, null, null, null, fixed);
        color = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;

        mUserData = new StringUserData();
    }


    public DesignJoint(Joint joint, Body[] bodyMapping) 
    {
        super(joint, bodyMapping);
        color = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
        
        if (mUserData == null)
        {
            mUserData = new StringUserData();
        }
    }
    
    public DesignJoint(DesignJoint joint, Body[] bodyMapping) 
    {
        super(joint, bodyMapping);
        color = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;

        mUserData = joint.mUserData.copy();
    }

    public Constraint copy(Body[] bodyMapping)
    {
        DesignJoint j = new DesignJoint( this, bodyMapping);
        j.color = color;
        return j;
    }

    public Color getColor()
    {
        return color;
    }
    
    public void setColor(Color c)
    {
        this.color = c;
    }
    
    /**
     * @param b1 Body 1
     */
    public void setBody1(Body b1)
    {
        super.setBody1(b1);
        body1Fixed = true;
    }

    /**
     * @param b2 Body 2
     */
    public void setBody2(Body b2)
    {
        super.setBody2(b2);
        body2Fixed = true;
    }

    public boolean Body1Fixed()
    {
        return body1Fixed;
    }

    public boolean bothBodyFixed()
    {
        return body1Fixed && body2Fixed;
    }

    public void drawObject(GraphicsWrapper g, Color c, Color c2, boolean drawFull) 
    {
        if (drawFull)
        {
            drawInteractives(g, c, c2);
            if (c2 != null)
            {
                if ( getBody1() instanceof DesignBody)
                {
                    ((DesignBody) getBody1()).drawObject(g, c2, c2, drawFull);
                }
                if ( getBody2() instanceof DesignBody)
                {
                    ((DesignBody) getBody2()).drawObject(g, c2, c2, drawFull);
                }
            }
        }
    }

    public void drawInteractives(GraphicsWrapper g, Color c, Color c2) 
    {
        g.setColor( move ? Color.red : Color.black );
        FXVector absolute1 = new FXVector(getPoint1());
        FXVector absolute2 = new FXVector(getPoint2());

        if (isFixed())
        {
            g.drawLine((int) (absolute1.xFX - jointRadius / g.getZoomScale()), (int) (absolute1.yFX), (int) (absolute1.xFX + jointRadius / g.getZoomScale()), (int) (absolute1.yFX)); 
            g.drawLine((int) (absolute1.xFX), (int) (absolute1.yFX - jointRadius / g.getZoomScale()), (int) (absolute1.xFX), (int) (absolute1.yFX)); 

            g.drawLine((int) (absolute2.xFX - jointRadius / g.getZoomScale()), (int) (absolute2.yFX), (int) (absolute2.xFX + jointRadius / g.getZoomScale()), (int) (absolute2.yFX)); 
            g.drawLine((int) (absolute2.xFX), (int) (absolute2.yFX + jointRadius / g.getZoomScale()), (int) (absolute2.xFX), (int) (absolute2.yFX));
        }
        else
        {
            g.drawArc((int) (absolute1.xFX - jointRadius / g.getZoomScale()), (int) (absolute1.yFX - jointRadius / g.getZoomScale()), (int)(jointRadius * 2 / g.getZoomScale()), (int) (jointRadius * 2 / g.getZoomScale()), 0, 180);
            g.drawLine((int) (absolute1.xFX), (int) (absolute1.yFX), (int) (absolute1.xFX), (int) (absolute1.yFX)); 

            g.drawArc((int) (absolute2.xFX - jointRadius / g.getZoomScale()), (int) (absolute2.yFX - jointRadius / g.getZoomScale()), (int)(jointRadius * 2 / g.getZoomScale()), (int) (jointRadius * 2 / g.getZoomScale()), 180, 180);
            g.drawLine((int) (absolute2.xFX), (int) (absolute2.yFX), (int) (absolute2.xFX), (int) (absolute2.yFX));
        }
    }

        
    public void drawSelecting(GraphicsWrapper g, FXVector pos) 
    {
        g.setColor( Color.red );
        if (getBody1() != null)
        {
            g.drawLine((int) (getBody1().positionFX().xFX), (int) (getBody1().positionFX().yFX), (int) (pos.xFX), (int) (pos.yFX));
        }
        if (getBody2() != null)
        {
            g.drawLine((int) (getBody2().positionFX().xFX), (int) (getBody2().positionFX().yFX), (int) (pos.xFX), (int) (pos.yFX));
        }
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
            fileWriter.write( Constraint.JOINT );

            fileWriter.write( bodies.indexOf(getBody1()) );
            fileWriter.writeFX( getRawPoint1() );
            fileWriter.write( bodies.indexOf(getBody2()) );
            fileWriter.writeFX( getRawPoint2() );
            fileWriter.write( isFixed() ? 1 : 0 );
            
            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }

    }

    public static Joint loadFromFile(File file, Vector bodies, UserData userData) 
    {
        PhysicsFileReader reader = new PhysicsFileReader(file);

        Joint joint = Joint.loadJoint(reader, bodies, userData);

        return new DesignJoint(joint, null);
    }

    public boolean canMovePoint(GraphicsWrapper g, FXVector point, FXVector mousePos)
    {
        FXVector tmp = new FXVector(point);
        tmp.subtract(mousePos); 
        return tmp.lengthFX() < jointRadius / g.getZoomScale();
    }

    public int getAction(GraphicsWrapper g, FXVector mousePos) 
    {
        if (canMovePoint(g, getPoint1(), mousePos) || canMovePoint(g, getPoint2(), mousePos))
        {
            return Designer.ACTION_MOVE;
        }
        return -1;
    }

    public boolean isPointInConstraint(GraphicsWrapper g, FXVector mousePos)
    {
        return false;   //no extra clickable area
    }

    public boolean isConstraintInSelection(GraphicsWrapper g, FXVector p1, FXVector p2)
    {
        return getPoint1().isInRect(p1, p2) || getPoint2().isInRect(p1, p2);
    }

    public FXVector makeMove(FXVector mousepos) 
    {
        actionperformed = true;
        if (move)
        {
            FXVector diff = new FXVector(mousepos);
            diff.subtract(lastPosition);    
            if (diff.lengthFX() > 0)
            {
                FXVector absolutePoint = getPoint1();            
                absolutePoint.add(diff);
                setFixPoint(absolutePoint);

            }

            lastPosition = new FXVector(mousepos);           
            return diff;
        }
        return new FXVector();
    }

    public int setAction(GraphicsWrapper g, FXVector mousePos, FXVector gridPos) 
    {
        if (move)
        {
            return Designer.ACTION_MOVE;
        }

        if( canMovePoint(g, getPoint1(), mousePos) )
        {
            move = true;
            setFixPoint(gridPos);

            lastPosition = gridPos;
            return Designer.ACTION_MOVE;
        }
        if( canMovePoint(g, getPoint2(), mousePos) )
        {
            move = true;
            setFixPoint(gridPos);

            FXVector diff = new FXVector(gridPos);
            diff.subtract(getPoint2());    
            diff.add(getPoint1());

            lastPosition = diff;
            return Designer.ACTION_MOVE;
        }

        return Designer.ACTION_NONE;
    }

    public void setCoAction(int action, GraphicsWrapper g, FXVector startPos, FXVector gridPos)
    {
        if (action != Designer.ACTION_MOVE || hasAction())
        {
            return;
        }

        move = true;
        FXVector lastPos = new FXVector(getPoint1());
        lastPos.add(gridPos);
        lastPos.subtract(startPos);
        setFixPoint(lastPos);

        lastPosition = gridPos;
    }

    public boolean unsetAction() 
    {
        boolean returnval = hasAction() && actionperformed;
        move = false;
        lastPosition = new FXVector();
        actionperformed = false;
        return returnval;
    }

    public boolean hasAction() 
    {
        return move;
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
        int scaleFX = (int) (scale * FXUtil.ONE_FX);
        this.getRawPoint1().multFX(scaleFX);
        this.getRawPoint2().multFX(scaleFX);
        
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
        return "Joint " + (isFixed() ? "(fixed) " : " ") + "(" + getBody1().getId() + " - " + getBody2().getId() + ")";
    }
}
