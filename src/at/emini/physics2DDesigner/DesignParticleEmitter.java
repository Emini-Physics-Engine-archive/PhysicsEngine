package at.emini.physics2DDesigner;

import java.awt.Color;
import java.io.IOException;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.ParticleEmitter;
import at.emini.physics2D.UserData;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;

public class DesignParticleEmitter extends ParticleEmitter implements DesignSelectionObject
{
    protected Color c;
    
    private static int currColor = 0;
    private static final Color defaultColors[] = {
        new Color(150,   0,   0), 
        new Color(100, 100,   0),
        new Color(  0, 150,   0),
        new Color(  0, 120, 120),
        new Color(  0,   0, 150),
        new Color(100,   0, 100) };
    
    private static int emitterSize = 4;
    
    private boolean movePoint1 = false;
    private boolean movePoint2 = false;
    private FXVector lastPosition;
    private boolean actionperformed = false;
    
    private FXVector[] vertices = new FXVector[4];
    
    public DesignParticleEmitter(int particleCount, int creationRateFX,
            int creationRateDeviationFX, short averageLifeTime,
            short averageLifeTimeDeviation, Body emitter,
            FXVector emitPosition1, FXVector emitPosition2, int emitSpeedFX,
            int emitSpeedDeviationFX, int emitAngle2FX,
            int emitAngleDeviation2FX, boolean fixedAxes,
            int elasticityFX, int gravityEffectFX, int dampingFX, int timestepFX)
    {
        super(particleCount, creationRateFX, creationRateDeviationFX, averageLifeTime,
                averageLifeTimeDeviation, emitter, emitPosition1, emitPosition2,
                emitSpeedFX, emitSpeedDeviationFX, emitAngle2FX, emitAngleDeviation2FX,
                fixedAxes, elasticityFX, gravityEffectFX, dampingFX, timestepFX);
        
        mUserData = new StringUserData();
        
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
        for( int i = 0; i < vertices.length; i++)
        {
            vertices[i] = new FXVector();
        }
    }
    
    public DesignParticleEmitter(int particleCount, int xFX, int yFX, Body emitter, int timestepFX)
    {
        super(particleCount, FXUtil.ONE_FX * 20, 0, FXUtil.ONE_FX * 2,  0, 
                emitter, new FXVector(xFX, yFX), new FXVector(xFX, yFX),
                FXUtil.ONE_FX * 10, 0, 
                0, 0,
                false, 
                FXUtil.ONE_FX, FXUtil.ONE_FX, 0, timestepFX);
        
        mUserData = new StringUserData();
        
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
        for( int i = 0; i < vertices.length; i++)
        {
            vertices[i] = new FXVector();
        }
    }
        
    public DesignParticleEmitter(DesignParticleEmitter emitter)
    {
        super(emitter);
        
        mUserData = emitter.mUserData.copy();        
        c = emitter.c;
        for( int i = 0; i < vertices.length; i++)
        {
            vertices[i] = new FXVector();
        }
    }
    
    public DesignParticleEmitter(ParticleEmitter emitter)
    {
        super(emitter);
        
        if (mUserData == null)
        {
            mUserData = new StringUserData();
        }
        
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
        for( int i = 0; i < vertices.length; i++)
        {
            vertices[i] = new FXVector();
        }
    }
    
    public ParticleEmitter copy(Body[] bodyMapping)
    {
        DesignParticleEmitter particleEmitter = new DesignParticleEmitter(this);
        if (getEmitter() != null)
        {
            if (bodyMapping == null)
            {
                particleEmitter.setEmitter(getEmitter());
            }
            else{
                particleEmitter.setEmitter(bodyMapping[getEmitter().getId()]);
            }
        }
        return particleEmitter;
    }

    public Color getColor()
    {
        return c;
    }
    
    public void setColor(Color c)
    {
        this.c = c;
    }
    
    public void drawParticles( GraphicsWrapper g, boolean drawParticleLines)
    {
        g.setColor(c);
        for( int i = 0; i < mMaxParticleCount; i++)
        {
            if (mLife[i]>0)
            {
                if (drawParticleLines)
                {
                    g.drawLine( mXFX[i], mYFX[i], mXPrevFX[i], mYPrevFX[i]);
                    g.drawArc( (int) (mXFX[i] - 2 / g.getZoomScale()), 
                               (int) (mYFX[i] - 2 / g.getZoomScale()), 
                               (int) (4 / g.getZoomScale()), 
                               (int) (4 / g.getZoomScale()), 0, 360);
                }
                else
                {
                    g.drawLine( mXFX[i], mYFX[i], mXFX[i], mYFX[i]);
                }
            }
        }
        
    }
    
