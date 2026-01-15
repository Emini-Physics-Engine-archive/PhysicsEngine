package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;           //#NoBasic

/**
 * The Motor class represents a motor constraint for a body.
 * <br>
 * The constraint is applied to a single body.
 * The body is held at a constant velocity (either translational or rotational)
 * with a maximum force.
 * If the force is not sufficient, the body slows down.
 * This can happen if the body collides with a wall or has to work against gravity.
 * If one direction of the target velocities is zero (for translational),
 * it is not considered.
 * That means that the body behaves unconstrained in that direction.
 *
 * @author Alexander Adensamer
 */
public class Motor implements Constraint
{

    /**
     * The body that is moved by the motor.
     */
    Body mBody;

    /**
     * Target speed.
     * Rotational in case of rotation {@link #mIsRotation}.
     * X component of target speed otherwise.
     */
    int mTargetAFX;

    /**
     * Y component of target speed.
     * Only applies in case of translational motor constraint.
     */
    int mTargetBFX;

    /**
     * Type of constraint.
     * True - rotation;
     * False - linear velocity
     */
    boolean mIsRotation;

    /**
     * Whether velocity is applied relative to the current body rotation.
     */
    boolean mIsRelative;

    /**
     * Whether orthogonal movement is fixed to zero.
     */
    boolean mFixOrthogonal;

    /**
     * Maximum impulse(FX).
     * That represents the maximum power of the constraint.
     */
    int mMaxForceFX;


    private static FXVector M_tmp = new FXVector();
    private static FXVector M_lambda = new FXVector();
    private static FXVector M_oldAccumLambda = new FXVector();

    //precalculated values
    private FXVector mJvbFX = new FXVector();
    private FXVector mTargetVelocity = new FXVector();
    private FXVector mTargetVelocityNormalized = new FXVector();

    /**
     * accumulated impulse for warmstarting
     * @fx
     */
    private FXVector mAccumLambdaFX = new FXVector();


    /**
     * User data
     */
    protected UserData mUserData = null;


    /**
     * Constructor for a rotational motor.
     * @fx
     * @param b the body that is affected by the motor.
     * @param targetRotation2FX the target angular velocity (2FX).
     * @param maxForceFX the maximum force that the motor can apply (FX).
     */
    public Motor(Body b, int targetRotation2FX, int maxForceFX)
    {
        mBody = b;
        mTargetAFX = targetRotation2FX;
        mIsRotation = true;
        mIsRelative = false;
        mFixOrthogonal = false;

        this.mMaxForceFX = maxForceFX;
    }

    /**
     * Constructor for a translational motor.
     * @fx
     * @param b the body that is affected by the motor.
     * @param targetXFX the target speed in x direction (FX).
     * @param targetYFX the target speed in y direction (FX).
     * @param maxForceFX the maximum force that the motor can apply (FX).
     */
    public Motor(Body b, int targetXFX, int targetYFX, int maxForceFX)
    {
        mBody = b;
        mTargetAFX = targetXFX;
        mTargetBFX = targetYFX;
        mIsRotation = false;
        mIsRelative = false;
        mFixOrthogonal = false;

        this.mMaxForceFX = maxForceFX;
    }

    /**
     * Copy constructor.
     * @param other the motor constraint to copy.
     * @param bodyMapping the mapping of bodies in the new world (null if not used).
     */
    public Motor(Motor other, Body[] bodyMapping)
    {
        if (bodyMapping != null)
        {
            mBody = bodyMapping[other.mBody.mId];
        }
        else
        {
            mBody = other.mBody;
        }
        mTargetAFX = other.mTargetAFX;
        mTargetBFX = other.mTargetBFX;
        mIsRotation = other.mIsRotation;
        mIsRelative = other.mIsRelative;
        mFixOrthogonal = other.mFixOrthogonal;

        mMaxForceFX = other.mMaxForceFX;

        if (other.mUserData != null)
        {
            mUserData = other.mUserData.copy();
        }
    }

