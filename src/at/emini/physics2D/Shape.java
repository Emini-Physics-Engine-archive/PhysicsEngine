package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;    //#NoBasic

/**
 * The Shape class represents the shape and physical properties of a body. <br>
 * The vertices of the shape are given as a convex polygon.
 * A single vertex is interpreted as a circle.  
 * The physical properties are:
 * <ul>
 * <li>friction</li>
 * <li>elasticity</li> 
 * <li>mass (density)</li>
 * <li>inertia</li>
 * </ul>  
 * 
 * @author Alexander Adensamer
 */
public class Shape 
{

    /**
     * Corners in clockwise direction (x to right, y up). 
     * in screen mode (x to right, y down): counterclockwise
     */
    protected FXVector[] mVertices;
    
    /**
     * The unique axes stores the projection vectors.
     * These are all unique side directions. <br>   
     * For example a rectangle has two pairs of perpendicular faces, 
     * that have not to be tested separately. <br>
     * They are stored as 2 dimensional array: [i11,i12,i21,i21,i31,i32,...-1,....]
     * where i11,i12 are the vertices of the first axis, i21,i22 of the 2nd and so on
    **/
    int[] mUniqueAxesIndices;
    
    /**
     * Number of unique axes. 
     */
    int mUniqueAxesIndicesCount = 0;

    /**
     * Bounding radius of the shape. <br>
     * The radius of the smallest possible circle around the center
     * covering the complete shape. 
     */
    int mBoundingRadius;
    
    /**
     * Bounding radius of the shape (FX). <br>
     * @fx
     */
    int mBoundingRadiusFX;
    
    /**
     * Smallest dimension (FX)
     * @fx
     */
    int mMinSizeFX;
    
    /**
     * Largest dimension (FX)
     * @fx
     */
    int mMaxSizeFX;
        
    /**
     * The area of the shape (FX). 
     * @fx
     */
    long mAreaFX = 0;
    
    /**
     * Elasticity factor (FX) for bodies with this shape. <br>
     * 0: no elasticity (all energy is lost during a collision). <br>
     * 1: full elasticity (all energy is conserved during a collision). <br>
     * For each collision, the product of both involved elasticities is calculated.
     * and used for the restitution part of the momentum 
     * @fx
     */
    protected int mElasticityFX = 0; //FXUtil.ONE_FX * 3 / 8;
    
    
    /**
     * Friction factor (FX) for bodies with this shape. <br>
     * 0: no friction (tangential momentum is conserved). <br>
     * 1: full friction (tangential momentum is removed completely). <br>
     * For each collision, the product of both involved frictions is calculated.
     * and used for the tangential part of the momentum 
     * @fx
     */
    protected int mFrictionFX = FXUtil.ONE_FX * 2 / 8;
    
    /**
     * Mass (FX) of the shape. 
     * @fx
     */
    int mMassFX = 1 << FXUtil.DECIMAL;
    /**
     * Inverse mass (2FX).
     * @fx
     */
    long mInvMass2FX = 1 << FXUtil.DECIMAL2;
    /**
     * Inertia (FX). 
     * @fx
     */
    long mInertiaFX = 1 << FXUtil.DECIMAL;
    /**
     * Inverse Inertia (2FX).
     * @fx
     */
    long mInvInertia2FX = 1 << FXUtil.DECIMAL2;
    
    /**
     * Unique identifier used by the shape set for identification. 
     */
    int mId = -1;
    
    /**
     * User data
     */
    protected UserData mUserData = null;

    /**
     * Centroid of the shape.
     * Position of the center of mass. This is used for the inertia calculation. 
     */
    protected FXVector mCcentroid = new FXVector();
    
    /**
     * Infinity mass (FX). 
     * @fx
     */
    public static final int MAX_MASS_FX = (FXUtil.ONE_FX << FXUtil.DECIMAL) + 1;    //#FX2F public static final float MAX_MASS_FX = Float.MAX_VALUE;
        
    /**
     * Creates a rectangle shape.  
     * @param width width of the rectangle.
     * @param height height of the rectangle.
     * @return the rectangle shape.
     */
    public static Shape createRectangle( int width, int height)
    {
        int widthFX = width << FXUtil.DECIMAL;
        int heightFX = height << FXUtil.DECIMAL;
        
        //clockwise - real world (screen - counterclockwise)
        FXVector[] rectCorners = new FXVector[4];
        rectCorners[0] = new FXVector(- widthFX / 2,- heightFX / 2);
        rectCorners[1] = new FXVector(- widthFX / 2,  heightFX / 2);
        rectCorners[2] = new FXVector(  widthFX / 2,  heightFX / 2);
        rectCorners[3] = new FXVector(  widthFX / 2,- heightFX / 2);
        
        Shape rectangle = new Shape(rectCorners);
        return rectangle;
    }
    
