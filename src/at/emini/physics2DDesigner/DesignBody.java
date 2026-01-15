package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.MultiShape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.UserData;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;


public class DesignBody extends Body implements DesignSelectionObject 
{

    private int moveRadius = 10;// << FXUtil.DECIMAL;
    private int rotRadius = 6; // << FXUtil.DECIMAL;
    private int rotDistance = 40; // << FXUtil.DECIMAL;
    private int vertexRadius = 4; // << FXUtil.DECIMAL;
    
    public boolean move = false;
    public boolean rotate = false;
    public int moveVertex = -1;
    private boolean actionperformed = false;
    
    private FXVector lastPosition;
    private int startRotation2FX;
    
    protected Color c;
    
    private static int currColor = 0;
    private static final Color defaultColor = new Color(200, 200, 200, 20);
    
    private static Color lightGreen = new Color(200,255,200); 
    
    public DesignBody(int xFX, int yFX, Shape shape, boolean dynamic) {
        super(0, 0, shape, dynamic);
        mPositionFX.xFX = xFX;
        mPositionFX.yFX = yFX;
        setRotation2FX(mRotation2FX);
        
        c = defaultColor;        
        if (shape instanceof DesignShapeStd)
        {
            c = ((DesignShape) shape).getColor();
        }
        
        mUserData = new StringUserData();
        calculateAABB(0);
    }
    
    public DesignBody(Body b) 
    {
        super(b);
        
        c = defaultColor; 
        if (mShape instanceof DesignShapeStd)
        {
            c = ((DesignShape) mShape).getColor();
        }
        
        if (mUserData == null)
        {
            mUserData = new StringUserData();
        }
        calculateAABB(0);
        
    }
    
    public Body copy()
    {
        DesignBody copy = new DesignBody(this);        
        copy.c = c;
        
        return copy;
    }
       
    
    public static DesignBody convert(Body body)
    {
        DesignBody b = new DesignBody(body);
        b.mId = body.getId();
        return b;
    }
    
    public void setId(int i)
    {
        mId = i;
    }
    
    public void recalcShapeInternals()
    {
        initShapeInternals();
    }
    
    
    public void drawObject(GraphicsWrapper g, Color fillColor, Color c2, boolean drawFull)
    {
        fillBody(g, fillColor);
    }
    
    public void drawInteractives(GraphicsWrapper g, Color color, Color c2)
    {   
    	fillBody(g, color);
    	double currentZoomScale = g.getZoomScale();
        
        g.setColor( move ? Color.red : Color.green);
        g.drawArc( (int) (mPositionFX.xFX - moveRadius / currentZoomScale), 
                   (int) (mPositionFX.yFX - moveRadius / currentZoomScale), 
                   (int) (moveRadius * 2 / currentZoomScale),
                   (int) (moveRadius * 2 / currentZoomScale), 0, 360);
        
        g.setColor( rotate ? Color.red : Color.green);
        FXVector rotationCenter = getRotationCenter(currentZoomScale);
        
        g.drawArc((int) (rotationCenter.xFX - rotRadius / currentZoomScale),
                  (int) (rotationCenter.yFX - rotRadius / currentZoomScale), 
                  (int) (rotRadius * 2 / currentZoomScale), 
                  (int) (rotRadius * 2 / currentZoomScale), 0, 360);
        
        g.setColor( lightGreen);
        
        g.drawLine((int) (mPositionFX.xFX), (int) (mPositionFX.yFX),
                   (int) (rotationCenter.xFX), (int) (rotationCenter.yFX));
            
    }
    
