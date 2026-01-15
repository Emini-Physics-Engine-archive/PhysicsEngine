package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;       //#NoBasic

/**
 * The Spring class represents a spring constraint on two bodies body. <br>
 * The constraint connects two bodies by a spring.
 * It fixes the distance of two points relative to two bodies like a physical spring.
 * The spring constant can be adjusted.
 * A fixed mode is also possible, where no slack is allowed.
 *
 * @author Alexander Adensamer
 */
public class Spring implements Constraint
{

    /**
     * First Body
     */
    Body mBody1;
    /**
     * Second Body
     */
    Body mBody2;

    /**
     * First anchor point, relative to body 1
     */
    FXVector mPoint1; // relative point in body1, where the joint is fixed
    /**
     * Second anchor point, relative to body 2
     */
    FXVector mPoint2; // relative point in body2, where the joint is fixed

    /**
     * The distance of the constraint.
     * if both anchor points have exactly this distance, no (constraint) force is acting on the bodies
     * @fx
     */
    int mDistanceFX;

    /**
     * The spring coefficient (0 = no spring) FX
     * @fx
     */
    protected int mCoefficientFX = 0;

    /**
     * User data
     */
    protected UserData mUserData = null;

    //precalculated Values;
    private long mOmegaTerm1FX;
    private long mOmegaTerm2FX;
    private FXVector mP1P2 = new FXVector();
    private int mCcurrDistanceFX;
    private FXVector mR1 = new FXVector();
    private FXVector mR2 = new FXVector();
    private long mMassEffectiveInv2FX;

    private int mAccumulatedLambdaFX = 0;

    //temporary vectors
    private static FXVector M_impulse = new FXVector();
    private static FXVector M_temp1 = new FXVector();
    private static FXVector M_temp2 = new FXVector();

    /**
     * Constructor.
     * @param b1 Body 1.
     * @param b2 Body 2.
     * @param p1 relative point 1.
     * @param p2 relative point 2.
     * @param distance default distance of the spring.
     * If -1 is passed, the distance is computed from the current body positions.
     */
    public Spring(Body b1, Body b2, FXVector p1, FXVector p2, int distance)
    {
        mBody1 = b1;
        mBody2 = b2;

        mPoint1 = p1;
        mPoint2 = p2;

        if (distance < 0 )
        {
            calcDistance();
        }
        else
        {
            mDistanceFX = distance << FXUtil.DECIMAL;
        }
    }

    /**
     * Copy constructor.
     * @param other the source spring object.
     * @param bodyMapping the mapping of bodies in the new world (null if not used).
     */
    protected Spring(Spring other, Body[] bodyMapping)
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
        mPoint2 = new FXVector(other.mPoint2);;
        mDistanceFX = other.mDistanceFX;
        mCoefficientFX = other.mCoefficientFX;

