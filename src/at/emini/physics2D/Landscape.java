package at.emini.physics2D;

import java.util.Arrays;                            //#NoJ2ME

import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;    //#NoBasic

/**
 * The Landscape class represents a collection of static bodies. 
 * The bodies are represented as lines and do not have to be closed shapes.  
 * They form a landscape for the physical simulation.<br>
 * Using a landscape, large worlds can be created without shape overhead.
 * This is an efficient alternative to static bodies. <br>  
 * Each line segment consists of a starting point, an end point and a face.
 * The face of each segment can be right, left or none. This Indicates how the segment works.
 * <ul>
 * <li>see {@link #FACE_NONE}: the segment is considered a line from both sides</li>
 * <li>see {@link #FACE_LEFT}: the segment is considered to be the left border of a solid, always assuming colliding objects to come from left.</li>
 * <li>see {@link #FACE_RIGHT}: the segment is considered to be the right border of a solid, always assuming colliding objects to come from right.</li>
 * </ul>
 * @author Alexander Adensamer
 *
 */
public class Landscape 
{
    /**
     * A dummy body.
     * It is required for the actual collision resolve
     */
    private Body mBody;  
    
    /**
     * The shape defining the physical properties.
     * Like friction, elasticity, etc. 
     * IT applies to all segments within the landscape. 
     */
    private Shape mShape;
    
    /**
     * The initial segment capacity. 
     */
    private static final int M_MAX_LANDSCAPE_SEGMENTS = 128;
    
    /**
     * Initial capacity for the checked segments
     */
    private static final int M_MAX_CHECK_SEGMENTS = 32;
    
    /**
     * Number of landscape segments.
     */
    protected int mSegmentCount = 0;
    
    /**
     * Start points of the segments.
     */
    protected FXVector[] mStartpoints = new FXVector[M_MAX_LANDSCAPE_SEGMENTS];
    /**
     * End points of the segments.
     */
    protected FXVector[] mEndpoints = new FXVector[M_MAX_LANDSCAPE_SEGMENTS];
    /**
     * Faces of the segments. 
     */
    protected short[] mFaces = new short[M_MAX_LANDSCAPE_SEGMENTS];
    
    
    private int mCurrentIndex = 0;
    private int[] mCurrentOpen = new int[M_MAX_CHECK_SEGMENTS];
    private int mOpenCheckSize = 0;
    private int mOpenCnt = 0;
    
    /**
     * Face type none. 
     * The segment is considered a line from both sides
     */
    public static final short FACE_NONE = 0;
    /**
     * Face type left. 
     * The segment is considered to be the left border of a solid, always assuming colliding objects to come from left.
     */
    public static final short FACE_LEFT = 1;
    /**
     * Face type right. 
     * The segment is considered to be the right border of a solid, always assuming colliding objects to come from right.
     */
    public static final short FACE_RIGHT = 2; 
    
    /**
     * Number of contacts. 
     */
    int mContactCount = 0; //counts the current contacts
    
    /**
     * Array containing the current contacts. 
     */
    Contact[] mContacts = new Contact[World.M_LANDSCAPE_INITIAL_MAX_CONTACTS];
   
    /**
     * Default Constructor. 
     */
    public Landscape()
    {
        this.mShape = new Shape(new FXVector[0]);
        mBody = new Body( 0, 0, mShape, false);
    }
    
    /**
     * Copy constructor. 
     * Creates a deep copy of the landscape. 
     * @param other copy source
     */
    public Landscape(Landscape other)
    {
        mShape = new Shape(new FXVector[0]);
        mBody = new Body( 0, 0, mShape, false);

        if (other == null)
        {
            return;
        }
        mShape.mFrictionFX = other.mShape.mFrictionFX;
        mShape.mElasticityFX = other.mShape.mElasticityFX;
        
        mSegmentCount = other.mSegmentCount;
        mStartpoints = new FXVector[Math.max(M_MAX_LANDSCAPE_SEGMENTS, mSegmentCount)];
        mEndpoints = new FXVector[Math.max(M_MAX_LANDSCAPE_SEGMENTS, mSegmentCount)];
        mFaces = new short[Math.max(M_MAX_LANDSCAPE_SEGMENTS, mSegmentCount)];
        for( int i = 0; i < mSegmentCount; i++)
        {
            mStartpoints[i] = new FXVector(other.mStartpoints[i]);
            mEndpoints[i] = new FXVector(other.mEndpoints[i]);
            mFaces[i] = other.mFaces[i];
        }
    }    

