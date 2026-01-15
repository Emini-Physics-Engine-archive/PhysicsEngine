package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;           //#NoBasic

/**
 * The Body class represents the physical state of a body. <br>
 * This comprises:
 * <list>
 * <li>Position (2D)</li>
 * <li>Angle (1D)</li>
 * <li>Velocity (2D)</li>
 * <li>Angular Velocity (1D)</li>
 * <li>Shape used as a body template, containing friction, elasticity and density (->mass)</li>
 * </list>
 *
 * Each object can be static or dynamic.
 * Static objects do not move and act like having a mass of infinity.
 *
 * @author Alexander Adensamer
 */
public class Body
{
    /**
     * Position vector of the body.
     * The position of the center of gravity.
     */
    protected FXVector mPositionFX = new FXVector();

    /**
     * Current velocity of the body.
     */
    protected FXVector mVelocityFX = new FXVector();

    /**
     * Current virtual velocity of the body.
     * Virtual velocity is used for resolving body penetration.
     * This is the implementation of split impulses.
     */
    protected FXVector mVirtualVelocityFX = new FXVector();

    /**
     * Current angle (2FX) of the body.
     * An angle of zero points in positive x direction.
     * @fx
     */
    protected int mRotation2FX = 0;
    /**
     * Current angular velocity (2FX) of the body.
     * @fx
     */
    protected int mAngularVelocity2FX = 0;

    /**
     * Current Virtual angular velocity of the body.
     * Virtual angular velocity is used for resolving body penetration.
     * This is the implementation of split impulses.
     */
    protected int mVirtualAngularVelocity2FX = 0;

    /**
     * Current rotation matrix.
     * This matrix is used to determine absolute positions
     * of points relative to the body.
     * It is cached to improve performance.
     * Whenever the angle of the object changes,
     * this is updated accordingly.
     */
    private FXMatrix mRotationMatrix = FXMatrix.createRotationMatrix(0);

    /**
     * Flag if the body is allowed to rotate.
     * It can be used to turn rotation off in case of a sprite (e.g: jump and run game).
     */
    boolean mCanRotate = true;

    /**
     * Flag if the body can move.
     * Dynamic bodies can be moved.
     * Static bodies are fixed to their respective position.
     * This can be interpreted as a  mass of infinity.
     */
    boolean mDynamic = true;

    /**
     * Flag if the body is allowed to interact with other bodies
     */
    boolean mInteracting = true;

    /**
     * Flag if the body is affected by gravity
     */
    boolean mGravityAffected = true;


    /**
     * The shape of the body.
     * The shape and physical properties of the body.
     */
    protected Shape mShape;

    /**
     * Current vertices in absolute coordinates.
     * Cached values for performance reasons.
     * Collision detection and rendering require the absolute coordinates of the vertices.
     */
    FXVector[] mVertices;
    FXVector[] mVertexPositionEstimates;

    /**
     * Flag if vertices cache is up to date.
     */
    private boolean mVerticesUpToDate = false;

    /**
     * Current projection axes in absolute coordinates.
     * Cached values for performance reasons.
     * Collision detection the absolute coordinates of the axes.
     */
    private FXVector[] mAxes;

    /**
     * Flag if axes cache is up to date.
     */
    private boolean mAxesUpToDate = false;

    /**
     * Left side of the current AABB (Axis Aligned Boundary Box).
     * This value is always held up to date.
     * @fx
     */
    int mAABBMinXFX = 0;
    /**
     * Right side of the current AABB (Axis Aligned Boundary Box).
     * This value is always held up to date.
     * @fx
     */
    int mAABBMaxXFX = 0;
    /**
     * Top of the current AABB (Axis Aligned Boundary Box).
     * This value is always held up to date.
     * @fx
     */
    int mAABBMinYFX = 0;
    /**
     * Bottom of the current AABB (Axis Aligned Boundary Box).
     * This value is always held up to date.
     * @fx
     */
    int mAABBMaxYFX = 0;

    /**
     * Bitflag for collision detection.
     * Two bodies can only collide if <code>bitflag1 & bitflag2 == 0</code>.
     * Two bodies that are not supposed to collide (e.g: connected by joint)
     * have one common bit set to 1.
     */
    int mColissionBitFlag = 0x0;

    /**
     * Number of current contacts.
     */
    int mContactCount = 0;
    /**
     * Current contacts.
     * All current contacts of the body except contacts with the landscape.
     */
    Contact[] mContacts = new Contact[World.M_BODY_MAX_CONTACTS];

