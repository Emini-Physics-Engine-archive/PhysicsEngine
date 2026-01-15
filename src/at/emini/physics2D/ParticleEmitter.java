package at.emini.physics2D;

import java.util.Random;

import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;

/**
 * Controls a batch of particles.
 *
 * The emitter controls all particle initialization parameters (like position, velocity).
 * It also manages the particles for collisions with other objects.
 * It resolves the collisions and controls the behavior of gravity and other external forces.
 *
 * @author Alexander Adensamer
 */
public class ParticleEmitter
{
    private static final int M_randomMask = (FXUtil.ONE_FX - 1);  //#FX2F

    //particle generation and lifetime parameters
    private int mCreationRateFX = FXUtil.ONE_FX;     //particles per second
    private int mCreationRateDeviationFX = 0;        //particles per second
    private int mCreationRateScaledFX;               //particles per step
    private int mCreationRateDeviationScaledFX;      //particles per step
    private int mCreationCountFX = 0;
    private int mAvgLifeTimeFX = 0;
    private int mAvgLifeTimeDeviationFX = 0;

    /**
     * Maximum number of particles.
     */
    protected int mMaxParticleCount = 0;

    //bahavior parameters
    private int mEelasticityFX = FXUtil.ONE_FX * 2 / 4;
    private int mGravityEffectFX = FXUtil.ONE_FX;
    private int mDampingFX = 0;

    //emitter parameters
    private Body mEmitter;
    private FXVector mRelEmitterPos1;
    private FXVector mRelEmitterPos2;
    private boolean mEmitAxesFixed;

    //emit speed, angle and variation
    private int mEmitSpeedFX = FXUtil.ONE_FX;
    private int mEmitSpeedDeviationFX = 0;
    private int mEmitAngle2FX = 0;
    private int mEmitAngleDeviation2FX = 0;

    /**
     * Performance switch: avoid complex calculation if not required
     */
    boolean mCanCollide = true;

    /**
     * X positions of the particles.
     */
    protected int mXFX[];
    /**
     * Y positions of the particles.
     */
    protected int mYFX[];
    /**
     * Last X positions of the particles.
     */
    protected int mXPrevFX[];
    /**
     * Last Y positions of the particles.
     */
    protected int mYPrevFX[];
    /**
     * remaining lifetime of particle.
     */
    protected short mLife[];

    //random generator
    private Random mRandom = new Random();

    //temporarily used rotation matrix (for initialization).
    private static FXMatrix M_rotation = new FXMatrix();

    //Helper vectors used by the collide method
    private static FXVector M_tmp1 = new FXVector();
    private static FXVector M_tmp2 = new FXVector();

    /**
     * User data
     */
    protected UserData mUserData = null;

    /**
     * Create an empty particle emitter.
     */
    private ParticleEmitter(int particleCount)
    {
        setMaxParticleCount(particleCount);
    }

    /**
     * Constructor.
     */
    public ParticleEmitter(int particleCount,
            int creationRateFX, int creationRateDeviationFX,
            int averageLifeTimeFX, int averageLifeTimeDeviationFX,
            Body emitter, FXVector emitPosition1, FXVector emitPosition2,
            int emitSpeedFX, int emitSpeedDeviationFX,
            int emitAngle2FX, int emitAngleDeviation2FX,
            boolean emitAxesFixed,
            int elasticityFX, int gravityEffectFX, int dampingFX,
            int timestepFX)
    {
        this(particleCount);

        this.mCreationRateFX = creationRateFX;
        this.mCreationRateDeviationFX = creationRateDeviationFX;
        this.mCreationRateScaledFX = FXUtil.multFX(timestepFX, creationRateFX);
        this.mCreationRateDeviationScaledFX = FXUtil.multFX(timestepFX, creationRateDeviationFX);
        this.mAvgLifeTimeFX = averageLifeTimeFX;
        this.mAvgLifeTimeDeviationFX = averageLifeTimeDeviationFX;


        this.mCreationRateScaledFX = FXUtil.multFX(timestepFX, mCreationRateFX);     //particles per step
        this.mCreationRateDeviationScaledFX = FXUtil.multFX(timestepFX, mCreationRateDeviationFX);     //particles per step

        this.mEmitter = emitter;
        this.mRelEmitterPos1 = emitPosition1;
        this.mRelEmitterPos2 = emitPosition2;
        this.mEmitAxesFixed = emitAxesFixed;

        this.mEmitSpeedFX = emitSpeedFX;
        this.mEmitSpeedDeviationFX = emitSpeedDeviationFX;
        this.mEmitAngle2FX = emitAngle2FX;
        this.mEmitAngleDeviation2FX = emitAngleDeviation2FX;

        this.mEelasticityFX = elasticityFX;
        this.mGravityEffectFX = gravityEffectFX;
        this.mDampingFX = dampingFX;
    }