    public void saveToFile(MyFileWriter fileWriter, World world)
    {
        try 
        {   
            
            fileWriter.write( getEmitter() != null ? world.bodyIndexOf(getEmitter().getId()) : -1 );
            fileWriter.write( emitAxesFixed() ? 1 : 0);
            
            fileWriter.writeFX( getRelEmitterPos1() );
            fileWriter.writeFX( getRelEmitterPos2() );
            
            fileWriter.writeInt(getEmitAngle2FX());
            fileWriter.writeInt(getEmitAngleDeviation2FX());
            fileWriter.writeInt(getEmitSpeedFX());
            fileWriter.writeInt(getEmitSpeedDeviationFX());
          
            fileWriter.writeInt(getCreationRateFX());
            fileWriter.writeInt(getCreationRateDeviationFX());
            fileWriter.writeInt(getAvgLifeTimeFX());
            fileWriter.writeInt(getAvgLifeTimeDeviationFX());
            fileWriter.writeInt(getMaxParticleCount());
            
            fileWriter.writeInt(getElasticityFX());
            fileWriter.writeInt(getGravityEffectFX());
            fileWriter.writeInt(getDampingFX());
            
            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }                
    }
    
    public static DesignParticleEmitter loadDesignParticleEmitter(PhysicsFileReader reader, World world, UserData userData) 
    {
        ParticleEmitter emitter = ParticleEmitter.loadParticleEmitter(reader, world, userData);

        return new DesignParticleEmitter(emitter);
    }
    
    public FXVector getAbsolutePoint(FXVector relativePoint)
    {
        if (getEmitter() == null)
        {
            return new FXVector(relativePoint);
        }
        else
        {
            if (emitAxesFixed())
            {
                FXVector absoluteVector = new FXVector(relativePoint);
                absoluteVector.add(getEmitter().positionFX());
                return absoluteVector;
            }
            else
            {   
                return getEmitter().getAbsoluePoint(relativePoint);
            }
        }        
    }
    
    public FXVector getRelativePoint(FXVector absolutePoint)
    {
        if (getEmitter() == null)
        {
            return new FXVector(absolutePoint);
        }
        else
        {
            if (emitAxesFixed())
            {
                FXVector relativePoint = new FXVector(absolutePoint);
                relativePoint.subtract(getEmitter().positionFX());
                return relativePoint;
            }
            else
            {   
                return getEmitter().getRelativePoint(absolutePoint);
            }
        }        
    }
    public boolean pointInObject(GraphicsWrapper g, FXVector mousePos)
    {
        FXVector pos1 = getAbsolutePoint(getRelEmitterPos1());
        FXVector pos2 = getAbsolutePoint(getRelEmitterPos2());
        if (pos1.equals(pos2)) 
        {
            return false;
        }
        
        return mousePos.distanceFX(pos1, pos2) < emitterSize / g.getZoomScale();
    }
    
    public boolean canMovePoint(GraphicsWrapper g, FXVector point, FXVector mousePos)
    {
        double zoomScale = g.getZoomScale();
        FXVector tmp = new FXVector(point);
        tmp.subtract(mousePos); 
        return tmp.lengthFX() < emitterSize / zoomScale;
    }

    public void drawInteractives(GraphicsWrapper g, Color color, Color c2)
    {
        FXVector absolute1 = getAbsolutePoint(getRelEmitterPos1());
        FXVector absolute2 = getAbsolutePoint(getRelEmitterPos2());
        double zoomScale = g.getZoomScale();
        
        double angle = Math.atan2(absolute1.xAsFloat() - absolute2.xAsFloat(), 
                                  absolute1.yAsFloat() - absolute2.yAsFloat());
        int startAngle = (int) (angle / Math.PI * 180); //#FX2F int startAngle = (int) (angle / Math.PI * 180);
        
        g.setColor( movePoint1 ? Color.red : color );
        g.drawArc((int) (absolute1.xFX - emitterSize / zoomScale), 
                  (int) (absolute1.yFX - emitterSize / zoomScale), 
                  (int) (emitterSize * 2 / zoomScale), 
                  (int) (emitterSize * 2 / zoomScale), startAngle + 180, movePoint1 && ! movePoint2 ? 360 : 180);
                
        g.setColor( movePoint2 ? Color.red : color );
        g.drawArc((int) (absolute2.xFX - emitterSize / zoomScale), 
                  (int) (absolute2.yFX - emitterSize / zoomScale), 
                  (int) (emitterSize * 2 / zoomScale), 
                  (int) (emitterSize * 2 / zoomScale), startAngle, movePoint2 && ! movePoint1 ? 360 : 180);
        
        FXMatrix diffRot = FXMatrix.createRotationMatrix(FXUtil.wrapAngleFX(90 - (int) (angle / Math.PI * FXUtil.PI_2FX)) );    //#FX2F
        //#FX2F FXMatrix diffRot = FXMatrix.createRotationMatrix(FXUtil.wrapAngleFX(90 - (float) (angle / Math.PI)) );
        FXVector diff = diffRot.mult(new FXVector((int) (emitterSize / zoomScale), 0)); //#FX2F FXVector diff = diffRot.mult(new FXVector((int) (emitterSize / zoomScale), 0));
        
        vertices[0].assign(absolute1); 
        vertices[0].add(diff); 
        vertices[1].assign(absolute2);
        vertices[1].add(diff); 
        vertices[2].assign(absolute2);
        vertices[2].subtract(diff); 
        vertices[3].assign(absolute1);
        vertices[3].subtract(diff); 
        
        g.setColor( movePoint2 && movePoint1 ? Color.red : color );
        g.drawPolygon( GraphicsWrapper.createPolygon(vertices, 0, vertices.length) );
        /*g.drawLine((int) (absolute1.xFX + diff.xFX), 
                (int) (absolute1.yFX + diff.yFX), 
                (int) (absolute2.xFX + diff.xFX), 
                (int) (absolute2.yFX + diff.yFX)); 

        g.drawLine((int) (absolute1.xFX - diff.xFX), 
                (int) (absolute1.yFX - diff.yFX), 
                (int) (absolute2.xFX - diff.xFX), 
                (int) (absolute2.yFX - diff.yFX)); */

    }