    static FXVector tmp = new FXVector();
    static FXVector tmp2 = new FXVector();
    int[] startIndices;
    int[] defaultStartIndices = {0, 1};
    public void fillBody(GraphicsWrapper g, Color fillColor)
    {
        Shape s = shape();
        int shapeCount = 1;
        FXVector[] positions = getVertices();
                
        if (s instanceof DesignMultiShape)
        {
            startIndices = ((DesignMultiShape) s).getStartIndices();
            shapeCount = ((DesignMultiShape) s).getShapeCount();
        }
        else
        {
            startIndices = defaultStartIndices;
            startIndices[1] = positions.length;
        }
        
        for( int i = 0; i < shapeCount; i++) 
        {            
            if (startIndices[i + 1] - startIndices[i] == 1)
            {
                int boundingRadiusFX = 0;
                if (mShape instanceof MultiShape)
                {
                    boundingRadiusFX = ((MultiShape)mShape).getShape(i).getBoundingRadiusFX();
                }
                else
                {
                    boundingRadiusFX = mShape.getBoundingRadiusFX();
                }
                
                g.setColor(fillColor);
                g.fillArc(mPositionFX.xFX - boundingRadiusFX, mPositionFX.yFX - boundingRadiusFX, 
                          boundingRadiusFX * 2, boundingRadiusFX * 2, 0, 360);
                
                g.setColor(Color.black);
                g.drawArc(mPositionFX.xFX - boundingRadiusFX, mPositionFX.yFX - boundingRadiusFX, 
                        boundingRadiusFX * 2, boundingRadiusFX * 2, 0, 360);
                
                
                tmp.assignFX(boundingRadiusFX, 0);
                getAbsoluePoint(tmp, tmp2);
                
                g.drawLine(mPositionFX.xFX, mPositionFX.yFX, tmp2.xFX, tmp2.yFX);
                
                continue;
            }
            
            Polygon polygon = GraphicsWrapper.createPolygon(positions, startIndices[i], startIndices[i+1]);
            
            //g.setPaint(new GradientPaint(0, 0, color1, 0, 1, Color.black));
            g.setColor(fillColor);
            g.fillPolygon(polygon);
            
            g.setColor(Color.black);
            g.drawPolygon(polygon);
        }
        
    }
    
    public int getAction(GraphicsWrapper g, FXVector mousePos)
    {
        if (canMove(g, mousePos))
        {
            return Designer.ACTION_MOVE;
        }
        if (canRotate(g, mousePos))
        {
            return Designer.ACTION_ROTATE;
        }
        return -1;
    }
    
    private FXVector getRotationCenter(double currentZoomScale)
    {        
        FXVector rotCenter = new FXVector(new FXVector((int) (rotDistance / (float) currentZoomScale), 0));
        rotCenter = getRotationMatrix().mult(rotCenter);
        rotCenter.add(mPositionFX);
        
        return rotCenter;
    }
    
    public boolean canMove(GraphicsWrapper g, FXVector mousePos)
    {
        if (mShape.getCorners().length == 2)
        {
            getVertices(); //to update them
            FXVector[] vertices = getVertices();
            for( int i = 0; i < vertices.length; i++)
            {
                FXVector tmp = new FXVector(vertices[i]);
                tmp.subtract(mousePos); 
                if ( tmp.lengthFX() < vertexRadius )
                {
                    return true;
                }
            }
            return false;
        }
        FXVector tmp = new FXVector(positionFX());
        tmp.subtract(mousePos); 
        return tmp.lengthFX() < moveRadius / g.getZoomScale();
    }
    
    public boolean canRotate(GraphicsWrapper g, FXVector mousePos)
    {
        double zoomScale = g.getZoomScale();
        FXVector tmp = new FXVector(getRotationCenter(zoomScale));
        tmp.subtract(mousePos); 
        return tmp.lengthFX() < rotRadius / zoomScale;
    }
    
