package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;           //#NoBasic

/**
 * The Joint class represents a joint connecting two bodies.
 * <br>
 * The joint is a pin joint at a single position
 * that fixes this point relative to both bodies.
 * This is the pivot point of the connection.
 * <br>
 * Two types of joints exists: loose joints or fixed joints.
 * For loose joints, the relative angle of the bodies can change.
 * For fixed joints this is fixed as well.
 *
 * @author Alexander Adensamer
 */
public class Joint implements Constraint
{

    /**
     * First body of the joint
     */
    Body mBody1;
    /**
     * Second body of the joint
     */
    Body mBody2;

    /**
     * Relative point in body1, where the joint is fixed
     */
    FXVector mPoint1;
    /**
     * relative point in body2, where the joint is fixed
     */
    FXVector mPoint2;

    /**
     * Flag whether the joint is fixed.
     */
    boolean mFixed;

    /**
     * secondary constraint (angle), if joint is fixed
     */
    int mFixedAngle2FX;


    //precalculated Values;
    private FXMatrix mMatrix = new FXMatrix();
    private FXVector mC1 = new FXVector();
    private FXVector mC2 = new FXVector();
    private FXVector mDistanceFX = new FXVector();
    private int mAngle2FX = 0;
    private long mInvInertia2FX;

    //stores the last impulse
    private FXVector mAccumulatedLambdaFX = new FXVector();

    private static FXVector M_temp1 = new FXVector();
    private static FXVector M_temp2 = new FXVector();

    /**
     * User data
     */
    protected UserData mUserData = null;


    /**
     * Constructor. <br>
     * Note: If the relative positions and the body positions do not match (=over determined system)
     * the initial movement will exhibit a strong urge to correct the inconsistency.
     * @param b1 First Body
     * @param b2 Second Body
     * @param p1 Relative Position of the pivot to Body 1
     * @param p2 relative Position of the pivot to Body 2
     */
    public Joint(Body b1, Body b2, FXVector p1, FXVector p2, boolean fixed)
    {
        mBody1 = b1;
        mBody2 = b2;

        mPoint1 = p1;
        mPoint2 = p2;

        this.mFixed = fixed;
        if (mBody1 != null && mBody2 != null)
            mFixedAngle2FX = FXUtil.wrapAngleFX(mBody1.mRotation2FX - mBody2.mRotation2FX);
    }

    /**
     * Copy Constructor.
     * @param other the source object
     * @param bodyMapping the mapping of bodies in the new world (null if not used).
     * This is required, so that the joint can find the corresponding bodies in the new environment
     */
    protected Joint(Joint other, Body[] bodyMapping)
    {
        if (bodyMapping == null)
        {
            mBody1 = other.mBody1;
            mBody2 = other.mBody2;
        }
        else
        {
            mBody1 = bodyMapping[other.mBody1.mId];
            mBody2 = bodyMapping[other.mBody2.mId];
        }
        mPoint1 = new FXVector(other.mPoint1);
        mPoint2 = new FXVector(other.mPoint2);

        mFixed = other.mFixed;
        mFixedAngle2FX = other.mFixedAngle2FX;

        if (other.mUserData != null)
        {
            mUserData = other.mUserData.copy();
        }
    }

    /**
     * Copy method.
     * Creates a deep copy of the joint.
     * @param bodyMapping the mapping of bodies in the new world (null if not used).
     * This is required, so that the joint can find the corresponding bodies in the new environment
     */
    public Constraint copy(Body[] bodyMapping)
    {
        return new Joint(this, bodyMapping);
    }

    /**
     * Empty constructor.
     * Used for the load method.
     */
    private Joint(){}

    /**
     * Loads a joint from stream.
     * @param reader the file reader
     * @param bodies a list of bodies, initially read from the file to match the stored indices
     * @return the loaded Joint constraint
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static Joint loadJoint(PhysicsFileReader reader, Vector bodies, UserData userData)
    {
        Joint joint = new Joint();

        joint.mBody1 = (Body) bodies.elementAt(reader.next()) ;
        joint.mPoint1 = reader.nextVector();
        joint.mBody2 = (Body) bodies.elementAt(reader.next()) ;
        joint.mPoint2 = reader.nextVector();
        joint.mFixed = reader.next() > 0;
        joint.mFixedAngle2FX = FXUtil.wrapAngleFX(joint.mBody1.mRotation2FX - joint.mBody2.mRotation2FX);

        if (reader.getVersion() > World.VERSION_7)
        {
            String userDataString = reader.nextString();
            if (userData != null)
            {
                joint.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_CONSTRAINT);
            }
        }
        return joint;
    }
    //#WorldLoadingOFF */
    //#NoBasic */