    public void drawObject(GraphicsWrapper g, Color c, Color c2, boolean drawFull)
    {
        if (drawFull)
        {
            drawInteractives(g, c, c2);
                if (c2 != null)
            {
                if ( getEmitter() instanceof DesignBody)
                {
                    ((DesignBody) getEmitter()).drawObject(g, c2, c2, drawFull);
                }            
            }
        }
    }

    public int getAction(GraphicsWrapper g, FXVector mousePos)
    {
        if (canMovePoint(g, getAbsolutePoint(getRelEmitterPos1()), mousePos) || 
            canMovePoint(g, getAbsolutePoint(getRelEmitterPos2()), mousePos) || 
            pointInObject(g, mousePos))
        {
            return Designer.ACTION_MOVE;
        }
        return -1;
    }

    public boolean hasAction()
    {        
        return movePoint1 || movePoint2;
    }

    public FXVector makeMove(FXVector newPos)
    {
        if (movePoint1)
        {
            FXVector diff = new FXVector(newPos);
            diff.subtract(lastPosition); 
            FXVector absolutePoint1 = getAbsolutePoint(getRelEmitterPos1());
            absolutePoint1.add(diff);
            
            setRelEmitterPos1(getRelativePoint(absolutePoint1));
        }
        if (movePoint2)
        {
            FXVector diff = new FXVector(newPos);
            diff.subtract(lastPosition);            
            FXVector absolutePoint2 = getAbsolutePoint(getRelEmitterPos2());
            absolutePoint2.add(diff);
            
            setRelEmitterPos2(getRelativePoint(absolutePoint2));
        }
        if (movePoint1 || movePoint2)
        {
            lastPosition = newPos;
        }
        
        notifyListeners();
        actionperformed = true;
        return new FXVector();
    }

    public int setAction(GraphicsWrapper g, FXVector mousePos, FXVector gridPos)
    {
        if (movePoint1 || movePoint2)
        {
            return Designer.ACTION_MOVE;
        }
        
        if( canMovePoint(g, getAbsolutePoint(getRelEmitterPos1()), mousePos) )
        {
            movePoint1 = true;
            setRelEmitterPos1(getRelativePoint(gridPos));
            lastPosition = gridPos;
            return Designer.ACTION_MOVE;
        }
        if( canMovePoint(g, getAbsolutePoint(getRelEmitterPos2()), mousePos) )
        {
            movePoint2 = true;
            setRelEmitterPos2(getRelativePoint(gridPos));
            lastPosition = gridPos;
            return Designer.ACTION_MOVE;
        }
        
        if ( pointInObject(g, mousePos))
        {
            movePoint1 = true;
            movePoint2 = true;
            lastPosition = gridPos;
            return Designer.ACTION_MOVE;
        }
        
        return Designer.ACTION_NONE;
    }

    public void setCoAction(int action, GraphicsWrapper g, FXVector startPos,
            FXVector gridPos)
    {
        if (action != Designer.ACTION_MOVE || hasAction())
        {
            return;
        }
        
        if (getEmitter() == null)   //otherwise it is moved with the bodies
        {
            movePoint1 = true;
            movePoint2 = true;
                    
            FXVector absolutePoint1 = getAbsolutePoint(getRelEmitterPos1());
            absolutePoint1.add(gridPos);
            absolutePoint1.subtract(startPos);
            FXVector absolutePoint2 = getAbsolutePoint(getRelEmitterPos2());
            absolutePoint2.add(gridPos);
            absolutePoint2.subtract(startPos);
            setRelEmitterPos1(getRelativePoint(absolutePoint1));
            setRelEmitterPos2(getRelativePoint(absolutePoint2));

            lastPosition = gridPos;
            notifyListeners();
        }        
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
        return "Particle Emitter " + (getEmitter() != null ? ("(body " + getEmitter().getId() + ")") : "(no body)");
    }
}