    /**
     * Flag if the body is at rest.
     * Resting bodies are treated specially and use less computation time.
     */
    boolean mIsResting = false;

    /**
     * Body id.
     */
    protected int mId = - 1;

    /**
     * User data.
     */
    protected UserData mUserData = null;

    private static FXVector M_tmp = new FXVector();
    private static FXMatrix M_tmpMatrix = new FXMatrix();

    /**
     * Constructor.
     * The body has to be added to the {@link World} to act there.
     * @param x x position
     * @param y y position
     * @param shape Shape of the body
     * @param dynamic whether the body can move or not
     */
    public Body(int x, int y, Shape shape, boolean dynamic)
    {
        mPositionFX = new FXVector(x << FXUtil.DECIMAL, y << FXUtil.DECIMAL);
        this.mShape = shape;

        initShapeInternals();

        this.mDynamic = dynamic;
        mIsResting = !dynamic;
    }


    /**
     * Constructor.
     * The body has to be added to the {@link World} to act there.
     * @param pos position of the body center
     * @param shape Shape of the body
     * @param dynamic whether the body can move or not
     */
    public Body(FXVector pos, Shape shape, boolean dynamic)
    {
        mPositionFX = new FXVector(pos);
        this.mShape = shape;

        initShapeInternals();

        this.mDynamic = dynamic;
        mIsResting = !dynamic;
   }

    /**
     * Copy constructor.
     * Creates a deep copy of the source body.
     * Only the shape, which is considered a prototype
     * and is shared among bodies anyway is not copied.
     * Has same id as the copied body.
     * @param other the source to copy
     */
    public Body(Body other)
    {
        mShape = other.mShape;
        initShapeInternals();

        mPositionFX = new FXVector(other.mPositionFX);
        mVelocityFX = new FXVector(other.mVelocityFX);

        setRotation2FX(other.mRotation2FX);
        mAngularVelocity2FX = other.mAngularVelocity2FX;

        mColissionBitFlag = other.mColissionBitFlag;

        mDynamic = other.mDynamic;
        mCanRotate = other.mCanRotate;
        mGravityAffected = other.mGravityAffected;
        mInteracting = other.mInteracting;

        if (other.mUserData != null)
        {
            mUserData = other.mUserData.copy();
        }

    }

    //used for loading
    private Body()
    {
    }

    /**
     * Copy method.
     * @return a deep copy of the body.
     */
    public Body copy()
    {
        return new Body(this);
    }

    /**
     * Loads a body from stream.
     * The body is read from the input stream.
     * @param reader the file reader representing the data stream
     * @param shapes a vector containing the shapes in correct order, (the order they are saved in the stored file)
     * @return the loaded body
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static Body loadBody(PhysicsFileReader reader, Vector shapes, UserData userData)
    {
        Body body = new Body();

        if (reader.getVersion() < World.VERSION_5)
        {
            reader.nextIntFX();   //read the mass bytes for older file versions
        }

        body.mPositionFX = reader.nextVector();
        body.mVelocityFX = reader.nextVector();

        body.setRotation2FX(reader.nextInt2FX());
        body.mAngularVelocity2FX = reader.nextInt2FX();

        body.mShape = (Shape) shapes.elementAt(reader.next()) ;

        int flags = reader.next();
        body.mDynamic = (flags & 1) != 0;
        body.mCanRotate = (flags & 2) != 0;
        body.mInteracting = (flags & 4) == 0;
        body.mGravityAffected = (flags & 8) == 0;

        body.mColissionBitFlag = reader.nextInt();

        if (reader.getVersion() > World.VERSION_6)
        {
            String userDataString = reader.nextString();
            if (userData != null)
            {
                body.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_BODY);
            }
        }

        body.initShapeInternals();

        return body;
    }
    //#WorldLoadingOFF */
    //#NoBasic */

    /**
     * Compares two bodies.
     * The comparison checks the id
     * @param other the other body to compare
     * @return whether the bodies are equal
     */
    public boolean equals(Body other)
    {
        return (other.mId == mId) && (mId != -1);
    }


    /**
     * Caches collision info from the shape.
     * Should be called when the shape internals have changed.
     */
    protected void initShapeInternals()
    {
        //init shape vectors
        mVertices = new FXVector[mShape.mVertices.length];
        mVertexPositionEstimates = new FXVector[mShape.mVertices.length];
        for( int i = 0; i < mVertices.length; i++)
        {
            mVertices[i] = new FXVector();
            mVertexPositionEstimates[i] = new FXVector();
        }
        mAxes = new FXVector[mShape.mUniqueAxesIndicesCount / 2];
        for( int i = 0; i < mAxes.length; i++)
        {
            mAxes[i] = new FXVector();
        }

        mVerticesUpToDate = false;
        mAxesUpToDate = false;
    }