    /**
     * Load a landscape from a stream. 
     * @param reader the physics reader
     * @return the loaded landscape object
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static Landscape loadLandscape(PhysicsFileReader reader)
    {
     	Landscape landscape = new Landscape();
        
        int segmentCount = reader.nextInt();
        
        for( int i = 0; i < segmentCount; i++)
        {
        	landscape.addSegment(reader.nextVector(),
			        			 reader.nextVector(),
			        			 (short) reader.next());
        }
        
        landscape.mShape = Shape.loadShape(reader, null);
        landscape.mBody = new Body( 0, 0, landscape.mShape, false);
        
        return landscape;
    }
    //#WorldLoadingOFF */
    //#NoBasic */
    
    /**
     * Copy method.
     * Creates a deep copy of the landscape. 
     * @return the copied landscape. 
     */
    public Landscape copy()
    {
        return new Landscape(this);
    }

    /**
     * Adds a segment to the landscape. 
     * @param start start point of the segment line
     * @param end end point of the segment line
     * @param face the face direction 
     */
    public void addSegment(FXVector start, FXVector end, short face)
    {
    	FXVector actualStart = start.xFX < end.xFX ? start : end;
    	FXVector actualEnd = start.xFX < end.xFX ? end : start;
    	if (end.xFX <= start.xFX & face != FACE_NONE)
    	{
    		face = (face == FACE_RIGHT) ? FACE_LEFT : FACE_RIGHT; 
    	}
        mStartpoints = World.checkVector(mStartpoints, mSegmentCount);
        mEndpoints = World.checkVector(mEndpoints, mSegmentCount);
        mFaces = World.checkVector(mFaces, mSegmentCount);

        mStartpoints[mSegmentCount] = actualStart;
        mEndpoints[mSegmentCount] = actualEnd;
        mFaces[mSegmentCount] = face;
        mSegmentCount++;        
        
        sortArrays();
    }
    
    /**
     * Removes a segment from the landscape. 
     * @param index index of teh segment
     */
    public void removeSegment(int index)
    {
        if (index < mSegmentCount)
        {
            mStartpoints[index] = mStartpoints[mSegmentCount - 1];
            mEndpoints[index] = mEndpoints[mSegmentCount - 1];
            mFaces[index] = mFaces[mSegmentCount - 1];
            mSegmentCount--;        
            
            sortArrays();
        }
    }
    
    /**
     * Sorts the segment arrays. 
     * When the segment vector is changed (externally), 
     * it has to sorted for the collision to work.  
     * Consistently sorted arrays are always required for fast collision detection. 
     */
    protected final void sortArrays()
    {
        //performs a linear sort on the three arrays -> see also check contacts for world
        FXVector currVec;        
        FXVector currVecEnd;
        short currface;
        int  j = 0;
        for( int i = 1; i < mSegmentCount; i++)
        {
            currVec = mStartpoints[i];
            currVecEnd = mEndpoints[i];
            currface = mFaces[i];
            for( j = i - 1; j >= 0 && mStartpoints[j].xFX > currVec.xFX; j--)
            {
                //swap (j + 1, j)
                mStartpoints[j + 1] = mStartpoints[j];                
                mEndpoints[j + 1] = mEndpoints[j];
                mFaces[j + 1] = mFaces[j]; 
            }
            mStartpoints[j + 1] = currVec;            
            mEndpoints[j + 1] = currVecEnd;            
            mFaces[j + 1] = currface;            
        }
    }
    
    /**
     * Initializes the collision process. 
     * Resets the state required for the collision detection. 
     */
    protected final void initCollision()
    {
        mCurrentIndex = 0;
        Arrays.fill(mCurrentOpen, 0);     //#NoJ2ME   mCurrentOpen = new int[M_MAX_CHECK_SEGMENTS];
        mOpenCheckSize = 0;
        mOpenCnt = 0;
    } 
    