    /**
     * Sets collision layers for joined bodies.
     * Convenience method to apply a collision layer to both involved bodies.
     * @param layer the layer number (somewhere between 0 and 32).
     * @see Body#mColissionBitFlag
     */
    public void setCollisionLayer(int layer)
    {
        mBody1.addCollisionLayer(layer);
        mBody2.addCollisionLayer(layer);
    }

    /**
     * Gets the absolute joint position (from body1).
     * @return the absolute pivot point calculated from the first body.
     */
    public FXVector getPoint1()
    {
        return mBody1.getAbsoluePoint(mPoint1);
    }

    /**
     * Gets the absolute joint position (from body2).
     * @return the absolute pivot point calculated from the second body.
     */
    public FXVector getPoint2()
    {
        return mBody2.getAbsoluePoint(mPoint2);
    }

    /**
     * Gets the relative joint position (from body1).
     * @return the Pivot point relative to the first body.
     */
    public FXVector getRawPoint1()
    {
        return mPoint1;
    }

    /**
     * Gets the relative joint position (from body2).
     * @return the Pivot point relative to the second body.
     */
    public FXVector getRawPoint2()
    {
        return mPoint2;
    }

    /**
     * Sets the first Body.
     * @param b the body
     */
    public void setBody1(Body b)
    {
        mBody1 = b;
    }

    /**
     * Sets the second Body.
     * @param b the body
     */
    protected void setBody2(Body b)
    {
        mBody2 = b;
    }

    /**
     * Gets the Body 1
     * @return the first body
     */
    public Body getBody1()
    {
        return mBody1;
    }

    /**
     * Gets the Body 2
     * @return the second body
     */
    public Body getBody2()
    {
        return mBody2;
    }

    /**
     * Gets the flag if it is a fixed joint.
     * @return true if the the joint has a fixed angle.
     */
    public boolean isFixed()
    {
        return mFixed;
    }

    /**
     * Precalculates the mass matrix.
     * It combines both implicit constraints:
     * One for the x direction, one for the y direction
     * @param invTimestepFX the inverse timestep of the simulation
     */
    public void precalculate(long invTimestepFX)
    {
        int b1InvMass2FX = (int) mBody1.getInvMass2FX();
        int b2InvMass2FX = (int) mBody2.getInvMass2FX();
        long b1InvInertia2FX = mBody1.getInvInertia2FX();
        long b2InvInertia2FX = mBody2.getInvInertia2FX();

        int massSumFX = (b1InvMass2FX + b2InvMass2FX) >> (FXUtil.DECIMAL - FXUtil.ADD_MATRIX_DECIMAL);

        mBody1.getRotationMatrix().mult(-mPoint1.yFX,  mPoint1.xFX, mC1);
        mBody2.getRotationMatrix().mult(mPoint2.yFX, -mPoint2.xFX, mC2);

        FXVector c1 = this.mC1;
        FXVector c2 = this.mC2;

        mMatrix.mCol1xFX = massSumFX + (int) (( ((((long)c1.xFX * (long)c1.xFX)) >> FXUtil.DECIMAL) * b1InvInertia2FX +
                                             (((long)c2.xFX * (long)c2.xFX) >> FXUtil.DECIMAL) * b2InvInertia2FX) >> (FXUtil.DECIMAL2 - FXUtil.ADD_MATRIX_DECIMAL));
        mMatrix.mCol1yFX =             (int) (( ((((long)c1.xFX * (long)c1.yFX)) >> FXUtil.DECIMAL) * b1InvInertia2FX +
                                             (((long)c2.xFX * (long)c2.yFX) >> FXUtil.DECIMAL) * b2InvInertia2FX) >> (FXUtil.DECIMAL2 - FXUtil.ADD_MATRIX_DECIMAL));
        mMatrix.mCol2xFX = mMatrix.mCol1yFX;
        mMatrix.mCol2yFX = massSumFX + (int) (( ((((long)c1.yFX * (long)c1.yFX)) >> FXUtil.DECIMAL) * b1InvInertia2FX +
                                             (((long)c2.yFX * (long)c2.yFX) >> FXUtil.DECIMAL) * b2InvInertia2FX) >> (FXUtil.DECIMAL2 - FXUtil.ADD_MATRIX_DECIMAL));

        mMatrix.invert();

        mBody1.getAbsoluePoint(mPoint1, M_temp1);
        mBody2.getAbsoluePoint(mPoint2, M_temp2);

        mDistanceFX.assignDiff(M_temp2, M_temp1);
        mDistanceFX.multFX(invTimestepFX);
        mDistanceFX.multFX(World.M_JOINT_alphaFX);

        //warmstarting: actually not good for joints
        //applyImpulse(accumulatedLambdaFX);

        if (mFixed)
        {
            mInvInertia2FX = (b1InvInertia2FX + b2InvInertia2FX);
            mAngle2FX = (int) ((((((long) FXUtil.angleDiffFX(mFixedAngle2FX, FXUtil.wrapAngleFX(mBody1.mRotation2FX - mBody2.mRotation2FX)))
                    * invTimestepFX) >> FXUtil.DECIMAL) * World.M_JOINT_angular_alphaFX) >> FXUtil.DECIMAL);

            //warmstarting: actually not good for joints
            //body1.applyTorque(  accumulatedRotFX);
            //body2.applyTorque(- accumulatedRotFX);
        }
    }

