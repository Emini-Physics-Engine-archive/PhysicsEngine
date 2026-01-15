package at.emini.physics2DVisualTest;

import java.awt.Color;
import java.awt.Graphics;

import at.emini.physics2D.Body;
import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class InteractiveBody extends Body {

    private int moveRadius = 10;
    private int rotRadius = 7;
    private int rotDistance = 40;

    public boolean move = false;
    public boolean rotate = false;

    private FXVector lastPosition;
    private int startRotation2FX;

    protected Color c;

    private static int currColor = 0;
    private static final Color defaultColors[] = {
    new Color(80,  0,  0),
    new Color(80, 80,  0),
    new Color( 0, 80,  0),
    new Color( 0, 80, 80),
    new Color( 0,  0, 80),
    new Color(80,  0, 80) };

    public InteractiveBody(int x, int y, Shape shape, boolean dynamic) {
        super(x, y, shape,  dynamic);
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    public InteractiveBody(int x, int y, Shape shape, boolean dynamic, Color c) {
        super(x, y, shape,  dynamic);
        this.c = c;
    }

    public InteractiveBody(Body b) {
        super(b);
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    public Body copy()
    {
        InteractiveBody copy = new InteractiveBody(this);
        copy.c = c;

        return copy;
    }

    public void setVelocityFX(FXVector velocity)
    {
    this.mVelocityFX = velocity;
    }


    public void drawInteractives(Graphics g, double zoom)
    {
        if (move)
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.green);
        }
        g.drawArc((int) (mPositionFX.xAsInt() * zoom) - moveRadius, (int) (mPositionFX.yAsFloat()*zoom) - moveRadius, moveRadius * 2, moveRadius * 2, 0, 360);

        if (rotate)
        {
            g.setColor(Color.red);
        }
        else
        {
            g.setColor(Color.green);
        }
        FXVector rotationCenter = getRotationCenter();
        g.drawArc((int) (rotationCenter.xAsFloat() * zoom) - rotRadius, (int) (rotationCenter.yAsFloat() * zoom) - rotRadius, rotRadius * 2, rotRadius * 2, 0, 360);

    }

    private FXVector getRotationCenter()
    {
        FXMatrix rotationMatrix = FXMatrix.createRotationMatrix(mRotation2FX);
        FXVector rotCenter = new FXVector(new FXVector(rotDistance << FXUtil.DECIMAL, 0));
        rotCenter = rotationMatrix.mult(rotCenter);
        rotCenter.add(mPositionFX);

        return rotCenter;
    }

    public boolean canMove(FXVector mousePos)
    {
        FXVector tmp = new FXVector(positionFX());
        tmp.subtract(mousePos);
        return tmp.lengthSquare() < moveRadius * moveRadius;
    }

    public boolean canRotate(FXVector mousePos)
    {
        FXVector tmp = new FXVector(getRotationCenter());
        tmp.subtract(mousePos);
        return tmp.lengthSquare() < rotRadius * rotRadius;
    }

    public synchronized void setAction(FXVector startPos)
    {
        if (canMove(startPos))
        {
            move = true;
            lastPosition = startPos;

            newPosFX.assign(mPositionFX);
            newVelocityFX.assign(mVelocityFX);
            startRotation2FX = mRotation2FX;
            newRotation2FX = mRotation2FX;

            return;
        }

        if (canRotate(startPos))
        {
            rotate = true;
            lastPosition = startPos;
            startRotation2FX = mRotation2FX;

            newPosFX.assign(mPositionFX);
            newVelocityFX.assign(mVelocityFX);
            newRotation2FX = mRotation2FX;
            newRotationSpeed2FX = mAngularVelocity2FX;

            return;
        }
    }

    public void unsetAction()
    {
        move = false;
        rotate = false;
        lastPosition = new FXVector();
    }


    protected FXVector newPosFX = new FXVector();
    protected FXVector newVelocityFX = new FXVector();
    protected int newRotation2FX = 0;
    protected int newRotationSpeed2FX = 0;
    protected boolean manualMovePending = false;
    public synchronized void makeMove(FXVector newPos)
    {
        manualMovePending = true;
        if (move)
        {
            FXVector diff = new FXVector(newPos);
            diff.subtract(lastPosition);
            newPosFX.add(diff);
            newVelocityFX.multFX( 1 << (FXUtil.DECIMAL - 2));
            newVelocityFX.add(diff.timesFX((1 << FXUtil.DECIMAL) - (1 << (FXUtil.DECIMAL - 2))));
            lastPosition = newPos;
        }

        if (rotate)
        {
            FXVector v1 = new FXVector(mPositionFX);
            v1.subtract(lastPosition);
            FXVector v2 = new FXVector(mPositionFX);
            v2.subtract(newPos);

            double projectDist = (double) v1.dotFX(v2) / (1 << FXUtil.DECIMAL);
            double dist1 = Math.sqrt(v1.lengthSquare());
            double dist2 = Math.sqrt(v2.lengthSquare());
            projectDist /= (dist1 * dist2);

            double angle = Math.acos( projectDist );

            FXVector v1T = new FXVector(v1.yFX, - v1.xFX);
            if (v1T.dotFX(v2) > 0)
            {
                angle *= -1;
            }

            int tmp2FX = newRotation2FX;
            newRotation2FX = startRotation2FX + (int) ((angle / (Math.PI * 2.0)) * FXUtil.TWO_PI_2FX);
            newRotation2FX = (newRotation2FX + FXUtil.TWO_PI_2FX) % FXUtil.TWO_PI_2FX;

            newRotationSpeed2FX = newRotationSpeed2FX / 4 - (newRotation2FX - tmp2FX) * 3 / 4;
            newRotationSpeed2FX = (newRotationSpeed2FX + FXUtil.TWO_PI_2FX) % FXUtil.TWO_PI_2FX;
            if (newRotationSpeed2FX > FXUtil.PI_2FX)
            {
                newRotationSpeed2FX = newRotationSpeed2FX - FXUtil.TWO_PI_2FX ;
            }

        }
    }

    public synchronized void executeManual()
    {
        if (manualMovePending)
        {
            mPositionFX.assign(newPosFX);
            mVelocityFX.assign(newVelocityFX);
            setRotation2FX(newRotation2FX);
            mAngularVelocity2FX = newRotationSpeed2FX;
            manualMovePending = false;
        }
    }
}