    /**
     * Checks collision of the landscape and a body. 
     * The algorithm assumes that the method is called 
     * sequentially for each body in the correct order.
     * If a single body has to be tested, the {@link #initCollision()} method should be called first. 
     * @param body the body to check for collision with landscape
     */
    protected final void collisionCheckBody(World world, Body body)
    {        
        if (!body.mDynamic)
        {
            return;
        }
        
        if ( (body.mColissionBitFlag & this.mBody.mColissionBitFlag) != 0)
        {
            return;
        }
        
        //remove remaining segments
        for( int j = 0; j < mOpenCheckSize; j++)
        {
            if ( mCurrentOpen[j] != -1 &&  mEndpoints[mCurrentOpen[j]].xFX < body.mAABBMinXFX)
            {
                mOpenCnt--;
                mCurrentOpen[j] = -1;
            }
        }
        
        int checkHoleIndex = 0;
        while( mCurrentIndex < mSegmentCount 
        		&& mStartpoints[mCurrentIndex].xFX < body.mAABBMaxXFX  )
        {   
            if (mEndpoints[mCurrentIndex].xFX < body.mAABBMinXFX)
            {
            	mCurrentIndex++;
            	continue;
            }
            else
            {
            	boolean toInsert = true;
                for( ; checkHoleIndex < mOpenCheckSize; checkHoleIndex++)
                {
                    if ( mCurrentOpen[checkHoleIndex] == -1)
                    {                    
                        mCurrentOpen[checkHoleIndex] = mCurrentIndex;
                        mOpenCnt++;
                        toInsert = false;
                        break;
                    }                                                                
                }              
                if (toInsert)
                {
                    mCurrentOpen = World.checkVector(mCurrentOpen, mOpenCheckSize);
                    mCurrentOpen[mOpenCheckSize++] = mCurrentIndex;
                    mOpenCnt++;
                }
                if (mOpenCheckSize > mOpenCnt * 2)
                {
                    mOpenCheckSize = World.compactVector(mCurrentOpen, mOpenCheckSize);                 
                }
            }            
            mCurrentIndex++;
        }
        
        //check collision with segments
        for( int j = 0; j < mOpenCheckSize; j++)
        {
            //check other dimension of AABB
            if (mCurrentOpen[j] >= 0 &&
                (! (body.mAABBMinYFX > Math.max( mEndpoints[mCurrentOpen[j]].yFX, mStartpoints[mCurrentOpen[j]].yFX)
                  || Math.min(mStartpoints[mCurrentOpen[j]].yFX, mEndpoints[mCurrentOpen[j]].yFX) > body.mAABBMaxYFX)) )                         
            {
                //detailed collide
                checkBodySegment(world, body, mCurrentOpen[j]);                    
            }
        }
     }
    
    /**
     * Checks whether two the bodies touches a segment.
     * Note: It is assumed that the calling routine already has checked the AABBs of the body.
     * @param world the world
     * @param body the Body to check
     * @param index index of the body segment to check
     */
    private final void checkBodySegment(World world, Body body, int index)
    {
        if (body.mShape instanceof MultiShape)
        {
            int shapeCount = ((MultiShape) body.mShape).getShapeCount();
            //check for multishape

            Contact newContact;
            for( int i = 0; i < shapeCount; i++)
            {
                newContact = Collision.detectCollision( body, i, this, index);
                if (newContact != null)
                {
                    world.mContacts = World.checkVector(world.mContacts, world.mContactCount);
                    world.mContacts[world.mContactCount] = newContact;
                    world.mContactCount++;
                }                   
            }
        }
        else
        {
            Contact newContact = Collision.detectCollision( body, this, index);
            //add the contact directly to the contact list of the world 
            if (newContact != null)
            {
                world.mContacts = World.checkVector(world.mContacts, world.mContactCount);
                world.mContacts[world.mContactCount] = newContact;
                world.mContactCount++;
            }
        }
    }
    
    /**
     * Clears all contacts from the previous step.
     */
    protected void resetContacts()
    {
        //delete contacts
        for( int i = 0; i < mContactCount; i++)
        {            
            if (mContacts[i] != null && !mContacts[i].mIsNew)
            {
                if (World.mContactStorageCount < World.mContactStorage.length)
                {
                    World.mContactStorage[World.mContactStorageCount++] = mContacts[i];
                    mContacts[i].mIsNew = true;   //not nice: this indicates that the contact is already in the storage...
                }
                mContacts[i] = null;
            }            
        }
        compactContacts();
    }
    
    
    /**
     * Compacts the contact vector.
     * Internal Usage - fill null pointer in open vector
     */
    private final void compactContacts()
    {        
        int finalSize = mContactCount;
        int i, j;
        for(i = 0, j = mContactCount -1; i < j; j--)
        {
            while(mContacts[i] != null && i < j) i++;
            while(mContacts[j] == null && i < j) j--;
            
            if (i < j)
            {
                mContacts[i] = mContacts[j];
                mContacts[j] = null;
                finalSize = j;
            }
        }
        while (finalSize > 0 && mContacts[finalSize - 1] == null) finalSize--;
        mContactCount = finalSize;
    }
    