    /**
     * Sets the motor parameters directly.
     * @fx
     * @param targetAFX the x velocity component or rotational velocity.
     * @param targetBFX the y velocity component.
     * @param rotate whether the motor is rotational of translational.
     * @param isRelative if not rotate: whether the motor is working in absolute coordinates or relative to the body it's applied to.
     * @param isOrthogonal if not rotate: whether the motor forces the orthogonal direction to zero.
     */
    public void setParameter(int targetAFX, int targetBFX, boolean rotate, boolean isRelative, boolean isOrthogonal)
    {
        this.mTargetAFX = targetAFX;
        this.mTargetBFX = targetBFX;
        this.mIsRotation = rotate;
        this.mIsRelative = isRelative;
        this.mFixOrthogonal = isOrthogonal;

        mAccumLambdaFX.assignFX(0,0);
    }

    /**
     * Copies the motor constraint.
     * @return a deep copy of the constraint.
     */
    public Constraint copy(Body[] bodyMapping)
    {
        return new Motor(this, bodyMapping);
    }

    /**
     * Empty constructor for loading only.
     */
    private Motor(){}

    /**
     * Loads a motor from a stream.
     * @param reader the physics reader.
     * @param bodies the body vector with the correct indexing.
     * @return the loaded Motor constraint.
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static Motor loadMotor(PhysicsFileReader reader, Vector bodies, UserData userData)
    {
        Motor motor = new Motor();

        motor.mBody = (Body) bodies.elementAt(reader.next()) ;
        motor.mTargetAFX = reader.nextIntFX();
        motor.mTargetBFX = reader.nextIntFX();
        motor.mMaxForceFX = reader.nextIntFX();

        int flags = reader.next();
        motor.mIsRotation = (flags & 0x01) > 0;
        motor.mIsRelative = (flags & 0x02) > 0;
        motor.mFixOrthogonal = (flags & 0x04) > 0;

        if (reader.getVersion() > World.VERSION_7)
        {
            String userDataString = reader.nextString();
            if (userData != null)
            {
                motor.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_CONSTRAINT);
            }
        }

        return motor;
    }
    //#WorldLoadingOFF */
    //#NoBasic */

    /**
     * Performs the warmstarting.<br>
     * Applies last force(= warmstarting for iteration process).
     * No precalculation required.
     * @param invTimestepFX the inverse timestep of the simulation
     */
    public void precalculate(long invTimestepFX)
    {
        if (mIsRotation)
        {
            mBody.applyTorque(mAccumLambdaFX.xFX);
        }
        else
        {

            M_tmp.assignFX(mTargetAFX, mTargetBFX);
            if (mIsRelative)
            {
                FXMatrix rotation = new FXMatrix(mBody.getRotationMatrix());
                rotation.invert();
                rotation.mult(M_tmp, mTargetVelocity);
            }
            else
            {
                mTargetVelocity.assign(M_tmp);
            }


            if (! mFixOrthogonal)
            {
                mTargetVelocityNormalized.assign(mTargetVelocity);
                mTargetVelocityNormalized.normalize();
            }

            //warmstarting
            mBody.applyMomentum(mAccumLambdaFX);
        }
    }

    /**
     * Applies the impulse of the motor. <br>
     * The motor does actual work due to a bias component: Jv + bias.
     * @param invTimestepFX the inverse timestep of the simulation
     */
    public boolean applyMomentum(long invTimestepFX)
    {
        //note: the access body.shape.inertiaFX ignores the possibility of
        //static bodies, but these wont be moved by a motor anyway (since the integration step is skipped)
        //it is bad application design to apply a motor to a static object -> uses performance without any reason
        if (mIsRotation)
        {
            int jvbFX = (int) ((((long) (mBody.mAngularVelocity2FX - mTargetAFX)) * mBody.mShape.mInertiaFX) >> FXUtil.DECIMAL2);

            M_oldAccumLambda.xFX = mAccumLambdaFX.xFX;
            mAccumLambdaFX.xFX = Math.max( Math.min(mAccumLambdaFX.xFX + jvbFX, mMaxForceFX), -mMaxForceFX);

            int lambdaFX = mAccumLambdaFX.xFX - M_oldAccumLambda.xFX;
            mBody.applyTorque(lambdaFX);

            return lambdaFX < World.M_CONTACT_IterationConvergenceFX;
        }
        else
        {
            mJvbFX.assign(mTargetVelocity);

            if (mFixOrthogonal)
            {
                mJvbFX.add(mBody.mVelocityFX, -FXUtil.ONE_FX);
            }
            else
            {
                long normVelocityPartFX = mTargetVelocityNormalized.dotFX(mBody.mVelocityFX);
                mJvbFX.add(mTargetVelocityNormalized, -normVelocityPartFX);
            }

            mJvbFX.multFX(mBody.mShape.mMassFX);

            M_oldAccumLambda.assign(mAccumLambdaFX);
            mAccumLambdaFX.add(mJvbFX);

            mAccumLambdaFX.minFX(mMaxForceFX);
            mAccumLambdaFX.maxFX(-mMaxForceFX);

            M_lambda.assignDiff(mAccumLambdaFX, M_oldAccumLambda);
            mBody.applyMomentum(M_lambda);

            return M_lambda.fastLengthFX() < World.M_CONTACT_IterationConvergenceFX;
        }
    }