    /**
     * Copy Constructor.
     */
    public ParticleEmitter(ParticleEmitter other)
    {
        this(other.mMaxParticleCount);

        this.mCreationRateFX = other.mCreationRateFX;
        this.mCreationRateScaledFX = other.mCreationRateScaledFX;
        this.mCreationRateDeviationFX = other.mCreationRateDeviationFX;
        this.mCreationRateDeviationScaledFX = other.mCreationRateDeviationScaledFX;
        this.mAvgLifeTimeFX = other.mAvgLifeTimeFX;
        this.mAvgLifeTimeDeviationFX = other.mAvgLifeTimeDeviationFX;

        this.mEmitter = other.mEmitter;
        this.mRelEmitterPos1 = other.mRelEmitterPos1;
        this.mRelEmitterPos2 = other.mRelEmitterPos2;
        this.mEmitAxesFixed = other.mEmitAxesFixed;

        this.mEmitSpeedFX = other.mEmitSpeedFX;
        this.mEmitSpeedDeviationFX = other.mEmitSpeedDeviationFX;
        this.mEmitAngle2FX = other.mEmitAngle2FX;
        this.mEmitAngleDeviation2FX = other.mEmitAngleDeviation2FX;

        this.mEelasticityFX = other.mEelasticityFX;
        this.mGravityEffectFX = other.mGravityEffectFX;
        this.mDampingFX = other.mDampingFX;

        if (other.mUserData != null)
        {
            mUserData = other.mUserData.copy();
        }
    }

    /**
     * Empty constructor for loading purposes.
     */
    private ParticleEmitter()
    {
    }


    /**
     * Copies the particle emitter.
     * Performs a deep copy.
     * @param bodyMapping the mapping of bodies in the new world (null if not used).
     * @return the new particle emitter.
     */
    public ParticleEmitter copy(Body[] bodyMapping)
    {
        ParticleEmitter particleEmitter = new ParticleEmitter(this);
        if (mEmitter != null)
        {
            if (bodyMapping == null)
            {
                particleEmitter.mEmitter = mEmitter;
            }
            else{
                particleEmitter.mEmitter = bodyMapping[mEmitter.mId];
            }
        }
        return particleEmitter;
    }


    /**
     * Creates the parameters for the particle at index.
     * This represents the particle.
     * @param index index of the new particle
     * @param timestepFX the timestep of the simulation
     */
    protected final void createParticle(int index, int timestepFX)
    {
        long partFX = (mRandom.nextInt() & M_randomMask);   //#FX2F float partFX = mRandom.nextFloat();
        if (mEmitter == null)
        {
            mXFX[index] = mRelEmitterPos1.xFX + (int) ((partFX * (long) (mRelEmitterPos2.xFX - mRelEmitterPos1.xFX)) >> FXUtil.DECIMAL);
            mYFX[index] = mRelEmitterPos1.yFX + (int) ((partFX * (long) (mRelEmitterPos2.yFX - mRelEmitterPos1.yFX)) >> FXUtil.DECIMAL);
        }
        else
        {
            if (mEmitAxesFixed)
            {
                mXFX[index] = mEmitter.mPositionFX.xFX + mRelEmitterPos1.xFX + ((int) (partFX * (long) (mRelEmitterPos2.xFX - mRelEmitterPos1.xFX)) >> FXUtil.DECIMAL);
                mYFX[index] = mEmitter.mPositionFX.yFX + mRelEmitterPos1.yFX + ((int) (partFX * (long) (mRelEmitterPos2.yFX - mRelEmitterPos1.yFX)) >> FXUtil.DECIMAL);
            }
            else
            {
                M_tmp1.assignFX(mRelEmitterPos1.xFX + (int) ((partFX * (long) (mRelEmitterPos2.xFX - mRelEmitterPos1.xFX)) >> FXUtil.DECIMAL),
                              mRelEmitterPos1.yFX + (int) ((partFX * (long) (mRelEmitterPos2.yFX - mRelEmitterPos1.yFX)) >> FXUtil.DECIMAL));
                mEmitter.getRotationMatrix().mult(M_tmp1, M_tmp2);
                mXFX[index] = M_tmp2.xFX + mEmitter.mPositionFX.xFX;
                mYFX[index] = M_tmp2.yFX + mEmitter.mPositionFX.yFX;
            }
        }

        int anglePartFX = (mRandom.nextInt() & M_randomMask) - FXUtil.ONE_FX / 2;  //#FX2F float anglePartFX = mRandom.nextFloat() - 0.5f;
        int angle2FX = mEmitAngle2FX + (int) (((long)mEmitAngleDeviation2FX * (long) anglePartFX) >> FXUtil.DECIMAL );
        if (! mEmitAxesFixed && mEmitter != null)
        {
            angle2FX += mEmitter.mRotation2FX;
        }
        angle2FX = FXUtil.wrapAngleFX(angle2FX);
        M_rotation.setRotationMatrix(angle2FX);

        long speedPartFX = (mRandom.nextInt() & M_randomMask) - FXUtil.ONE_FX / 2;  //#FX2F float speedPartFX = mRandom.nextFloat() - 0.5f;
        M_rotation.mult(mEmitSpeedFX + (int) (((long)mEmitSpeedDeviationFX * speedPartFX) >> FXUtil.DECIMAL), 0, M_tmp1);
        FXVector newVelocity = M_tmp1;
        if (mEmitter != null)
        {
            newVelocity.add(mEmitter.mVelocityFX);
        }
        newVelocity.multFX(timestepFX);

        mXPrevFX[index] = mXFX[index] - newVelocity.xFX;
        mYPrevFX[index] = mYFX[index] - newVelocity.yFX;
    }