    /**
     * Gets the id.
     * @return the body id.
     */
    public int getId()
    {
        return mId;
    }

    /**
     * Checks if the body is dynamic.
     * @return whether the body can move
     */
    public boolean isDynamic()
    {
        return mDynamic;
    }

    /**
     * Checks if the body is resting.
     * @return whether the body is at rest
     */
    public boolean isResting()
    {
        return mIsResting;
    }

    /**
     * Checks if the body can rotate.
     * @return whether the body can rotate.
     */
    public boolean canRotate()
    {
        return mCanRotate;
    }

    /**
     * Sets if the body is allowed to rotate.
     * @param rotatable whether the body is allowed to rotate.
     */
    public void setRotatable(boolean rotatable)
    {
        mCanRotate = rotatable;
    }


    /**
     * Checks if the body can interact with other bodies.
     * @return whether the body interacts.
     */
    public boolean isInteracting()
    {
        return mInteracting;
    }

    /**
     * Sets if the body is allowed to interact with other bodies.
     * @param interacting whether the body is allowed to interact.
     */
    public void setInteracting(boolean interacting)
    {
        this.mInteracting = interacting;
    }

    /**
     * Checks if the body is affected by gravity.
     * @return whether gravity affects the body.
     */
    public boolean isAffectedByGravity()
    {
        return mGravityAffected;
    }

    /**
     * Sets if the body is affected by gravity.
     * @param affected whether the body is affecetd by gravity.
     */
    public void setGravityAffected(boolean affected)
    {
        this.mGravityAffected = affected;
    }


    /**
     * Add a collision layer to the body.
     * Further details for collision layers see {@link Body#mColissionBitFlag}.
     * @param layer the layer for the interaction,
     * must be smaller than 32 as the bitflag is stored in an int
     */
    public void addCollisionLayer(int layer)
    {
        mColissionBitFlag |= 1 << layer;
    }

    /**
     * Remove a collision layer from the body.
     * Further details for collision layers see {@link Body#mColissionBitFlag}.
     * @param layer the layer for the interaction,
     * must be smaller than 32 as the bitflag is stored in an int
     */
    public void removeCollisionLayer(int layer)
    {
        mColissionBitFlag &= ~(1 << layer);
    }

    /**
     * Gets the inverted mass of the body.
     * For static bodies the inverted mass is always zero.
     * @fx
     * @return the mass defined in the shape.
     */
    public final long getInvMass2FX()
    {
        return mDynamic ? mShape.mInvMass2FX : 0;
    }

    /**
     * Gets the inverted inertia of the body.
     * For static bodies the inverted inertia is always zero.
     * @fx
     * @return the inertia defined in the shape.
     */
    public final long getInvInertia2FX()
    {
        return mDynamic ? mShape.mInvInertia2FX : 0;
    }


    /*
     * checks if the body is resting
     */
    /*protected void checkResting()
    {
        if (!isResting)
        {
            boolean restingNeighbor = false;
            if (velocityFX.fastLengthFX() < World.RESTING_THRESHOLD && (angularVelocity2FX >> FXUtil.DECIMAL)< World.RESTING_THRESHOLD)
            {
                //check contacts for a resting (or static) neighbor
                for( int i = 0; i < contactCount; i++)
                {
                    if (contacts[i].body1.invMass2FX == 0 || contacts[i].body2.invMass2FX == 0)
                    {
                        restingNeighbor = true;
                        break;
                    }
                }
            }
            if (restingNeighbor)
            {
                isResting = true;
                invInertia2FX = 0;
                invMass2FX = 0;
            }
        }
    }*/

    /**
     * Moves the body directly.
     * @param translation vector for translation
     * @param timestepFX timestep of the simulation
     */
    public void translate(FXVector translation, int timestepFX)
    {
        mPositionFX.add(translation);
        mVerticesUpToDate = false;
        mAxesUpToDate = false;
        calculateAABB(timestepFX);
    }

    /**
     * Forces an update of body internals.
     * @param timestepFX timestep of the simulation
     */
    public void forceUpdate(int timestepFX)
    {
        mVerticesUpToDate = false;
        mAxesUpToDate = false;
        calculateAABB(timestepFX);
    }