    /**
     * Empty.
     */
    public void postStep()
    {
    }


    /**
     * Checks if the constraint applies to a body.
     * @param b the body the check.
     * @return true if the body matches the constraint body.
     */
    public boolean concernsBody(Body b)
    {
        return b.equals(mBody);
    }


    /**
     * Checks for equality of two constraints.
     * @param other the other constraint to check.
     * @return true if the constraints are equal.
     */
    public boolean equals(Constraint other)
    {
        return (other instanceof Motor) &&
                mBody.equals(((Motor) other).mBody) &&
                mTargetAFX == ((Motor) other).mTargetAFX &&
                mTargetBFX == ((Motor) other).mTargetBFX &&
                mIsRotation == ((Motor) other).mIsRotation &&
                mIsRelative == ((Motor) other).mIsRelative &&
                mMaxForceFX == ((Motor) other).mMaxForceFX;
    }

    /**
     * Gets the last impulse.
     * @fx
     * @return the last acted impulse (FX).
     */
    public int getImpulseFX()
    {
        return mAccumLambdaFX.lengthFX();
    }


    /**
     * Gets the body, that the motor applies to.
     * @return the body
     */
    public Body body()
    {
        return mBody;
    }

    /**
     * Checks if the constraint type is rotation.
     * @return true if the motor acts rotational.
     */
    public boolean isRotation()
    {
        return mIsRotation;
    }

    /**
     * Checks if the velocity is applied relative.
     * @return true if velocity is applied relative.
     */
    public boolean isRelative()
    {
        return mIsRelative;
    }

    /**
     * Checks if the orthogonal movement is treated as well by the motor.
     * @return true if orthogonal movement is fixed to zero.
     */
    public boolean isFixOrthogonal()
    {
        return mFixOrthogonal;
    }


    /**
     * Gets the maximum force.
     * @return the maximum force.
     */
    public int getMaxForceFX()
    {
        return mMaxForceFX;
    }

    /**
     * gets the target X.
     * @return the x component of the target velocity.
     */
    protected int getTargetAFX()
    {
        return mTargetAFX;
    }

    /**
     * gets the target Y.
     * @return the y component of the target velocity.
     */
    protected int getTargetBFX()
    {
        return mTargetBFX;
    }

    /**
     * Sets the target velocity X.
     * @param targetAFX the x component of the target velocity.
     */
    protected void setTargetAFX(int targetAFX)
    {
        this.mTargetAFX = targetAFX;
    }

    /**
     * Sets the target velocity Y.
     * @param targetBFX the x component of the target velocity.
     */
    protected void setTargetBFX(int targetBFX)
    {
        this.mTargetBFX = targetBFX;
    }

    /**
     * Sets the maximum force.
     * @param maxForceFX the maximum allowed force by the motor.
     */
    public void setMaxForceFX(int maxForceFX)
    {
        this.mMaxForceFX = maxForceFX;
    }

    /**
     * Sets the rotation mode.
     * @param isRotation the rotation mode.
     */
    protected void setRotation(boolean isRotation)
    {
        this.mIsRotation = isRotation;
    }

    /**
     * Sets the relative flag.
     * This determines whether the velocity is applied relative to the current body rotation.
     * @param isRelative flag whether to apply the velocity relative.
     */
    protected void setIsRelative(boolean isRelative)
    {
        this.mIsRelative = isRelative;
    }

    /**
     * Sets the fix orthogonal flag.
     * This determines whether the orthogonal is fixed to zero.
     * @param fixOrtogonal flag deciding whether to apply the orthogonal correction.
     */
    protected void setFixOrthogonal(boolean fixOrthogonal)
    {
        this.mFixOrthogonal = fixOrthogonal;
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