    /**
     * Creates a circle shape.  
     * @param radius radius of the circle.
     * @return the circle shape.
     */
    public static Shape createCircle(int radius)
    {
        FXVector[] corners = new FXVector[1];
        corners[0] = new FXVector(0, radius << FXUtil.DECIMAL);
        
        return new Shape(corners);
    }
    
    /**
     * Creates a regular polygon shape. 
     * @param radius radius of the polygon.
     * @param vertices number of vertices (max. {@link World#M_SHAPE_MAX_VERTICES}).
     * @return the polygon shape.
     */
    public static Shape createRegularPolygon( int radius, int vertices)
    {
        //clockwise - real world (screen - counterclockwise)
        FXVector[] corners = new FXVector[vertices];
        
        FXVector baseVector = new FXVector(0, radius << FXUtil.DECIMAL); 
        
        for( int i = 0; i < vertices; i++)
        {
            long angle2FX = ((long) FXUtil.TWO_PI_2FX) * (i * 2 + 1) / (vertices*2);
            FXMatrix rot = FXMatrix.createRotationMatrix((int)angle2FX);
            corners[vertices - 1 - i] = rot.mult(baseVector);
        }
        
        Shape shape = new Shape(corners);
        return shape;
    }
    
    /**
     * Loads a shape from a stream. 
     * @param reader a physics file reader. 
     * @return the loaded shape. 
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static Shape loadShape( PhysicsFileReader reader, UserData userData)
    {
        int version = reader.getVersion();
        if (version <= World.VERSION_2)
        {
            reader.next();  //read the id byte, but is not used anymore
        }
        
        int verticesCnt = reader.next();
        
        FXVector[] corners = new FXVector[verticesCnt];
        for( int i = 0; i < verticesCnt; i++)
        {
            corners[i] = reader.nextVector();
        }        
        
        Shape shape = new Shape(corners);
        shape.setElasticityFX(reader.nextIntFX());
        shape.setFrictionFX(reader.nextIntFX());
        if (version >= World.VERSION_5)
        {
            shape.setMassFX(reader.nextIntFX());
        }
        
        if (reader.getVersion() > World.VERSION_6)
        {
            String userDataString = reader.nextString();
            if (userData != null)
            {
                shape.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_SHAPE);
            }
        }
        
        return shape;
    }
    //#WorldLoadingOFF */
    //#NoBasic */
    
    /**
     * Constructor.
     * Creates a shape with the vectors (FX) for the corners centered around the origin.
     * The polygon formed by the points must be convex.
     * If the corners are not centered around the origin (0,0),
     * the body will behave like having a non-uniform density distribution. 
     * To avoid this the centroid has to centered by the call {@link Shape#correctCentroid()}.
     * @param corners the corners of the polygon
     */
    public Shape(FXVector[] corners)
    {
        if (corners.length > World.M_SHAPE_MAX_VERTICES)
        {
            return;
        }
        this.mVertices = corners;
     
        updateInternals();
        setMass(1);
        //correctCentroid();    //should not be performed automatically for purposeful incorrect centroid
    }
    
    /**
     * Copy Constructor.
     */
    public Shape(Shape other)
    {
        
        mVertices = new FXVector[other.mVertices.length];
        System.arraycopy(other.mVertices, 0, mVertices, 0, mVertices.length);
     
        setElasticityFX(other.mElasticityFX);
        setFrictionFX(other.mFrictionFX);
        
        updateInternals();
        setMassFX(other.mMassFX);
        //correctCentroid();        //should not be performed automatically for purposeful incorrect centroid
        
        if (other.mUserData != null)
        {
            mUserData = other.mUserData.copy();
        }
    }
    