    /**
     * Creates the required number of new particles in the simulation.
     */
    protected void createParticles(int timestepFX)
    {
        int currIdx = getNextFreeIdx(0);
        int lifeTimeFX;
        while(mCreationCountFX < mCreationRateScaledFX && currIdx != -1)
        {
            lifeTimeFX = mAvgLifeTimeFX + FXUtil.multFX((mRandom.nextInt() & M_randomMask) - FXUtil.ONE_FX / 2, mAvgLifeTimeDeviationFX); //#FX2F
            //#FX2F lifeTimeFX = mAvgLifeTimeFX + (mRandom.nextFloat() - 0.5f) * mAvgLifeTimeDeviationFX;
            mLife[currIdx] = (short) ( (FXUtil.divideFX(lifeTimeFX, timestepFX)) >> FXUtil.DECIMAL);
            createParticle(currIdx, timestepFX);
            mCreationCountFX += FXUtil.ONE_FX;
            currIdx = getNextFreeIdx(currIdx);
        }
        int creationPartFX = (mRandom.nextInt() & M_randomMask) - FXUtil.ONE_FX / 2; //#FX2F float creationPartFX = mRandom.nextFloat() - 0.5f;
        mCreationCountFX = Math.max(0, mCreationCountFX - mCreationRateScaledFX - FXUtil.multFX(creationPartFX, mCreationRateDeviationScaledFX));
    }

    /**
     * Looks for the next free space in the particle arrays.
     * @param currIdx last searched index (if used in a loop).
     * @return the next free index. -1 if none was found.
     */
    private final int getNextFreeIdx(int currIdx)
    {
        for( int i = currIdx; i < mMaxParticleCount; i++)
        {
            if (mLife[i] <= 0) return i;
        }
        return -1;
    }

    /**
     * Moves all particles.
     * The particles are moved according to theirs respective velocity.
     * Performs collision and resolves it.
     * Collision response is only for particles not interacting bodies.
     */
    public void integrateParticles(int timestepFX)
    {
        createParticles(timestepFX);
        int dampingFX = FXUtil.ONE_FX - this.mDampingFX;
        int xTmpFX, yTmpFX;
        for( int i = 0; i < mMaxParticleCount; i++)
        {
            if (mLife[i] > 0)
            {

                xTmpFX = mXFX[i];
                yTmpFX = mYFX[i];
                //apply damping
                if (dampingFX != FXUtil.ONE_FX)
                {
                    mXFX[i] += FXUtil.multFX(dampingFX, mXFX[i] - mXPrevFX[i]);
                    mYFX[i] += FXUtil.multFX(dampingFX, mYFX[i] - mYPrevFX[i]);
                }
                else
                {
                    mXFX[i] += mXFX[i] - mXPrevFX[i];
                    mYFX[i] += mYFX[i] - mYPrevFX[i];
                }
                mXPrevFX[i] = xTmpFX;
                mYPrevFX[i] = yTmpFX;
                mLife[i]--;
            }
        }
        sortParticleList();
    }