    /**
     * Converts a relative position to absolute coordinates.
     * @param relativePoint position relative to the body center,
     * @return the position in absolute coordinates.
     */
    public final FXVector getAbsoluePoint(FXVector relativePoint)
    {
        FXVector absolutePoint = mRotationMatrix.mult(relativePoint);
        absolutePoint.add(mPositionFX);
        return absolutePoint;
    }

    /**
     * Converts a relative position to absolute coordinates.
     * @param relativePoint position relative to the body center,
     * @param target the vector to store the result into
     */
    public final void getAbsoluePoint(FXVector relativePoint, FXVector target)
    {
        mRotationMatrix.mult(relativePoint, target);
        target.add(mPositionFX);
    }

    /**
     * Converts an absolute position to relative coordinates.
     * Note: This method is very expensive!
     * (create and invert rotation matrix)
     * @param absolutePoint
     * @return a new vector of the relative position.
     */
    public final FXVector getRelativePoint(FXVector absolutePoint)
    {
        FXVector relativePoint = new FXVector(absolutePoint);
        relativePoint.subtract(mPositionFX);
        FXMatrix m2 = FXMatrix.createRotationMatrix(mRotation2FX);
        m2.invert();
        return m2.mult(relativePoint);
    }

    /**
     * Calculates the velocity of a point of the body.
     * The point is given in relative (wrt. the body) coordinates.
     * @param relativePositionFX coordinates of the point relative to the body center
     * @return the velocity at this point.
     */
    public final FXVector getVelocity(FXVector relativePositionFX)
    {
        FXVector velocity = new FXVector(relativePositionFX);
        velocity.crossScalar2FX(mAngularVelocity2FX);
        velocity.add(mVelocityFX);

        return velocity;
    }

    /**
     * Calculates the virtual velocity of a point of the body.
     * The point is given in relative (wrt. the body) coordinates.
     * @param relativePositionFX coordinates of the point relative to the body center
     * @return the virtual velocity at this point.
     */
    protected final FXVector getVirtualVelocity(FXVector relativePositionFX)
    {
        FXVector velocity = new FXVector(relativePositionFX);
        velocity.crossScalar2FX(mVirtualAngularVelocity2FX);
        velocity.add(mVirtualVelocityFX);

        return velocity;
    }

    /**
     * Calculates the velocity of a point (reuse object).
     * The point is given in relative (wrt. the body) coordinates.
     * Writes the resulting velocity into the supplied vector.
     * @param relativePositionFX coordinates of the point relative  to the center.
     * @param result the vector for storing the resulting velocity.
     */
    public final void getVelocity(FXVector relativePositionFX, FXVector result)
    {
        result.assign(relativePositionFX);
        result.crossScalar2FX(mAngularVelocity2FX);
        result.add(mVelocityFX);
    }

    /**
     * Calculates the virtual velocity of a point (reuse object).
     * The point is given in relative (wrt. the body) coordinates.
     * Writes the resulting velocity into the supplied vector.
     * @param relativePositionFX coordinates of the point relative to the center.
     * @param result the vector, where the result is written to
     */
    protected final void getVirtualVelocity(FXVector relativePositionFX, FXVector result)
    {
        result.assign(relativePositionFX);
        result.crossScalar2FX(mVirtualAngularVelocity2FX);
        result.add(mVirtualVelocityFX);
    }


    /**
     * Gets the shape of the body.
     * @return the shape of the body.
     */
    public Shape shape()
    {
        return mShape;
    }

    /**
     * Sets the shape.
     * @param newShape the shape of the body.
     */
    public void setShape(Shape newShape)
    {
        mShape = newShape;

        initShapeInternals();
    }


    /**
     * Calculates the current vertices in absolute coordinates.
     * @return the vertices in absolute coordinates.
     */
    public final FXVector[] getVertices()
    {
        if (!mVerticesUpToDate)
        {
            mShape.getVerticesFX(mPositionFX, mRotationMatrix, mVertices);
            mVerticesUpToDate = true;
        }
        return mVertices;
    }

    /**
     * Calculates the projection axes in absolute coordinates.
     * @return array containing the projection axes in absolute coordinates.
     */
    public final FXVector[] getAxes()
    {
        if (!mAxesUpToDate)
        {
            //assure that the vertices are up to date
            if (!mVerticesUpToDate) getVertices();

            for(int i = 0; i < mAxes.length; i++)
            {
                if (mShape.mUniqueAxesIndices[i*2] < 0) break;
                mAxes[i].assignDiff(mVertices[mShape.mUniqueAxesIndices[i*2]], mVertices[mShape.mUniqueAxesIndices[i*2+1]]);
                mAxes[i].normalize(); //#ContactPrecision mAxes[i].normalizePrecise();
                mAxes[i].turnRight();
            }
            mAxesUpToDate = true;
        }

        return mAxes;
    }