    /**
     * Used for multi shape
     */
    protected Shape()
    {        
    }
    
        
    /**
     * Computes internal values for fast access. <br> 
     * Computes unique axes for optimized collision detection.<br> 
     * Computes bounding radius. <br>
     * Computes min/max sizes. <br>
     * Corrects the centroid (-> places the center of gravity correctly). 
     */
    protected void updateInternals()
    {
        determineUniqueAxes();
        
        mBoundingRadius = 0;
        for( int i = 0; i < mVertices.length; i++)
        {
            FXVector corner = mVertices[i];
            int rad = corner.lengthSquare();
            if (rad > mBoundingRadius * mBoundingRadius)
            {
                mBoundingRadius = corner.lengthFX() >> FXUtil.DECIMAL;   //#FX2F mBoundingRadius = (int) corner.lengthFX(); 
            }
        }
        mBoundingRadiusFX = mBoundingRadius << FXUtil.DECIMAL;
        
        computeAreaFX();
                
        determineMinMaxSize();   
    }
    
    /**
     * Centers the shape.
     * Corrects the position of the corners so that the center of the gravity is the origin.
     */
    public void correctCentroid() 
    {
       if (mVertices.length < 3)
       {
           return;
       }
       //use longs instead of FXVector to avoid overflow for centroid computation
       long centroidxFX = 0;
       long centroidyFX = 0;
       
       for( int i = 0, j = mVertices.length -1; i < mVertices.length; j = i, i++ )
       {
           long crossFX = mVertices[i].crossFX(mVertices[j]);
           centroidxFX += ((crossFX * (long) (mVertices[i].xFX + mVertices[j].xFX)) >> FXUtil.DECIMAL); 
           centroidyFX += ((crossFX * (long) (mVertices[i].yFX + mVertices[j].yFX)) >> FXUtil.DECIMAL);
       }
       
       centroidxFX = (centroidxFX << FXUtil.DECIMAL) / (mAreaFX * 6);
       centroidyFX = (centroidyFX << FXUtil.DECIMAL) / (mAreaFX * 6);
       
       for( int i = 0; i < mVertices.length; i++ )
       {
           mVertices[i].subtract(new FXVector((int)centroidxFX, (int)centroidyFX));
       }
       updateInternals();
    }

    /**
     * Gets the shape vertices. 
     * @return the corners.
     */
    public FXVector[] getCorners() 
    {
        return mVertices;
    }

    /**
     * Gets the square of the bounding radius.
     * @return the square of the bounding radius.  
     */
    public int getBoundingRadiusSquare()
    {
        return mBoundingRadius * mBoundingRadius;
    }

    /**
     * Gets the bounding radius. 
     * @return the bounding radius.  
     */
    protected int getBoundingRadius()
    {
        return mBoundingRadius;
    }    
    
    /**
     * Gets the bounding radius (FX).
     * @fx
     * @return the bounding radius (FX). 
     */
    public int getBoundingRadiusFX()
    {
        return mBoundingRadiusFX;
    }
    
    
    /**
     * Determines the unique axes.
     * Collision detection works with the projections along the unique axes.
     * Similar axes are considered unique - saves performance.
     */
    void determineUniqueAxes()
    {
        if (mVertices.length == 1)
        {
            mUniqueAxesIndices = new int[0];
            mUniqueAxesIndicesCount = 0;
            return;
        }
        mUniqueAxesIndices = new int[mVertices.length * 2];
        Vector uniqueAxes = new Vector();        
        for( int i = 0, j = mVertices.length - 1; i < mVertices.length; j = i, i++)
        {
            FXVector axis = new FXVector(mVertices[i]);
            axis.subtract(mVertices[j]);
            axis.normalize();
            boolean unique = true;
            for( int k = 0; k < uniqueAxes.size(); k++)
            {
                FXVector axisVec = (FXVector)uniqueAxes.elementAt(k);
                if (Math.abs(axis.dotFX(axisVec)) > World.M_SHAPE_UniqueAxesFactorFX)
                {
                    unique = false;
                    break;
                }
            }
            if (unique)
            {
                mUniqueAxesIndices[uniqueAxes.size() * 2] = i;
                mUniqueAxesIndices[uniqueAxes.size() * 2 + 1] = j;
                uniqueAxes.addElement(axis);                  
            }
        }
        
        mUniqueAxesIndicesCount = uniqueAxes.size() * 2;
        for( int i = uniqueAxes.size() * 2; i < mUniqueAxesIndices.length; i++)
        {
            mUniqueAxesIndices[i] = -1;
        }
    }
    
