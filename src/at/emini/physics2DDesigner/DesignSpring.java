package at.emini.physics2DDesigner;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Spring;
import at.emini.physics2D.UserData;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;

public class DesignSpring extends Spring implements DesignConstraint {


    private boolean body1Fixed = false;
    private boolean body2Fixed = false;

    private boolean movePoint1 = false;
    private boolean movePoint2 = false;
    private FXVector lastPosition;
    private boolean actionperformed = false;

    private static final int jointRadius = 5; // << FXUtil.DECIMAL;
    private static final int selectDistance = 3; // << FXUtil.DECIMAL;

    protected Color color;

    private static int currColor = 0;
    private static final Color defaultColors[] = {
    new Color(140,  0,  0),
    new Color(140, 140,  0),
    new Color( 0, 140,  0),
    new Color( 0, 140, 140),
    new Color( 0,  0, 140),
    new Color(140,  0, 140) };

    public DesignSpring(Body b1, Body b2, FXVector p1, FXVector p2, int distance) {
        super(b1, b2, p1, p2, distance);
        mUserData = new StringUserData();
        color = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    public DesignSpring() {
        super(null, null, null, null, 0);
        mUserData = new StringUserData();
        color = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    protected DesignSpring(Spring spring, Body[] bodyMapping)
    {
        super(spring, bodyMapping);
        color = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
        if (mUserData == null)
        {
            mUserData = new StringUserData();
        }
    }

    protected DesignSpring(DesignSpring spring, Body[] bodyMapping)
    {
        super(spring, bodyMapping);
        mUserData = spring.mUserData.copy();
        color = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    public Constraint copy(Body[] bodyMapping)
    {
        DesignSpring j = new DesignSpring( this, bodyMapping);
        j.color = color;
        return j;
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color c)
    {
        color = c;
    }

    /**
     * Set the first involved body and the (first) pivot of the spring
     * @param b1 Body 1
     * @param point absolute Point of the pivot
     */
    public void setBody1(Body b1, FXVector point)
    {
        setBody1(b1);
        setAbsolutePoint1( point);

        body1Fixed = true;
    }

    /**
     * Set the first involved body and the (second) pivot of the spring
     * @param b2 Body 2
     * @param point absolute Point of the pivot
     */
    public void setBody2(Body b2, FXVector point)
    {
    setBody2(b2);
        setAbsolutePoint2( point);

        body2Fixed = true;
    }

    public void calcDistance()
    {
        super.calcDistance();
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
        drawInteractives(g, c, c2);

        if (drawFull)
        {
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

    public void drawInteractives(GraphicsWrapper g, Color color, Color c2)
    {
        FXVector absolute1 = new FXVector(getPoint1());
        double zoomScale = g.getZoomScale();

        g.setColor( movePoint1 ? Color.red : Color.black );
        g.drawArc((int) (absolute1.xFX - jointRadius / zoomScale),
                  (int) (absolute1.yFX - jointRadius / zoomScale),
                  (int) (jointRadius * 2 / zoomScale),
                  (int) (jointRadius * 2 / zoomScale), 0, 360);

        FXVector absolute2 = new FXVector(getPoint2());

        g.setColor( movePoint2 ? Color.red : Color.black );
        g.drawArc((int) (absolute2.xFX - jointRadius / zoomScale), (int) (absolute2.yFX - jointRadius / zoomScale), (int) (jointRadius * 2 / zoomScale), (int) (jointRadius * 2 / zoomScale), 0, 360);

        g.setColor( color );
        g.drawLine((int) (absolute1.xFX), (int) (absolute1.yFX), (int) (absolute2.xFX), (int) (absolute2.yFX));
    }

    public void drawSelecting(GraphicsWrapper g, FXVector pos)
    {
        g.setColor( Color.red );
        if (getBody1() != null)
        {
            FXVector absolute1 = new FXVector(getPoint1());
            g.drawLine((int) (absolute1.xFX), (int) (absolute1.yFX), (int) (pos.xFX), (int) (pos.yFX));
        }
        if (getBody2() != null)
        {
            FXVector absolute2 = new FXVector(getPoint2());
            g.drawLine((int) (absolute2.xFX), (int) (absolute2.yFX), (int) (pos.xFX), (int) (pos.yFX));
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
            fileWriter.write( Constraint.SPRING );

            fileWriter.write( bodies.indexOf(getBody1()) );
            fileWriter.writeFX( getRawPoint1() );
            fileWriter.write( bodies.indexOf(getBody2()) );
            fileWriter.writeFX( getRawPoint2() );
            fileWriter.writeInt( getDistanceFX() );
            fileWriter.writeInt( mCoefficientFX );

            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);

        }
        catch( IOException e)
        {
            System.out.print("Error while writing file!\n");
        }

    }

    public static DesignSpring loadFromFile(File file, Vector bodies, UserData userData)
    {
        PhysicsFileReader reader = new PhysicsFileReader(file);

        Spring spring = Spring.loadSpring(reader, bodies, userData);

        return new DesignSpring(spring, null);
    }

    public void scale(float scale)
    {
        int scaleFX = (int) (scale * FXUtil.ONE_FX);
        this.getRawPoint1().multFX(scaleFX);
        this.getRawPoint2().multFX(scaleFX);

        calcDistance();
    }

    public boolean canMovePoint(GraphicsWrapper g, FXVector point, FXVector mousePos)
    {
        double zoomScale = g.getZoomScale();
        FXVector tmp = new FXVector(point);
        tmp.subtract(mousePos);
        return tmp.lengthFX() < jointRadius / zoomScale;
    }

    public int getAction(GraphicsWrapper g, FXVector mousePos)
    {
       if (canMovePoint(g, getPoint1(), mousePos) ||
           canMovePoint(g, getPoint2(), mousePos))
       {
           return Designer.ACTION_MOVE;
       }
       return -1;
    }

    public boolean isPointInConstraint(GraphicsWrapper g, FXVector mousePos)
    {
        FXVector p1 = getPoint1();
        FXVector p2 = getPoint2();
        FXVector n = new FXVector(p2);
        n.subtract(p1);
        int abDistFX = n.lengthFX();
        n.normalize();

        return mousePos.distanceFX(p1, p2, n, abDistFX) < selectDistance / g.getZoomScale();
    }

    public boolean isConstraintInSelection(GraphicsWrapper g, FXVector p1, FXVector p2)
    {
        return getPoint1().isInRect(p1, p2) || getPoint2().isInRect(p1, p2);
    }

    public FXVector makeMove(FXVector mousepos)
    {
        if (movePoint1)
        {
            FXVector diff = new FXVector(mousepos);
            diff.subtract(lastPosition);
            FXVector absolutePoint1 = getPoint1();
            absolutePoint1.add(diff);

            setAbsolutePoint1(absolutePoint1);
        }
        if (movePoint2)
        {
            FXVector diff = new FXVector(mousepos);
            diff.subtract(lastPosition);
            FXVector absolutePoint2 = new FXVector(getPoint2());
            absolutePoint2.add(diff);

            setAbsolutePoint2(absolutePoint2);
        }
        if (movePoint1 || movePoint2)
        {
            calcDistance();
            lastPosition = mousepos;
        }

        actionperformed = true;
        return new FXVector();
    }

    public int setAction(GraphicsWrapper g, FXVector mousePos, FXVector gridPos)
    {
        if (movePoint1 || movePoint2)
        {
            return Designer.ACTION_MOVE;
        }

        if( canMovePoint(g, getPoint1(), mousePos) )
        {
            movePoint1 = true;
            setAbsolutePoint1(gridPos);
            lastPosition = gridPos;
            return Designer.ACTION_MOVE;
        }
        if( canMovePoint(g, getPoint2(), mousePos) )
        {
            movePoint2 = true;
            setAbsolutePoint2(gridPos);
            lastPosition = gridPos;
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
        movePoint1 = true;
        movePoint2 = true;

        FXVector absolutePoint1 = getPoint1();
        absolutePoint1.add(gridPos);
        absolutePoint1.subtract(startPos);
        FXVector absolutePoint2 = getPoint2();
        absolutePoint2.add(gridPos);
        absolutePoint2.subtract(startPos);
        setAbsolutePoint1(absolutePoint1);
        setAbsolutePoint2(absolutePoint2);

        lastPosition = gridPos;
    }

    public boolean unsetAction()
    {
        boolean returnval = hasAction() && actionperformed;
        movePoint1 = false;
        movePoint2 = false;
        lastPosition = new FXVector();
        actionperformed = false;
        return returnval;
    }

    public boolean hasAction()
    {
        return movePoint1 | movePoint2;
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

    public void notifyListeners()
    {
        for(int i = 0; i < listeners.size(); i++)
        {
            ((DesignObjectChangeListener) listeners.elementAt(i)).designObjectChanged(this);
        }
    }

    public String toString()
    {
        return "Spring " + "(" + getBody1().getId() + " - " + getBody2().getId() + ")";
    }
}