    /**
     * Gets the current angle.
     * @return the current rotation (2FX) of the body.
     * @fx
     */
    public int rotation2FX()
    {
        return mRotation2FX;
    }

    /**
     * Gets the current angular velocity.
     * @return the current angular velocity (2FX) of the body.
     * @fx
     */
    public int angularVelocity2FX()
    {
        return mAngularVelocity2FX;
    }

    /**
     * Gets the rotation matrix.
     * @return the current rotation matrix.
     */
    public FXMatrix getRotationMatrix()
    {
        return mRotationMatrix;
    }

    /**
     * Gets the body position
     * @return the current position of the center of gravity.
     * @fx
     */
    public FXVector positionFX()
    {
        return mPositionFX;
    }

    /**
     * Gets the current velocity.
     * @return the current velocity.
     * @fx
     */
    public FXVector velocityFX()
    {
        return mVelocityFX;
    }

    /**
     * Gets the angulare velocity.
     * @return the angular velocity(2FX)
     * @fx
     */
    public int rotationVelocity2FX()
    {
        return mAngularVelocity2FX;
    }

    /**
     * Sets the current angle (2FX).
     * Updates the rotation matrix and invalidates the vertices and axes.
     * @fx
     * @param rotation2FX new angle (2FX)
     */
    public void setRotation2FX(int rotation2FX)
    {
        this.mRotation2FX = FXUtil.wrapAngleFX(rotation2FX);

        mRotationMatrix.setRotationMatrix(this.mRotation2FX);
        mVerticesUpToDate = false;
        mAxesUpToDate = false;
        mIsResting = false;
    }

    /**
     * Sets the current angle.
     * Updates the rotation matrix and invalidates the vertices and axes.
     * @param rot new angle in degrees (0-360)
     */
    public void setRotationDeg(int rot)
    {
        mRotation2FX = (int)(((long)rot * (long)FXUtil.PI_2FX) / 180);
        while(this.mRotation2FX < 0) this.mRotation2FX += FXUtil.TWO_PI_2FX;
        while(this.mRotation2FX > FXUtil.TWO_PI_2FX) this.mRotation2FX -= FXUtil.TWO_PI_2FX;
        mRotationMatrix.setRotationMatrix(mRotation2FX);
        mVerticesUpToDate = false;
        mAxesUpToDate = false;

    }

    /**
     * Sets the position.
     * Use this method with care.
     * Directly manipulating body positions can cause non-physical behavior.
     * @fx
     * @param positionFX new position for the center of gravity.
     */
    public void setPositionFX(FXVector positionFX)
    {
        this.mPositionFX.assign(positionFX);
    }

    /**
     * Sets the angular velocity.
     * Directly modifies the angular velocity of the body.
     * @fx
     * @param angularVelocity2FX the new velocity (2FX)
     */
    public void angularVelocity2FX(int angularVelocity2FX)
    {
        this.mAngularVelocity2FX = angularVelocity2FX;
    }

    /**
     * Applies acceleration to the body.
     * This can be used for forces like gravity.
     * @fx
     * @param acceleration the acceleration
     * @param timestepFX timestep of the simulation (= time that the force is acting).
     * This should not be larger than the simulation timestep.
     */
    public void applyAcceleration (FXVector acceleration, int timestepFX)
    {
        if (mDynamic && ! mIsResting)
        {
            mVelocityFX.add( acceleration, timestepFX );
        }
    }

    /**
     * Applies forces to the body.
     * This can be used for forces like gravity.
     * @fx
     * @param force the applied force
     * @param timestepFX timestep of the simulation (= time that the force is acting).
     * This should not be larger than the simulation timestep.
     */
    public void applyForce (FXVector force, int timestepFX)
    {
        if (mDynamic && ! mIsResting)
        {
            mVelocityFX.add2FX( force, (mShape.mInvMass2FX * timestepFX) >> FXUtil.DECIMAL  );
        }
    }


    /**
     * Applies an impulse at a position.
     * Applying an impulse represents an acting force over the period of the timestep.
     * The force causes a change in velocity and angular velocity.
     * @param impulse the impulse vector.
     * @param position position in the body (relative  to the center).
     */
    public final void applyMomentumAt (FXVector impulse, FXVector position)
    {
        if (mDynamic && ! mIsResting)
        {
            if (mCanRotate)
            {
                mAngularVelocity2FX -= (int) ((position.crossFX(impulse) * mShape.mInvInertia2FX ) >> (FXUtil.DECIMAL));
            }
            mVelocityFX.add2FX( impulse, mShape.mInvMass2FX );
        }
    }