    public synchronized int setAction(GraphicsWrapper g, FXVector startPos, FXVector gridPos)
    {
        if (move || moveVertex > 0)
        {
            return Designer.ACTION_MOVE;
        }
        if (rotate)
        {
            return Designer.ACTION_ROTATE;
        }
        
        
        if (mShape.getCorners().length == 2)
        {
            //this case is obsolete due to new landscape handling
            getVertices(); //to update them
            FXVector[] vertices = getVertices();
            for( int i = 0; i < vertices.length; i++)
            {
                FXVector tmp = new FXVector(vertices[i]);
                tmp.subtract(startPos); 
                if ( tmp.lengthFX() < vertexRadius )
                {
                    moveVertex = i;
                  //correct initial position
                    if ( gridPos.xFX != startPos.xFX || 
                            gridPos.yFX != startPos.yFX)
                    {
                        FXVector diff = new FXVector(vertices[i]);
                        diff.subtract(gridPos);
                        mPositionFX.subtract(diff);
                        setRotation2FX(mRotation2FX); //trigger recalculation of vertices                        
                    }
                    
                    initMovement(gridPos);
                    return Designer.ACTION_MOVE;
                }
            }
        }
        else
        {
            if (canMove(g, startPos))
            {
                move = true;
                if ( gridPos.xFX != startPos.xFX || 
                     gridPos.yFX != startPos.yFX)
                {
                    mPositionFX = gridPos;
                    setRotation2FX(mRotation2FX); //trigger recalculation of vertices
                }
                initMovement(gridPos);                     
                return Designer.ACTION_MOVE;
            }
            
            if (canRotate(g, startPos))
            {
                rotate = true;                
                initMovement(gridPos);
                return Designer.ACTION_ROTATE;
            }
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
        if ( gridPos.xFX != startPos.xFX || 
                gridPos.yFX != startPos.yFX)
        {
            mPositionFX.add(gridPos);
            mPositionFX.subtract(startPos);
            setRotation2FX(mRotation2FX); //trigger recalculation of vertices
        }
        initMovement(gridPos);      
        
    }
    
    
    private void initMovement(FXVector gridPos)
    {
        lastPosition = gridPos;
        startRotation2FX = mRotation2FX;        
    }
    
    public boolean unsetAction()
    {
        boolean returnval = hasAction() && actionperformed;
        move = false;
        rotate = false;
        moveVertex = -1;
        lastPosition = new FXVector();
        actionperformed = false;
        
        return returnval;
    }
     
    public boolean hasAction()
    {
        return move | rotate;        
    }
     
    public synchronized FXVector makeMove(FXVector newPos)
    {
        actionperformed = true;
        if (move)
        {
            FXVector diff = new FXVector(newPos);
            diff.subtract(lastPosition);            
            mPositionFX.add(diff);
            //velocityFX.multFX( 1 << (FXUtil.DECIMAL - 2));
            //velocityFX.add(diff.timesFX((1 << FXUtil.DECIMAL) - (1 << (FXUtil.DECIMAL - 2))));
            lastPosition = newPos;
            setRotation2FX(mRotation2FX);
            
            notifyListeners();
            
            return diff;
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
            
            float angle = (float) Math.acos( projectDist );
            
            FXVector v1T = new FXVector(v1.yFX, - v1.xFX);
            if (v1T.dotFX(v2) > 0)
            {
                angle *= -1;
            }
            
            int tmp2FX = startRotation2FX + (int) ((angle / (float) (Math.PI * 2.0)) * FXUtil.TWO_PI_2FX); 
            setRotation2FX(tmp2FX);
                        
            /*angularVelocity2FX = angularVelocity2FX / 4 - (angularVelocity2FX - tmp2FX) * 3 / 4;
            angularVelocity2FX = (angularVelocity2FX + FXUtil.TWO_PI_2FX) % FXUtil.TWO_PI_2FX;
            if (angularVelocity2FX > FXUtil.PI_2FX)
            {
                angularVelocity2FX = angularVelocity2FX - FXUtil.TWO_PI_2FX ; 
            }*/
            notifyListeners();
        }   
        
        if (moveVertex >= 0)
        {
            FXVector diff = new FXVector(newPos);
            diff.subtract(lastPosition);            
            mShape.getCorners()[moveVertex].add(diff);
            centerShape();
            setRotation2FX(mRotation2FX); //trigger recalculation of vertices and axes
            
            lastPosition = newPos;
            notifyListeners();
        }

        return new FXVector();
    }
    
    private void centerShape()
    {
        if (mShape.getCorners().length == 2)
        {
            FXVector center = new FXVector();
            for( int i = 0; i < mShape.getCorners().length; i++)
            {
                center.add(mShape.getCorners()[i]);
            }
            center.divideBy(mShape.getCorners().length);
            for( int i = 0; i < mShape.getCorners().length; i++)
            {
                mShape.getCorners()[i].subtract(center);
            }
            mPositionFX.add(center);
        }
    }
    
    public boolean isPointInShape(FXVector pos)
    {
        if (pos.xFX < getAABBMinXFX() ||
            pos.xFX > getAABBMaxXFX() ||
            pos.yFX < getAABBMinYFX() ||
            pos.yFX > getAABBMaxYFX() )
        {
            return false;
        }
            
        getAxes(); //to update the axes
        FXVector[] axes = getAxes(); 
        FXVector[] vertices = getVertices();
        for( int i = 0; i < axes.length; i++)
        {
            long refProjectionFX = pos.dotFX(axes[i]);
            long minFX = vertices[0].dotFX(axes[i]);
            long maxFX = minFX;
            for( int j = 1; j < vertices.length; j++)
            {
                long dotFX = vertices[j].dotFX(axes[i]);
                if (minFX > dotFX) minFX = dotFX;
                if (maxFX < dotFX) maxFX = dotFX;
            }
            if (minFX > refProjectionFX || maxFX < refProjectionFX) return false;
        }
        
        return true;
    }
 
    public boolean isShapeInSelection(FXVector p1, FXVector p2)
    {   
        return (p2.xFX >= getAABBMinXFX() &&
                p2.yFX >= getAABBMinYFX() &&
                p1.xFX <= getAABBMaxXFX() &&
                p1.yFX <= getAABBMaxYFX() );
    }
    
    public void saveToFile(File file, Vector shapes)
    {
        try 
        {
            MyFileWriter fileWriter = new MyFileWriter( file );
            saveToFile(fileWriter, shapes);
            fileWriter.close();
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }
    }
    
    public void saveToFile(MyFileWriter fileWriter, Vector shapes)
    {
        try 
        {   
            fileWriter.writeFX( mPositionFX );
            fileWriter.writeFX( mVelocityFX );

            fileWriter.writeInt( mRotation2FX );
            fileWriter.writeInt( mAngularVelocity2FX );
            
            fileWriter.write( shapes.indexOf(mShape) );

            byte flags = 0;
            flags |= isDynamic() ? 1 : 0;
            flags |= canRotate() ? 2 : 0;
            flags |= !isInteracting() ? 4 : 0;
            flags |= !isAffectedByGravity() ? 8 : 0;
            fileWriter.write( flags );
            
            fileWriter.writeInt( getColissionBitFlag() );
            
            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);
            
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }
                
    }