        if (other.mUserData != null)
        {
            mUserData = other.mUserData.copy();
        }

    }

    /**
     * Copies the spring.
     * @param bodyMapping the mapping of bodies in the new world (null if not used).
     * @return a deep copy of the constraint.
     */
    public Constraint copy(Body[] bodyMapping)
    {
        return new Spring(this, bodyMapping);
    }

    /**
     * Empty constructor.
     */
    private Spring(){}

    /**
     * Loads a spring from a stream.
     * @param reader the file reader.
     * @param bodies the list of bodies that is references by indices in the stream.
     * @return the loaded spring constraint.
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static Spring loadSpring(PhysicsFileReader reader, Vector bodies, UserData userData)
    {

        Spring spring = new Spring();

        spring.mBody1 = (Body) bodies.elementAt(reader.next()) ;
        spring.mPoint1 = reader.nextVector();
        spring.mBody2 = (Body) bodies.elementAt(reader.next()) ;
        spring.mPoint2 = reader.nextVector();
        spring.mDistanceFX = reader.nextIntFX();
        spring.mCoefficientFX = reader.nextIntFX();

        if (reader.getVersion() > World.VERSION_7)
        {
            String userDataString = reader.nextString();
            if (userData != null)
            {
                spring.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_CONSTRAINT);
            }
        }

        return spring;
    }
    //#WorldLoadingOFF */
    //#NoBasic */

    /**
     * Calculates the default distance.
     * The current distance is computed based on the current body positions.
     */
    protected void calcDistance()
    {
        if (mBody1 != null && mBody2 != null)
        {
            mBody1.getAbsoluePoint(mPoint1, M_temp1);
            mBody2.getAbsoluePoint(mPoint2, M_temp2);

            FXVector p1p2 = new FXVector(M_temp2);
            p1p2.subtract(M_temp1);
            mDistanceFX = p1p2.lengthFX();
        }
    }

    /**
     * Sets the spring coefficient.
     * @param coeff the coefficient.
     * @see #mCoefficientFX
     */
    public void setCoefficient(int coeff)
    {
        mCoefficientFX = (FXUtil.ONE_FX * coeff);
    }

    /**
     * Sets the spring coefficient (FX).
     * @fx
     * @param coeffFX the coefficient (FX)
     * @see Spring#mCoefficientFX
     */
    public void setCoefficientFX(int coeffFX)
    {
        mCoefficientFX = coeffFX;
    }

    /**
     * Gets the spring coefficient.
     * @fx
     * @return the spring coefficient (FX) (see {@link Spring#mCoefficientFX})
     * @see Spring#mCoefficientFX
     */
    public int getCoefficientFX()
    {
        return mCoefficientFX;
    }

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
     * Gets the absolute position of point 1.
     * @return the absolute point 1
     */
    public FXVector getPoint1()
    {
        return mBody1.getAbsoluePoint(mPoint1);
    }

    /**
     * Gets the absolute position of point 1.
     * @param target the target vector
     */
    public void getPoint1(FXVector target)
    {
        mBody1.getAbsoluePoint(mPoint1, target);
    }

    /**
     * Gets the absolute position of point 2.
     * @return the absolute point 2
     */
    public FXVector getPoint2()
    {
        return mBody2.getAbsoluePoint(mPoint2);
    }

    /**
     * Gets the absolute position of point 2.
     * @param target the target vector
     */
    public void getPoint2(FXVector target)
    {
        mBody2.getAbsoluePoint(mPoint2, target);
    }

    /**
     * Gets the relative position of point 1.
     * @return the point 1 relative to body 1
     */
    public FXVector getRawPoint1()
    {
        return mPoint1;
    }

    /**
     * Gets the relative position of point 2.
     * @return the point 2 relative to body 2
     */
    public FXVector getRawPoint2()
    {
        return mPoint2;
    }

    /**
     * Gets the first body.
     * @return the body 1
     */
    public Body getBody1()
    {
        return mBody1;
    }

    /**
     * Gets the second body.
     * @return the body 2
     */
    public Body getBody2()
    {
        return mBody2;
    }

    /**
     * Precalculates the values for the constraint solver iteration.
     * @param invTimestepFX the inverse timestep of the simulation
     */
    public void precalculate(long invTimestepFX)
    {
        mBody1.getAbsoluePoint(mPoint1, M_temp1);
        mBody2.getAbsoluePoint(mPoint2, M_temp2);

        mP1P2.assignDiff(M_temp2, M_temp1);
        mCcurrDistanceFX = mP1P2.lengthFX();
        if (mCcurrDistanceFX == 0)
        {
            return;
        }

        mBody1.getRotationMatrix().mult(mPoint1, mR1);
        mBody2.getRotationMatrix().mult(mPoint2, mR2);

        mP1P2.divideByFX(mCcurrDistanceFX);

        mOmegaTerm1FX = - mP1P2.crossFX(mR1);
        mOmegaTerm2FX = - mP1P2.crossFX(mR2);

        int effInertia1FX = (int) ((mBody1.getInvInertia2FX() * (((long) mOmegaTerm1FX * (long) mOmegaTerm1FX) >> FXUtil.DECIMAL)) >> FXUtil.DECIMAL);
        int effInertia2FX = (int) ((mBody2.getInvInertia2FX() * (((long) mOmegaTerm2FX * (long) mOmegaTerm2FX) >> FXUtil.DECIMAL)) >> FXUtil.DECIMAL);

        mMassEffectiveInv2FX = (mBody1.getInvMass2FX() + effInertia1FX + mBody2.getInvMass2FX() + effInertia2FX);


        //if we have an actual spring only a single iteration is required
        if (mCoefficientFX > 0 )
        {
            int lambdaFX = - (int) (((((long)mCoefficientFX * (long)(mDistanceFX - mCcurrDistanceFX) ) << FXUtil.DECIMAL) / invTimestepFX)>> FXUtil.DECIMAL);
            M_impulse.assign(mP1P2);
            M_impulse.multFX(lambdaFX);

            mBody1.applyMomentumAt(M_impulse, mR1);

            M_impulse.mult(-1);
            mBody2.applyMomentumAt(M_impulse, mR2);
        }

        //warmstarting: proved not to be efficient for springs (see also joints)
        //applyImpulse(accumulatedLambdaFX);
    }

    /**
     * Applies the momentum of the constraint.
     * @param invTimestepFX the inverse timestep of the simulation
     */
    public boolean applyMomentum(long invTimestepFX)
    {
        if (mCoefficientFX > 0 || mMassEffectiveInv2FX == 0)
        {
            return true;
        }
        long jvFX = + mBody1.mVelocityFX.dotFX(mP1P2)
                   - (((long) mBody1.mAngularVelocity2FX * mOmegaTerm1FX) >> FXUtil.DECIMAL2)
                   - mBody2.mVelocityFX.dotFX(mP1P2)
                   + (((long) mBody2.mAngularVelocity2FX * mOmegaTerm2FX) >> FXUtil.DECIMAL2);

        //Baumgarte stabilisation: add a first order error term
        jvFX = jvFX + ((((World.M_JOINT_alphaFX * (long)(mDistanceFX - mCcurrDistanceFX) ) >> FXUtil.DECIMAL) * invTimestepFX) >> FXUtil.DECIMAL);

        int lambdaFX = - (int) ((jvFX << FXUtil.DECIMAL2) / mMassEffectiveInv2FX) ;

        M_impulse.assign(mP1P2);
        M_impulse.multFX(lambdaFX);

        mBody1.applyMomentumAt(M_impulse, mR1);

        M_impulse.mult(-1);
        mBody2.applyMomentumAt(M_impulse, mR2);

        mAccumulatedLambdaFX += lambdaFX;

        return Math.abs(lambdaFX) < World.M_CONTACT_IterationConvergenceFX;
    }


    /**
     * Empty.
     */
    public void postStep()
    {
    }

    /**
     * Returns the default distance of the spring.
     * @return the default distance.
     */
    public int getDistance()
    {
        return mDistanceFX >> FXUtil.DECIMAL;    //#FX2F return (int) mDistanceFX;
    }

    /**
     * Returns the default distance of the spring (FX).
     * @fx
     * @return the default distance (FX)
     */
    public int getDistanceFX()
    {
        return mDistanceFX;
    }

    /**
     * Sets the default distance of the spring (FX).
     * @fx
     * @param distanceFX the new distance (FX) of the spring.
     */
    public void setDistanceFX(int distanceFX)
    {
        if (distanceFX < 0 )
        {
            calcDistance();
        }
        else
        {
            this.mDistanceFX = distanceFX;
        }
    }

    /**
     * Gets the last applied impulse.
     * @fx
     * @return the last impulse (FX) that was used to correct the constraint.
     */
    public int getImpulseFX()
    {
        return mAccumulatedLambdaFX;
    }

    /**
     * Checks if the spring is applied to a body.
     * @param b the body to be checked.
     * @return whether the body is concerned.
     */
    public boolean concernsBody(Body b)
    {
        return mBody1 == b || mBody2 == b;
    }

    /**
     * Checks for equality of two constraints.
     * @param other the comparison constraint.
     * @return whether the constraints are equal.
     */
    public boolean equals(Constraint other)
    {
        return (other instanceof Spring)
        && ((Spring) other).mBody1.equals(mBody1)
        && ((Spring) other).mBody2.equals(mBody2)
        && ((Spring) other).mPoint1.xFX == mPoint1.xFX
        && ((Spring) other).mPoint1.yFX == mPoint1.yFX
        && ((Spring) other).mPoint2.xFX == mPoint2.xFX
        && ((Spring) other).mPoint2.yFX == mPoint2.yFX;
    }

    /**
     * Sets the first body.
     * @param body1 the body.
     */
    protected void setBody1(Body body1)
    {
        this.mBody1 = body1;
    }

    /**
     * Sets the second body.
     * @param body2 the body.
     */
    protected void setBody2(Body body2)
    {
        this.mBody2 = body2;
    }

    /**
     * Sets the first absolute anchor position.
     * @param absolutePoint the absolute position.
     */
    protected void setAbsolutePoint1(FXVector absolutePoint)
    {
        mPoint1 = mBody1.getRelativePoint(absolutePoint);
    }

    /**
     * Sets the second absolute anchor position.
     * @param absolutePoint the absolute position.
     */
    protected void setAbsolutePoint2(FXVector absolutePoint)
    {
        mPoint2 = mBody2.getRelativePoint(absolutePoint);
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