    /**
     * Applies an impulse at a position (inverts the imulse).
     * COnvenience method for contact resolving.
     * Applying an impulse represents an acting force over the period of the timestep.
     * The force causes a change in velocity and angular velocity.
     * @param impulse the impulse vector.
     * @param position position in the body (relative  to the center).
     */
    final void applyMomentumReverseAt (FXVector impulse, FXVector position)
    {
        if (mDynamic && ! mIsResting)
        {
            if (mCanRotate)
            {
                mAngularVelocity2FX += (int) ((position.crossFX(impulse) * mShape.mInvInertia2FX ) >> (FXUtil.DECIMAL));
            }
            mVelocityFX.add2FX( impulse, -mShape.mInvMass2FX );
        }
    }


    /**
     * Applies a virtual impulse at a position.
     * Implements split impulses.
     * The force causes a change in the virtual velocity and virtual angular velocity.
     * @param impulse the virtual impulse vector.
     * @param position position in the body (relative  to the center).
     */
    public final void applyVirtualMomentumAt (FXVector impulse, FXVector position)
    {
        if (mDynamic && ! mIsResting)
        {
            if (mCanRotate)
            {
                mVirtualAngularVelocity2FX -= (int) ((position.crossFX(impulse) * mShape.mInvInertia2FX ) >> (FXUtil.DECIMAL));
            }
            mVirtualVelocityFX.add2FX( impulse, mShape.mInvMass2FX );
        }
    }

    /**
     * Applies a virtual impulse at a position (inverts impulse).
     * Implements split impulses.
     * The force causes a change in the virtual velocity and virtual angular velocity.
     * @param impulse the virtual impulse vector.
     * @param position position in the body (relative  to the center).
     */
    final void applyVirtualMomentumReverseAt (FXVector impulse, FXVector position)
    {
        if (mDynamic && ! mIsResting)
        {
            if (mCanRotate)
            {
                mVirtualAngularVelocity2FX += (int) ((position.crossFX(impulse) * mShape.mInvInertia2FX ) >> (FXUtil.DECIMAL));
            }
            mVirtualVelocityFX.add2FX( impulse, -mShape.mInvMass2FX );
        }
    }

    /**
     * Applies an impulse to the body center.
     * @param impulse the impulse vector to apply
     */
    public final void applyMomentum (FXVector impulse)
    {
        if (mDynamic && ! mIsResting)
        {
            mVelocityFX.add2FX( impulse, mShape.mInvMass2FX );
        }
    }

    /**
     * Applies torque to the body.
     * @fx
     * @param torqueFX the torque to apply (FX).
     */
    public final void applyTorque(int torqueFX)
    {
        if (mDynamic && mCanRotate && ! mIsResting)
        {
            mAngularVelocity2FX -= (int) ((mShape.mInvInertia2FX * (long)torqueFX ) >> (FXUtil.DECIMAL));
        }
    }

    /**
     * Integrates the velocity.
     * This moves the body one tick forward.
     * Euler integration scheme is used.
     * @fx
     * @param dtFX timestep (FX)
     */
    public final void integrateVelocity (int dtFX)
    {
        if (! mDynamic || mIsResting)
        {
            return;
        }

        mPositionFX.add(mVelocityFX, dtFX);

        setRotation2FX(mRotation2FX - (int)( ((long)(mAngularVelocity2FX)* (long)dtFX) >> FXUtil.DECIMAL));
     }

    public final void integrateVirtualVelocity (int dtFX, FXVector gravity)
    {
        if (! mDynamic || mIsResting)
        {
            return;
        }

        M_tmp.assign(mVirtualVelocityFX);
        M_tmp.multFX(dtFX);
        mPositionFX.add(M_tmp);

        setRotation2FX(mRotation2FX - (int)( ((long)(mVirtualAngularVelocity2FX)* (long)dtFX) >> FXUtil.DECIMAL));

        mVirtualAngularVelocity2FX = 0;
        mVirtualVelocityFX.assignFX(0,0);
     }