    /**
     * Adds a new contact.
     * @param c the determined contact. 
     */
    protected void addContact(Contact c)
    {
        mContacts = World.checkVector(mContacts, mContactCount);        
        mContacts[mContactCount] = c;
        mContactCount++;        
    }
    
    /**
     * Gets all current contacts.
     * @return all current contacts. 
     */
    public Contact[] getContacts()
    {
        return mContacts;
    }
    
    public final void fillVertices(FXVector[] vertices, Contact c, int idx)
    {
        int index = c.mBody1 == mBody ? c.mB1Index : c.mB2Index;
        vertices[0] = mStartpoints[index];
        vertices[1] = mEndpoints[index];
    }
    
    /**
     * Finds a contact of a segment with a body. 
     * @param body the body.
     * @param index the index of the involved body segment.
     * @return the contact if exists, null otherwise.
     */
    protected Contact getContact( Body body, int bodyIndex, int index)
    {
        //TODO: make faster!
        Contact c;
        for( int j = 0; j < mContactCount; j++)
        {
            c = mContacts[j];
            if ( c != null && 
                ((c.mBody1 == body && bodyIndex == c.mB1Index && index == c.mB2Index) || 
                 (c.mBody2 == body && bodyIndex == c.mB2Index && index == c.mB1Index)) )
            {
                return mContacts[j];                
            }
        }
        
        return null;
    }
    
    //#NoBasic /*
    public void collideParticles(ParticleEmitter particles)
    {
        int particleStartIdx = 0;
        int particleIdx = 0;
        for( int i = 0; i < mSegmentCount; i++)
        {            
            while ( particleStartIdx < particles.mMaxParticleCount && 
                   (particles.mLife[particleStartIdx] <= 0 || 
                     (mStartpoints[i].xFX > particles.mXFX[particleStartIdx] &&
                      mStartpoints[i].xFX > particles.mXPrevFX[particleStartIdx])) )
            {
                particleStartIdx++;
            }
            
            particleIdx = particleStartIdx;
            while ( particleIdx < particles.mMaxParticleCount &&
                   (particles.mLife[particleIdx] <= 0 ||
                     (mEndpoints[i].xFX > particles.mXFX[particleIdx] ||
                      mEndpoints[i].xFX > particles.mXPrevFX[particleIdx])) )
            {
                if (particles.mLife[particleIdx] > 0)
                {
                    FXVector normal = Collision.detectCollision(this, i, particles.mXFX[particleIdx], particles.mYFX[particleIdx], particles.mXPrevFX[particleIdx], particles.mYPrevFX[particleIdx]);
                    if (normal != null)
                    {
                        particles.collide(normal, particleIdx);
                    }
                }
                particleIdx++;
            }
        }
    }
    //#NoBasic */
    
    /**
     * Gets the dummy body.  
     * @return the dummy body.  
     */    
    public Body getBody()
    {
        return mBody;
    }
    
    /**
     * Gets the shape. 
     * @return the shap. 
     */
    public Shape getShape()
    {
        return mShape;
    }

    
    /**
     * Gets the number of landscape segments.  
     * @return the segment count
     */
    public int segmentCount() 
    {
        return mSegmentCount;
    }
    
    /**
     * Gets the starting points of the segments.
     * The returned array is sorted by the x value of the start points. 
     * Note: The array might longer than the segment count (use {@link #segmentCount()}). 
     * @return the array containing the starting points. 
     */
    public FXVector[] elementStartPoints() 
    {
        return mStartpoints;
    }
    
    /**
     * Gets the end points of the segments.
     * The returned array is sorted by the x value of the corresponding start points. 
     * Note: The array might longer than the element count (see {@link #segmentCount()}
     * @return the array containing the end points. 
     */
    public FXVector[] elementEndPoints() 
    {
        return mEndpoints;
    }
    
    /**
     * Gets the start vector of a segment. 
     * @param index the index of the segment. 
     * @return the start vector of the segment at index.  
     */
    public FXVector startPoint(int index)
    {
        return mStartpoints[index];
    }
    /**
     * Gets the end vector of a segment. 
     * @param index the index of the segment. 
     * @return the end vector of the segment at index.  
     */
    public FXVector endPoint(int index)
    {
        return mEndpoints[index];
    }
}