    /**
     * Sorts teh complete liist.
     * This is required for performance reasons.
     */
    private final void sortParticleList()
    {
        int currXFX = 0;
        int currYFX = 0;
        short currlife = 0;
        int currXOldFX = 0;
        int currYOldFX = 0;

        int j = 0;
        for( int i = 1; i < mMaxParticleCount; i++)
        {
            if (mLife[i] <= 0)
            {
                continue;
            }

            currXFX = mXFX[i];
            currYFX = mYFX[i];
            currlife = mLife[i];
            currXOldFX = mXPrevFX[i];
            currYOldFX = mYPrevFX[i];
            for( j = i - 1; j >= 0 && (mXFX[j] > currXFX || mLife[j] <= 0); j--)
            {
                //swap (j + 1, j)
                mXFX[j + 1] = mXFX[j];
                mYFX[j + 1] = mYFX[j];
                mLife[j + 1] = mLife[j];
                mXPrevFX[j + 1] = mXPrevFX[j];
                mYPrevFX[j + 1] = mYPrevFX[j];

            }
            mXFX[j + 1] = currXFX;
            mYFX[j + 1] = currYFX;
            mLife[j + 1] = currlife;
            mXPrevFX[j + 1] = currXOldFX;
            mYPrevFX[j + 1] = currYOldFX;

        }
    }

    /**
     * APplies gravity to particles.
     * @param gravity
     * @param dtFX
     */
    public void applyAcceleration(FXVector gravity, int dtFX)
    {
        int scaleFX = (int) (((long)dtFX * (long)dtFX * (long) mGravityEffectFX) >> FXUtil.DECIMAL2);
        for( int i = 0; i < mMaxParticleCount; i++)
        {
            if (mLife[i] > 0)
            {
                mXFX[i] += FXUtil.multFX(scaleFX, gravity.xFX);
                mYFX[i] += FXUtil.multFX(scaleFX, gravity.yFX);
                //velocity[i].add( gravity, scaleFX );
            }
        }
    }

    /**
     * Performs collision for a given particle.
     */
    protected void collide(FXVector normal, int particleIdx)
    {
        //mirror velocity for particleIdx

        mXFX[particleIdx] -= normal.xFX;
        mYFX[particleIdx] -= normal.yFX;
        mXPrevFX[particleIdx] -= normal.xFX;
        mYPrevFX[particleIdx] -= normal.yFX;

        normal.normalizeFast();
        M_tmp2.assignFX(mXFX[particleIdx] - mXPrevFX[particleIdx], mYFX[particleIdx] - mYPrevFX[particleIdx]);
        M_tmp1.assign(normal);
        M_tmp1.multFX( M_tmp2.dotFX(normal) );

        mXPrevFX[particleIdx] += M_tmp1.xFX;
        mYPrevFX[particleIdx] += M_tmp1.yFX;

        M_tmp1.multFX( mEelasticityFX );
        mXFX[particleIdx] -= M_tmp1.xFX;
        mYFX[particleIdx] -= M_tmp1.yFX;
    }


    public int getCreationRateFX()
    {
        return mCreationRateFX;
    }

    public int getCreationRateDeviationFX()
    {
        return mCreationRateDeviationFX;
    }

    public int getAvgLifeTimeFX()
    {
        return mAvgLifeTimeFX;
    }

    public int getAvgLifeTimeDeviationFX()
    {
        return mAvgLifeTimeDeviationFX;
    }

    public int getMaxParticleCount()
    {
        return mMaxParticleCount;
    }

    public int getElasticityFX()
    {
        return mEelasticityFX;
    }

    public int getGravityEffectFX()
    {
        return mGravityEffectFX;
    }

    public Body getEmitter()
    {
        return mEmitter;
    }

    public FXVector getRelEmitterPos1()
    {
        return mRelEmitterPos1;
    }

    public FXVector getRelEmitterPos2()
    {
        return mRelEmitterPos2;
    }

    public boolean emitAxesFixed()
    {
        return mEmitAxesFixed;
    }

    public int getEmitSpeedFX()
    {
        return mEmitSpeedFX;
    }

    public int getEmitSpeedDeviationFX()
    {
        return mEmitSpeedDeviationFX;
    }

    public int getEmitAngle2FX()
    {
        return mEmitAngle2FX;
    }

    public int getEmitAngleDeviation2FX()
    {
        return mEmitAngleDeviation2FX;
    }

    public int getDampingFX()
    {
        return mDampingFX;
    }

    public boolean canCollide()
    {
        return mCanCollide;
    }

    public void setCreationRateFX(int creationRateFX, int creationRateDeviationFX, int timestepFX)
    {
        this.mCreationRateFX = creationRateFX;
        this.mCreationRateScaledFX = FXUtil.multFX(timestepFX, creationRateFX);
        this.mCreationRateDeviationFX = creationRateDeviationFX;
        this.mCreationRateDeviationScaledFX = FXUtil.multFX(timestepFX, creationRateDeviationFX);
    }