    /**
     * Recalculates the AABB.
     * The AABB (axis aligned bounding boxes) are recomputed.
     * This is typically done by the simulation, but can be relevant when a body is manually moved.
     * @param timestepFX the timestep for the lookahead. Use the current timestep of the world.
     */
    protected final void calculateAABB(int timestepFX)
    {
        FXVector[] vertices = getVertices();    //ensure vertices are up to date
        mAABBMinXFX = mAABBMaxXFX = vertices[0].xFX;
        mAABBMinYFX = mAABBMaxYFX = vertices[0].yFX;

        if (mShape instanceof MultiShape)
        {
            MultiShape multiShape = (MultiShape) mShape;

            for( int i = 0; i < multiShape.mShapes.length; i++)
            {
                calcVertexEstimates(multiShape.mShapes[i], multiShape.mVertexStartIndices[i], timestepFX);
            }
        }
        else
        {
            calcVertexEstimates(mShape, 0, timestepFX);
        }
    }

    /**
     * Calculate vertex estimates for the lookout collision detection with AABBs.
     * @param shape
     * @param startIdx
     * @param timestepFX
     */
    private final void calcVertexEstimates(Shape shape, int startIdx, int timestepFX)
    {
        FXVector[] vertices = getVertices();    //ensure vertices are up to date

        if (shape.mVertices.length > 1)
        {
          //compute new bounding box

            int newRotation2FX = FXUtil.wrapAngleFX(mRotation2FX - (int)( ((long)(mAngularVelocity2FX)* (long)timestepFX) >> FXUtil.DECIMAL));
            M_tmpMatrix.setRotationMatrix(newRotation2FX);
            FXVector[] corners = shape.mVertices;
            FXVector pos = M_tmp;
            pos.assign(mPositionFX);
            pos.add(mVelocityFX, timestepFX);

            for( int i = 0; i < shape.mVertices.length; i++)
            {
                M_tmpMatrix.mult(corners[i], mVertexPositionEstimates[startIdx + i]);
                mVertexPositionEstimates[startIdx + i].add(pos);
                mVertexPositionEstimates[startIdx + i].subtract(vertices[startIdx + i]);
            }

            int endIdx = startIdx + shape.mVertices.length;
            for( int i = startIdx; i < endIdx; i++)
            {
                if (mAABBMinXFX > vertices[i].xFX) mAABBMinXFX = vertices[i].xFX;
                if (mAABBMaxXFX < vertices[i].xFX) mAABBMaxXFX = vertices[i].xFX;
                if (mAABBMinYFX > vertices[i].yFX) mAABBMinYFX = vertices[i].yFX;
                if (mAABBMaxYFX < vertices[i].yFX) mAABBMaxYFX = vertices[i].yFX;
                if (mAABBMinXFX > vertices[i].xFX + mVertexPositionEstimates[i].xFX) mAABBMinXFX = vertices[i].xFX + mVertexPositionEstimates[i].xFX;
                if (mAABBMaxXFX < vertices[i].xFX + mVertexPositionEstimates[i].xFX) mAABBMaxXFX = vertices[i].xFX + mVertexPositionEstimates[i].xFX;
                if (mAABBMinYFX > vertices[i].yFX + mVertexPositionEstimates[i].yFX) mAABBMinYFX = vertices[i].yFX + mVertexPositionEstimates[i].yFX;
                if (mAABBMaxYFX < vertices[i].yFX + mVertexPositionEstimates[i].yFX) mAABBMaxYFX = vertices[i].yFX + mVertexPositionEstimates[i].yFX;
            }

        }
        else    //we have a circle
        {
            int xOffsetFX = FXUtil.multFX(mVelocityFX.xFX, timestepFX);
            int yOffsetFX = FXUtil.multFX(mVelocityFX.yFX, timestepFX);

            if (mAABBMinXFX > mPositionFX.xFX - shape.mBoundingRadiusFX) mAABBMinXFX = mPositionFX.xFX - shape.mBoundingRadiusFX;
            if (mAABBMaxXFX < mPositionFX.xFX + shape.mBoundingRadiusFX) mAABBMaxXFX = mPositionFX.xFX + shape.mBoundingRadiusFX;
            if (mAABBMinYFX > mPositionFX.yFX - shape.mBoundingRadiusFX) mAABBMinYFX = mPositionFX.yFX - shape.mBoundingRadiusFX;
            if (mAABBMaxYFX < mPositionFX.yFX + shape.mBoundingRadiusFX) mAABBMaxYFX = mPositionFX.yFX + shape.mBoundingRadiusFX;
            if (mAABBMinXFX > mPositionFX.xFX - shape.mBoundingRadiusFX + xOffsetFX) mAABBMinXFX = mPositionFX.xFX - shape.mBoundingRadiusFX + xOffsetFX;
            if (mAABBMaxXFX < mPositionFX.xFX + shape.mBoundingRadiusFX + xOffsetFX) mAABBMaxXFX = mPositionFX.xFX + shape.mBoundingRadiusFX + xOffsetFX;
            if (mAABBMinYFX > mPositionFX.yFX - shape.mBoundingRadiusFX + yOffsetFX) mAABBMinYFX = mPositionFX.yFX - shape.mBoundingRadiusFX + yOffsetFX;
            if (mAABBMaxYFX < mPositionFX.yFX + shape.mBoundingRadiusFX + yOffsetFX) mAABBMaxYFX = mPositionFX.yFX + shape.mBoundingRadiusFX + yOffsetFX;

            mVertexPositionEstimates[startIdx].assign(mVelocityFX);
            mVertexPositionEstimates[startIdx].multFX(timestepFX);
        }
    }