    /**
     * Computes the min and max extend of the shape.
     * Each projection is considered and largest and smallest extend are stored. 
     */
    private final void determineMinMaxSize()
    {
        mMinSizeFX = mMaxSizeFX = mBoundingRadiusFX * 2;
        for( int i = 0; i < mUniqueAxesIndices.length; i+=2)
        {
            if (mUniqueAxesIndices[i] < 0) break;
            FXVector axis = new FXVector(mVertices[mUniqueAxesIndices[i] ]);
            axis.subtract(mVertices[mUniqueAxesIndices[i+1]]);
            axis.normalize();
            
            long dFX = 0;
            long max1FX = 0;
            long min1FX = max1FX = mVertices[0].crossFX(axis);            
            for( int j = 1; j < mVertices.length; j++)
            {
                dFX = mVertices[j].crossFX(axis);
                if (dFX < min1FX) 
                    min1FX = dFX; 
                else if (dFX > max1FX) 
                    max1FX = dFX;                            
            }
            if (mMaxSizeFX < max1FX - min1FX)
            {
                mMaxSizeFX = (int) (max1FX - min1FX);
            }
            if (mMinSizeFX > max1FX - min1FX)
            {
                mMinSizeFX = (int) (max1FX - min1FX);
            }
        }
    }
    
    /**
     * Sets the friction (in percent).
     * @param friction - 0 means no friction, 100 max. friction
     * @see Shape#mFrictionFX
     */
    public void setFriction(int friction)
    {
        this.mFrictionFX = friction * FXUtil.ONE_FX / 100;
    }
    
    /**
     * Sets the friction of the shape (FX). 
     * @fx
     * @param frictionFX - 0 means no friction, ONE_FX max friction
     * @see Shape#mFrictionFX
     */
    public void setFrictionFX(int frictionFX)
    {
        this.mFrictionFX = frictionFX;
    }

    /**
     * Gets the friction (FX). 
     * @fx
     * @return the friction (FX). 
     * @see Shape#mFrictionFX
     */
    public int getFrictionFX()
    {
        return mFrictionFX;
    }

    
    /**
     * Set the elasticity (in percent). 
     * @param elasticity - 0 all energy of the collision is lost (no bounce), 100 full energy conserved (high bounce).
     * @see Shape#mElasticityFX
     */
    public void setElasticity(int elasticity)
    {
        this.mElasticityFX = elasticity * FXUtil.ONE_FX / 100;
    }
    
    /**
     * Set the elasticity (FX). 
     * @fx
     * @param elasticityFX - 0 all energy of the collision is lost (no bounce), ONE_FX full energy conserved (high bounce).
     * @see Shape#mElasticityFX
     */
    public void setElasticityFX(int elasticityFX)
    {
        this.mElasticityFX = elasticityFX;
    }
    
    /**
     * Gets the elasticity.
     * @fx
     * @return elasticity (FX). 
     * @see Shape#mElasticityFX
     */
    public int getElasticityFX()
    {
        return mElasticityFX;
    }
    
    
    /**
     * Sets the mass. 
     * All related values are computed as well: InvMass, Inertia, InvInertia
     * @param mass new mass of the body.
     */
    public final void setMass(int mass)
    {
        setMassFX(mass << FXUtil.DECIMAL);
    }
    
    /**
     * Sets the mass (FX). 
     * All related values are computed as well: InvMass, Inertia, InvInertia
     * @fx  
     * @param massFX new mass of the body (FX). 
     */
    public final void setMassFX(int massFX)
    {
        if (mAreaFX == 0)
        {
            massFX = MAX_MASS_FX;
            mInertiaFX = MAX_MASS_FX;
            mInvMass2FX = 1;
            mInvInertia2FX = 1;
            
            return;
        }
        
        this.mMassFX = massFX;
        mInvMass2FX = (((long) FXUtil.ONE_FX << FXUtil.DECIMAL2)/ massFX);
        
        //safeguard against extremely small and massive bodies
        if (mInvMass2FX == 0)
        {
            mInvMass2FX = 1;
        }
        
        //to recalculate inertia
        computeAreaFX();
    }
    
    /**
     * Gets the mass.  
     * @return the mass.  
     */
    public int getMass()
    {
        return mMassFX >> FXUtil.DECIMAL;    //#FX2F return (int) mMassFX;
    }
    
    /**
     * Gets the mass (FX). 
     * @fx
     * @return the mass (FX). 
     */
    public int getMassFX()
    {
        return mMassFX;
    }
    
    /**
     * Get the shape id. <br>
     * If the shape id is -1, the shape is not registered in the shapeset {@link ShapeSet}. 
     * That can only happen if no body uses it and it was not registered manually. 
     * @return the shape id as used by the shape set
     */
    public int getId()
    {
        return mId;
    }