    public void setAvgLifeTime(int avgLifeTimeFX, int avgLifeTimeDeviationFX)
    {
        this.mAvgLifeTimeFX = avgLifeTimeFX;
        this.mAvgLifeTimeDeviationFX = avgLifeTimeDeviationFX;
    }

    public void setElasticityFX(int elasticityFX)
    {
        this.mEelasticityFX = elasticityFX;
    }

    public void setGravityEffectFX(int gravityEffectFX)
    {
        this.mGravityEffectFX = gravityEffectFX;
    }

    public void setEmitter(Body emitter)
    {
        this.mEmitter = emitter;
    }

    public void setRelEmitterPos1(FXVector relEmitterPos1)
    {
        this.mRelEmitterPos1 = relEmitterPos1;
    }

    public void setRelEmitterPos2(FXVector relEmitterPos2)
    {
        this.mRelEmitterPos2 = relEmitterPos2;
    }

    public void setEmitAxesFixed(boolean emitAxesFixed)
    {
        this.mEmitAxesFixed = emitAxesFixed;
    }

    public void setEmitSpeedFX(int emitSpeedFX, int emitSpeedDeviationFX)
    {
        this.mEmitSpeedFX = emitSpeedFX;
        this.mEmitSpeedDeviationFX = emitSpeedDeviationFX;
    }

    public void setEmitAngle2FX(int emitAngle2FX, int emitAngleDeviation2FX)
    {
        this.mEmitAngle2FX = emitAngle2FX;
        this.mEmitAngleDeviation2FX = emitAngleDeviation2FX;
    }

    public void setMaxParticleCount(int maxParticleCount)
    {
        if (this.mMaxParticleCount != maxParticleCount)
        {
            this.mMaxParticleCount = maxParticleCount;

            mXFX = new int[maxParticleCount];        //#FX2F mXFX = new float[maxParticleCount];
            mYFX = new int[maxParticleCount];        //#FX2F mYFX = new float[maxParticleCount];
            mXPrevFX = new int[maxParticleCount];    //#FX2F mXPrevFX = new float[maxParticleCount];
            mYPrevFX = new int[maxParticleCount];    //#FX2F mYPrevFX = new float[maxParticleCount];
            mLife = new short[maxParticleCount];
        }
    }


    public void setDampingFX(int dampingFX)
    {
        this.mDampingFX = dampingFX;
    }

    public void setCanCollide(boolean canCollide)
    {
        this.mCanCollide = canCollide;
    }

    public int[] getXPosFX()
    {
        return mXFX;
    }

    public int[] getYPosFX()
    {
        return mYFX;
    }

    public int[] getXPrevPosFX()
    {
        return mXPrevFX;
    }

    public int[] getYPrevPosFX()
    {
        return mYPrevFX;
    }

    public short[] getLifeTimes()
    {
        return mLife;
    }


    /**
     * Loads a Particle Emitter from stream.
     * @param reader the file reader representing the data stream
     * @param world the world for correct referencing of bodies used by the particle emitter
     * @return the loaded particle emitter
     */
    //#WorldLoadingOFF /*
    public static ParticleEmitter loadParticleEmitter(PhysicsFileReader reader, World world, UserData userData)
    {
        ParticleEmitter emitter = new ParticleEmitter();

        emitter.mEmitter = world.findBodyById( reader.next() );
        emitter.mEmitAxesFixed = reader.next() != 0;

        emitter.mRelEmitterPos1 = reader.nextVector();
        emitter.mRelEmitterPos2 = reader.nextVector();

        emitter.setEmitAngle2FX(reader.nextIntFX(), reader.nextIntFX());
        emitter.setEmitSpeedFX(reader.nextIntFX(), reader.nextIntFX());

        emitter.setCreationRateFX( reader.nextIntFX(), reader.nextIntFX(), world.getTimestepFX());
        emitter.setAvgLifeTime(reader.nextIntFX(), reader.nextIntFX());
        emitter.setMaxParticleCount(reader.nextInt());

        emitter.setElasticityFX(reader.nextIntFX());
        emitter.setGravityEffectFX(reader.nextIntFX());
        emitter.setDampingFX(reader.nextIntFX());

        if (reader.getVersion() > World.VERSION_7)
        {
            String userDataString = reader.nextString();
            if (userData != null)
            {
                emitter.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_PARTICLE);
            }
        }
        return emitter;
    }
    //#WorldLoadingOFF */

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