    /**
     * Postprocesses the velocity.
     * This is used for damping.
     * @fx
     * @param dampingLinearFX linear damping factor
     * @param dampingRotationalFX rotational damping factor
     */
    protected final void updateVelocity(int dampingLinearFX, int dampingRotationalFX)
    {
        if (dampingLinearFX < FXUtil.ONE_FX)
        {
            mVelocityFX.multFX(dampingLinearFX);
        }
        if (dampingRotationalFX < FXUtil.ONE_FX)
        {
            mAngularVelocity2FX = (int) ((long) mAngularVelocity2FX * dampingRotationalFX) >> FXUtil.DECIMAL;
        }
    }

    /**
     * Clear all contacts from previous step.
     */
    protected void resetContacts()
    {
        //delete contacts
        for( int i = 0; i < mContactCount; i++)
        {
            if (!mContacts[i].mIsNew && World.mContactStorageCount < World.mContactStorage.length)
            {
                World.mContactStorage[World.mContactStorageCount++] = mContacts[i];
                mContacts[i].mIsNew = true;   //not nice: this indicates that the contact is already in the storage... (reusing parameter ok, because similar usage)
            }
            mContacts[i] = null;
        }
        mContactCount = 0;
    }

    /**
     * Adds a new contact.
     * This is used by the collision detection.
     * @param c the new Contact
     */
    protected void addContact(Contact c)
    {
        if (mContactCount < World.M_BODY_MAX_CONTACTS)
        {
            mContacts[mContactCount] = c;
            mContactCount++;
        }
    }

    /**
     * Gets the current contacts.
     * @return all current contacts
     */
    public Contact[] getContacts()
    {
        return mContacts;
    }

    /**
     * Find a contact with another body.
     * @param other other body.
     * @return the contact if exists, null otherwise.
     */
    protected Contact getContact( Body other)
    {
        for( int j = 0; j < mContacts.length; j++)
        {
            if (mContacts[j] == null)
            {
                break;
            }
            if ( mContacts[j].mBody2 == other || mContacts[j].mBody1 == other)
            {
                return mContacts[j];
            }
        }

        return null;
    }

    /**
     * Find a contact with another body.
     * @param other other body.
     * @return the contact if exists, null otherwise.
     */
    protected Contact getContact( int idx1, Body other, int idx2)
    {
        for( int j = 0; j < mContacts.length; j++)
        {
            if (mContacts[j] == null)
            {
                break;
            }
            if ( (mContacts[j].mBody2 == other && mContacts[j].mB2Index == idx2 && mContacts[j].mB1Index == idx1)||
                 (mContacts[j].mBody1 == other && mContacts[j].mB1Index == idx2 && mContacts[j].mB2Index == idx1))
            {
                return mContacts[j];
            }
        }

        return null;
    }

    /**
     * Sets dynamic property of the body.
     * @param dynamic if the body can move.
     */
    public void setDynamic(boolean dynamic)
    {
        this.mDynamic = dynamic;
    }

    /**
     * Gets the left side of the AABB.
     * @return the Left side of the AABB.
     */
    public int getAABBMinXFX()
    {
        return mAABBMinXFX;
    }

    /**
     * Gets the right side of the AABB.
     * @return the Left side of the AABB.
     */
    public int getAABBMaxXFX()
    {
        return mAABBMaxXFX;
    }

    /**
     * Gets the top of the AABB.
     * @return the Left side of the AABB.
     */
    public int getAABBMinYFX()
    {
        return mAABBMinYFX;
    }

    /**
     * Gets the bottom of the AABB.
     * @return the Left side of the AABB.
     */
    public int getAABBMaxYFX()
    {
        return mAABBMaxYFX;
    }

    /**
     * Gets the complete collision flag.
     * @return the complete bit flag
     * containing information about all collision layers.
     */
    public int getColissionBitFlag()
    {
        return mColissionBitFlag;
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