    /**
     * Gets the area of the shape (FX). 
     * @fx
     * @return the area (FX)
     */
    public long getAreaFX()
    {
        return mAreaFX;
    }
    
    /**
     * Calculates the area and inertia of the shape. 
     */
    void computeAreaFX() 
    {
        mAreaFX = 0;
        mInertiaFX = 0;
        if (mVertices.length == 1)
        {
            mAreaFX = (((long) FXUtil.PI_2FX * (long) FXUtil.multFX(mBoundingRadiusFX, mBoundingRadiusFX) ) >> FXUtil.DECIMAL2) ;
            
            mInertiaFX = (int) (((long) (mMassFX / 2)* (long) mBoundingRadiusFX * (long) mBoundingRadiusFX) >> FXUtil.DECIMAL2);
        }
        else if (mVertices.length > 2)
        {
            FXVector v1, v2;
            for ( int i = 0; i < mVertices.length; i++ ) {
                v1 = mVertices[i];
                v2 = mVertices[(i+1) >= mVertices.length ? 0 : (i+1)];
                
                long tAreaFX = v2.crossFX(v1) ; //Math.abs(v2.crossFX(v1) / 2);
                
                //area
                mAreaFX += tAreaFX / 2;
                
                //inertia
                long ex1FX = v1.xFX, ey1FX = v1.yFX;
                long ex2FX = v2.xFX, ey2FX = v2.yFX;

                long intx2FX = (int) (((ex1FX*ex1FX + ex2FX*ex1FX + ex2FX*ex2FX) / 12) >> FXUtil.DECIMAL);
                long inty2FX = (int) (((ey1FX*ey1FX + ey2FX*ey1FX + ey2FX*ey2FX) / 12) >> FXUtil.DECIMAL);

                mInertiaFX += (tAreaFX * (intx2FX + inty2FX)) >> FXUtil.DECIMAL;
              
            }
            //areaFX = Math.abs(areaFX / 2);
            mInertiaFX = (((mInertiaFX << FXUtil.DECIMAL) / mAreaFX) * mMassFX) >> FXUtil.DECIMAL;
        }      
        
        if (mAreaFX > 0)
        {
            //compute centroid
            long centroidxFX = 0;
            long centroidyFX = 0;
            
            for( int i = 0, j = mVertices.length -1; i < mVertices.length; j = i, i++ )
            {
                long crossFX = mVertices[i].crossFX(mVertices[j]);
                centroidxFX += ((crossFX * (long) (mVertices[i].xFX + mVertices[j].xFX)) >> FXUtil.DECIMAL); 
                centroidyFX += ((crossFX * (long) (mVertices[i].yFX + mVertices[j].yFX)) >> FXUtil.DECIMAL);
            }
            
            centroidxFX = (centroidxFX << FXUtil.DECIMAL) / (mAreaFX * 6);
            centroidyFX = (centroidyFX << FXUtil.DECIMAL) / (mAreaFX * 6);
            
            mCcentroid.assignFX((int)centroidxFX, (int)centroidyFX);
        }
        else
        {
            mCcentroid.assignFX(0,0);
        }
        
        mInertiaFX += (((long) mMassFX * mCcentroid.lengthSquareFX()) >> FXUtil.DECIMAL); 
        
        if (mInertiaFX == 0)
        {
            mInvInertia2FX = (1 << 31) - 1;
        }
        else
        {
            mInvInertia2FX = (((long)FXUtil.ONE_FX << (FXUtil.DECIMAL2 ))/ mInertiaFX);
        }
    }
    
    
    /**
     * Computes rotated and shifted vertices. 
     * @param pos the center position of the translated shape
     * @param rotation the rotation matrix of the translation 
     * @param vertices - the vertices vector, where the vertices in world coordinates are written to
     */
    protected final void getVerticesFX(FXVector pos, FXMatrix rotation, FXVector[] vertices)
    {
        if (vertices.length < mVertices.length)
            return;
        
        for( int i = 0; i < mVertices.length; i++)
        {
            rotation.mult(mVertices[i], vertices[i]);
            vertices[i].add(pos);
        }        
    }
    
    /**
     * Get user data.
     * @return the user data.
     */
    public UserData getUserData()
    {
        return mUserData;
    }

    /**
     * Set User data
     * @param userData the user data
     */
    public void setUserData(UserData userData)
    {
        this.mUserData = userData;
    }
}