    /**
     * Applies the momentum.
     * Uses the precalculated mass matrix and the current velocities of the bodies
     * to calculate and apply the impulse to satisfy the constraint.
     * @param invTimestepFX the inverse timestep of the simulation
     */
    public boolean applyMomentum(long invTimestepFX)
    {
        if (mFixed && mInvInertia2FX != 0)
        {
            int lambdaRotFX = (int) ((((long) (mBody1.mAngularVelocity2FX - mBody2.mAngularVelocity2FX) - mAngle2FX) << FXUtil.DECIMAL) / mInvInertia2FX);
            mBody1.applyTorque(  lambdaRotFX);
            mBody2.applyTorque(- lambdaRotFX);
        }
        FXVector jvFX = M_temp2;
        jvFX.assignFX(
                (int) (mBody1.mVelocityFX.xFX - (((long) mC1.xFX * (long) mBody1.mAngularVelocity2FX)>> FXUtil.DECIMAL2)
                     - mBody2.mVelocityFX.xFX - (((long) mC2.xFX * (long) mBody2.mAngularVelocity2FX)>> FXUtil.DECIMAL2) ),
                (int) (mBody1.mVelocityFX.yFX - (((long) mC1.yFX * (long) mBody1.mAngularVelocity2FX)>> FXUtil.DECIMAL2)
                     - mBody2.mVelocityFX.yFX - (((long) mC2.yFX * (long) mBody2.mAngularVelocity2FX)>> FXUtil.DECIMAL2) ) );
        jvFX.subtract(mDistanceFX);
        jvFX.mult(-1);

        mMatrix.mult(jvFX, M_temp1);
        FXVector lambda = M_temp1;

        //TODO: use body1.applymomentumat()...
        mBody1.applyMomentum(lambda);
        mBody1.applyTorque( (int)( (((long)lambda.xFX * (long)mC1.xFX) >> FXUtil.DECIMAL) + (((long)lambda.yFX * (long)mC1.yFX) >> FXUtil.DECIMAL) ));

        mBody2.applyTorque( (int)( (((long)lambda.xFX * (long)mC2.xFX) >> FXUtil.DECIMAL) + (((long)lambda.yFX * (long)mC2.yFX) >> FXUtil.DECIMAL) ));
        lambda.mult(-1);
        mBody2.applyMomentum(lambda);

        lambda.mult(-1);

        mAccumulatedLambdaFX.assign( lambda );

        return lambda.fastLengthFX() < World.M_CONTACT_IterationConvergenceFX;
    }

    /**
     * Empty.
     */
    public void postStep()
    {
    }



    /**
     * Gets the last impulse of teh joint.
     * THis represents the virtual work by the constraint.
     * @fx
     * @return the last impulse (FX) acting on the joined bodies.
     */
    public int getImpulseFX()
    {
        return mAccumulatedLambdaFX.lengthFX();
    }

    /**
     * Checks if the joint applies to a body.
     * @param b the body to be checked
     * @return true if the joint applies to the body.
     */
    public boolean concernsBody(Body b)
    {
        return mBody1 == b || mBody2 == b;
    }

    /**
     * Checks for equality of two constraints.
     * @param other the comparison constraint
     * @return true if the constraints are equal.
     */
    public boolean equals(Constraint other)
    {
        return (other instanceof Joint)
        && ((Joint) other).mBody1.equals(mBody1)
        && ((Joint) other).mBody2.equals(mBody2)
        && ((Joint) other).mPoint1.xFX == mPoint1.xFX
        && ((Joint) other).mPoint1.yFX == mPoint1.yFX
        && isFixed() == ((Joint) other).isFixed();
    }

    /**
     * Sets the fix point for the joint.
     * @param absolutePoint the fix point in absolute coordinates
     */
    public void setFixPoint(FXVector absolutePoint)
    {
        if (mBody1 != null)
            mPoint1 = mBody1.getRelativePoint(absolutePoint);
        if (mBody2 != null)
            mPoint2 = mBody2.getRelativePoint( absolutePoint);
        if (mBody1 != null && mBody2 != null)
        mFixedAngle2FX = FXUtil.wrapAngleFX(mBody1.rotation2FX() - mBody2.rotation2FX());
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