    public static DesignBody loadFromFile(File file, Vector shapes, UserData userData) 
    {
        PhysicsFileReader reader = new PhysicsFileReader(file);
        
        Body body = Body.loadBody(reader, shapes, userData);
                
        return new DesignBody(body);
    }
    
    public static DesignBody loadDesignBody(PhysicsFileReader reader, Vector shapes, UserData userData)
    {
        Body body = Body.loadBody(reader, shapes, userData);        
        return convert(body);
    }

    public void setOrigPoint(FXVector vector) 
    {
        if (rotate || move)
        {
            lastPosition = vector;
        }
    }
    

    public int getColissionBitFlags()
    {
        return getColissionBitFlag();
    }
    
    public void setColissionBitFlags(int bitflags)
    {
		for( int i = 0; i < 32; i++)
		{
		    if ((bitflags & (1 << i)) != 0)
		    {
		    	addCollisionLayer(i);
		    }
		    else
		    {
		    	removeCollisionLayer(i);
		    }
		}
    }
    
    public Color getColor()
    {
        if ( mShape instanceof DesignShapeStd)
        {
            return ((DesignShape) mShape).getColor();
        }
        return c;
    }
    
    public void setColor(Color c)
    {
        this.c = c;
        if ( mShape instanceof DesignShapeStd)
        {
            ((DesignShape) mShape).setColor(c);
        }
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
        calculateAABB(0);
        for(int i = 0; i < listeners.size(); i++)
        {
            ((DesignObjectChangeListener) listeners.elementAt(i)).designObjectChanged(this);
        }
    }
    
    public String toString()
    {
        return "Body " + getId();
    }
   
}