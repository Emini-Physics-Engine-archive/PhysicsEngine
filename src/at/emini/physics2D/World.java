package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;        //#NoBasic

/**
 *
 * The world class represents the simulation environment. <br>
 * It has the following responsibilities:
 * <ul>
 * <li>It keeps track of all objects. </li>
 * <li>It checks for occurrence of registered events. </li>
 * <li>It ticks the world (= perform a simulation step).</li>
 * <li>Retrieve all informations about the current simulation status. </li>
 * </ul>
 * <p>
 * The simulation step consists of
 * <ul>
 * <li>application of external forces (e.g: gravity)</li>
 * <li>collision checking</li>
 * <li>contact resolution</li>
 * <li>constraint resolution</li>
 * <li>velocity integration</li>
 * </ul>
 *
 * It handles fast movement detection and resolution,
 * event checking and handling and execution of body scripts.<br>
 *
 * All physical units are given in pixel and seconds (e.g: speed in pixel/sec.).
 * Masses are on dimensionless scale.
 *
 *
 * @author Alexander Adensamer
 */
public class World
{

    //Save/Load version flags
    static final int MASK_VERSION = 0xFF00;   //version mask
    static final int VERSION_1  =   0x0100;   //version Index
    static final int VERSION_2  =   0x0200;   //version Index
    static final int VERSION_3  =   0x0300;   //version Index
    static final int VERSION_4  =   0x0400;   //version Index 4: new event structure, trigger once
    static final int VERSION_5  =   0x0500;   //version Index 5: shape uses mass
    static final int VERSION_6  =   0x0600;   //version Index 6: add simulation parameter in world file
    static final int VERSION_7  =   0x0700;   //version Index 7: userData for shape and body
    static final int VERSION_8  =   0x0800;   //version Index 8: userData particle emitter, world, events and constraints
    static final int VERSION_9  =   0x0900;   //version Index 9: multishapes
    static final int VERSION_10 =   0x0A00;   //version Index 10: rotational damping

    //Saving/Loading section indices
    static final int SHAPES_IDX       = 1;
    static final int BODY_IDX         = 2;
    static final int CONSTRAINTS_IDX  = 3;
    static final int SCRIPTS_IDX      = 4;
    static final int EVENTS_IDX       = 5;
    static final int LANDSCAPE_IDX    = 6;
    static final int WORLD_IDX        = 7;
    static final int PARTICLES_IDX    = 8;

    /**
     * Registered Event listener
     */
    private PhysicsEventListener mListener;                              //#NoBasic

    //physical parameters
    /**
     * Gravity (FX), in pixel/sec^2
     */
    private FXVector mGravity = new FXVector(0, FXUtil.ONE_FX * 100 );

    /**
     * Lateral Damping factor (FX) controls background damping of all motion. <br>
     * Each lateral velocity is decelerated by that factor each timestep
     * reasonable values range from 0.0..0.005 {@link FXUtil#ONE_FX}
     */
    private int mDampingLinearFX = 0; //FXUtil.ONE_FX * 2 / 1000;

    /**
     * Rotational Damping factor (FX) controls background damping of all motion. <br>
     * Each rotational velocity is decelerated by that factor each timestep
     * reasonable values range from 0.0..0.005 {@link FXUtil#ONE_FX}
     */
    private int mDampingRotationalFX = 0; //FXUtil.ONE_FX * 2 / 1000;

    //simulation parameters
    /**
     * Simluation timestep (FX): large timestep leads to faster, but more imprecise simulation
     * The default value is 1 / 20 ( {@link FXUtil#ONE_FX} / 20 )
     */
    int mTimestepFX = FXUtil.ONE_FX / 20;
    /**
     * The inverse timestep (FX) is the actually used timestep
     */
    long mInvTimestepFX = (FXUtil.ONE_FX << FXUtil.DECIMAL) / mTimestepFX;

    /**
     * The number of iterations performed for the constraint solver.
     * Smaller values lead to faster but imprecise simulation,
     * because the convergence to the correct equation solution is worse.
     * Default is 10.
     */
    int mConstraintIterations = 10;

    /**
     * The number of iterations performed for the position constraint solver.
     * Smaller values lead to faster but imprecise simulation,
     * because the convergence to the correct equation solution is worse.
     * The position constraints govern the positions of the bodies directly.
     * Default is 5.
     */
    int mPositionConstraintIterations = 5;

    /**
     * Determines whether dynamic constraint iterations are used.
     * The constraint iterations are still acting as maximum.
     */
    boolean mDynamicConstraintIteration = false;



    //stabilisation parameters
    static final int M_CONSTRAINT_IterationConvergenceFX = FXUtil.ONE_FX * 4 / 512;

    /**
     * The Baumgarte stabilization factor (FX) for the joint constraints.
     */
    static final long M_JOINT_alphaFX = FXUtil.ONE_FX * 4 / 16;               //Baumgarte stabilization factor: joint, spring
    static final long M_JOINT_angular_alphaFX = FXUtil.ONE_FX * 12 / 16;      //Baumgarte stabilization factor: joint, spring

    /**
     * Slack, that is permitted for body penetration.
     * This inhibits jittering and thus makes the simulation smoother.
     */
    static final int M_CONTACT_touchEpsilonFX = FXUtil.ONE_FX * 1 / 16;                   //allowed penetration depth: contact
    static final int M_CONTACT_touchEpsilonCollisionSlackFX = FXUtil.ONE_FX * 5 / 64;     //allowed penetration depth: contact
    static final int M_CONTACT_touchEpsilonSlack1FX = - FXUtil.ONE_FX * 1 / 16;           //check whether to skip the contact
    static final int M_CONTACT_touchEpsilonSlack2FX = FXUtil.ONE_FX * 1 / 16 * 2;         //final check

    static final int M_CONTACT_positionCorrectThresholdFX = FXUtil.ONE_FX * 2 / 256;    //allowed penetration depth: contact

    static final int M_CONTACT_IterationConvergenceFX = FXUtil.ONE_FX * 2 / 512;
    /**
     * Correct factor (FX) for resolving penetrations. Similar to Baumgarte stabilization.
     */
    static final long M_CONTACT_betaFX = FXUtil.ONE_FX * 8 / 16;                             //correct factor for contact

    /**
     * Condition number (FX) for numeric singularities.
     * Singularities can happen if two contacts are very close, and actually represent a single constraint.
     * If the corresponding mass matrix becomes singular, we treat it as a single contact.
     */
    static final int M_CONTACT_MaxConditionNumber = 30;             //condition Number for (numeric) singularities

    //collision parameters
    static final int M_COLLISION_collinearityDeltaFX = (FXUtil.ONE_FX * 16 / 4); //factor for determining collinearity for two faces
    static final int M_COLLISION_collinearityThresholdFX = FXUtil.ONE_FX + (FXUtil.ONE_FX / 16);

    public static final int M_SHAPE_MAX_VERTICES = 12;
    static final int M_SHAPE_UniqueAxesFactorFX = FXUtil.ONE_FX - (FXUtil.ONE_FX / 512);       //factor for determining the relevant axes for projecting at a shape

    static final int M_RESTING_THRESHOLD = - (1 << 0);


    //technical and memory management parameters
    static final int M_INITIAL_SCRIPT_MAX_SCRIPTS = 32;
    static final int M_INITIAL_SCRIPT_MAX_BODIES = 32;

    static final int M_BODY_MAX_CONTACTS = 8;
    static final int M_LANDSCAPE_INITIAL_MAX_CONTACTS = 32;

    /**
     * increment for static array resizing
     */
    private static final int M_WORLD_ARRAY_INCREMENT = 32;

    /**
     * Initial size of body vector
     */
    static final int M_INITIAL_MAX_BODIES = 128;
    /**
     * Initial size of vector containing fast bodies
     */
    static final int M_INITIAL_MAX_FAST_BODIES = 16;
    /**
     * Initial size of contact vector
     */
    static final int M_INITIAL_MAX_CONTACTS = 128;
    /**
     * Maximum size of contact storage vector
     */
    static final int M_INITIAL_MAX_STORAGE_CONTACTS = 32;
    /**
     * Initial size of constraints vector
     */
    static final int M_INITIAL_MAX_CONSTRAINTS = 32;

    //World members
    /**
     * Shapeset managing the shapes.
     */
    protected ShapeSet mShapeSet = new ShapeSet();
    /**
     * Eventset managing the events.
     */
    protected EventSet mEventSet = new EventSet();                       //#NoBasic

    /**
     * helper data structure for optimized collision
     * sweep and prune
     */
    private Body[] mCurrentOpen = new Body[M_INITIAL_MAX_BODIES];

    /**
     * the start of the computation area (along the x-axis)
     */
    int mAreaStartFX = Integer.MIN_VALUE;
    /**
     * the end of the computation area (along the x-axis)
     */
    int mAreaEndFX = Integer.MAX_VALUE;

    //World bodies (physical and constraints)
    int mBodyCount = 0;
    Body[] mBodies = new Body[M_INITIAL_MAX_BODIES];   //sorted bodies
    private int mCurrentBodyId = 0;                    //id for next body insertion

    int mBodyStartIndex = 0;
    int mBodyEndIndex = mBodyCount;

    //Landscape (if used)
    Landscape mLandscape = new Landscape();                                          //#NoEco

    //Contact storage
    int mContactCount = 0;
    Contact[] mContacts = new Contact[M_INITIAL_MAX_CONTACTS];
    static int mContactStorageCount = 0;
    static Contact[] mContactStorage = new Contact[M_INITIAL_MAX_STORAGE_CONTACTS];
    int mConstraintCount = 0;                                                        //#NoEco
    Constraint[] mConstraints = new Constraint[M_INITIAL_MAX_CONSTRAINTS];           //#NoEco

    //particles
    protected Vector mParticles = new Vector();                                      //#NoBasic

    //external forces
    protected Vector mForces = new Vector();                                         //#NoBasic

    //scripts
    Script[] mScripts = new Script[World.M_INITIAL_SCRIPT_MAX_SCRIPTS];                //#NoBasic
    int mScriptCount = 0;                                                              //#NoBasic

    Body[] mScriptBodies = new Body[World.M_INITIAL_SCRIPT_MAX_BODIES];                //#NoBasic
    int[] mScriptIndex = new int[World.M_INITIAL_SCRIPT_MAX_BODIES];                   //#NoBasic
    int[] mScriptElementIndex = new int[World.M_INITIAL_SCRIPT_MAX_BODIES];            //#NoBasic
    int[] mScriptExecutionIndex = new int[World.M_INITIAL_SCRIPT_MAX_BODIES];          //#NoBasic
    int mScriptBodyCount = 0;                                                        //#NoBasic

    /**
     * Iteration counter
     */
    static int M_iteration = 0;


    /**
     * User data
     */
    protected UserData mUserData = null;


    /**
     * Empty Constructor.
     * Creates an empty world.
     */
    public World()
    {
    }

    /**
     * Constructor with shapes.
     * Creates an empty world using a special shape set.
     */
    protected World(ShapeSet set)
    {
        mShapeSet = set;
    }

    /**
     * Copy constructor.
     * Creates a deep copy of the world except for shapes, which are references.
     * @param world source world object
     */
    public World(World world)
    {
        if (world == null)
        {
            return;
        }
        mShapeSet = world.mShapeSet.copy();

        mGravity.assign(world.mGravity);
        mDampingLinearFX = world.mDampingLinearFX;
        mDampingRotationalFX = world.mDampingRotationalFX;
        mAreaStartFX = world.mAreaStartFX;
        mAreaEndFX = world.mAreaEndFX;

        mTimestepFX = world.mTimestepFX;
        mInvTimestepFX = world.mInvTimestepFX;
        mConstraintIterations = world.mConstraintIterations;
        mPositionConstraintIterations = world.mPositionConstraintIterations;

        addWorld(world);

        mEventSet = world.mEventSet.copy();           //#NoBasic

        //#NoEco /*
        if (world.mLandscape != null)
        {
            setLandscape(world.mLandscape.copy());
        }
        //#NoEco */

        if (world.mUserData != null)
        {
            mUserData = world.mUserData.copy();
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


    /**
     * Loads a world from a stream.
     * Convenience method without user data.
     * @param reader the reader.
     * @return the loaded world.
     */
    //#NoBasic /*
    public static World loadWorld(PhysicsFileReader reader)
    {
        return loadWorld(reader, null);
    }
    //#NoBasic

    /**
     * Loads a world from a stream.
     * Makes use of user data.
     * @param reader the reader.
     * @param userData empty user data object
     * @return the loaded world.
     */
  //#NoBasic /*
  //#WorldLoadingOFF /*
    public static World loadWorld(PhysicsFileReader reader, UserData userData)
    {
        int version = reader.getVersion();
        if ( ((version & MASK_VERSION) < VERSION_1) ||
             ((version & MASK_VERSION) > VERSION_10) )
        {
            return null;
        }
        World world = new World();
        Vector shapes = new Vector();
        Vector bodies = new Vector();

        int nextItem = 0;
        while( nextItem != -1)
        {
            switch(nextItem)
            {
            case SHAPES_IDX:
                {
                    int shapeCount = reader.next();
                    for( int i = 0; i < shapeCount; i++)
                    {
                        shapes.addElement( Shape.loadShape(reader, userData));
                    }
                    if (version > VERSION_8)
                    {
                        int multiShapeCount = reader.next();
                        for( int i = 0; i < multiShapeCount; i++)
                        {
                            shapes.addElement( MultiShape.loadShape(reader, userData, shapes));
                        }
                    }
                    //do this after directly loading to ensure the correct order of shapes
                    world.mShapeSet.registerShapes(shapes);

                }
                break;
            case BODY_IDX:
                {
                    int bodyCount = reader.next();

                    for( int i = 0; i < bodyCount; i++)
                    {
                        Body b = Body.loadBody(reader, shapes, userData);
                        bodies.addElement(b);
                        world.addBody(b);
                    }
                }
                break;
            case LANDSCAPE_IDX:
                {
                    world.setLandscape( Landscape.loadLandscape(reader) );
                }
                break;
            case CONSTRAINTS_IDX:
                {
                    int constraintCount = reader.next();
                    for( int i = 0; i < constraintCount; i++)
                    {
                        world.addConstraint( World.loadConstraint(reader, bodies, userData));
                    }
                }
                break;
            case SCRIPTS_IDX:
                {
                    int scriptCount = reader.next();
                    for( int i = 0; i < scriptCount; i++)
                    {
                        world.addScript( Script.loadScript(reader));
                    }

                    world.mScriptBodyCount = reader.next();
                    for( int i = 0; i < world.mScriptBodyCount; i++)
                    {
                        int index = reader.next();
                        int bodyIndex = reader.next();
                        if (bodyIndex >= 0 && bodyIndex < bodies.size() && index < scriptCount)
                        {
                            world.mScriptIndex[i] = reader.next();
                            world.mScriptBodies[i] = (Body) bodies.elementAt(reader.next());
                        }
                    }
                }
                break;
            case EVENTS_IDX:
                {
                    int eventCount = reader.next();
                    for( int i = 0; i < eventCount; i++)
                    {
                        world.addEvent( Event.loadEvent(reader, world, userData ));
                    }
                }
                break;
            case WORLD_IDX:
                {
                    world.setGravity(reader.nextVector());
                    if (reader.getVersion() > World.VERSION_9)
                    {
                        world.setDampingLateralFX(reader.nextInt());
                        world.setDampingRotationalFX(reader.nextInt());
                    }
                    else
                    {
                        int dampingFX = FXUtil.ONE_FX - reader.nextInt();
                        world.setDampingLateralFX(dampingFX);
                        world.setDampingRotationalFX(dampingFX);
                    }

                    if (reader.getVersion() > World.VERSION_7)
                    {
                        String userDataString = reader.nextString();
                        if (userData != null)
                        {
                            world.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_WORLD);
                        }
                    }
                }
                break;
            case PARTICLES_IDX:
                {
                    int particleCount = reader.next();
                    for( int i = 0; i < particleCount; i++)
                    {
                        world.addParticleEmitter( ParticleEmitter.loadParticleEmitter(reader, world, userData ));
                    }
                }
                break;
            default:
                break;
            }

            //the version1 has no type indicator
            //we simply switch to the next
            //all were present
            if ( (version & MASK_VERSION) == VERSION_1)
            {
                nextItem++;
              //the original files had no scripts
                if (nextItem == SCRIPTS_IDX) nextItem++;
                if (nextItem > EVENTS_IDX)
                {
                    break;
                }
            }
            else
            {
                //newer version read the next section type
                nextItem = reader.next();
            }
        }

        reader.close();

        return world;
    }
    //#WorldLoadingOFF */
    //#NoBasic */

    /**
     * Gets the timestep.
     * @return the timestep (FX) of the simulation.
     */
    public int getTimestepFX()
    {
        return mTimestepFX;
    }

    /**
     * Gets the inverse of the timestep.
     * @return inverse of the timestep (FX) of the simulation.
     */
    public long getInverseTimestepFX()
    {
        return mInvTimestepFX;
    }

    /**
     * Sets the timestep.
     * Simluation timestep (FX): large timestep leads to faster, but imprecise simulation
     * The default value is 1 / 20.
     * @param timeStepFX the new timestep (FX)
     */
    public void setTimestepFX(int timeStepFX)
    {
        mTimestepFX = timeStepFX;
        mInvTimestepFX = (FXUtil.ONE_FX << FXUtil.DECIMAL) / mTimestepFX;
    }


    /**
     * Sets the number of constraint iterations.
     * The default value is 10.
     * Raising this value increases the simulation precision, but costs performance.
     * @param constraintIterations number of iterations for constraint (default: 10)
     */
    public void setConstraintIterations(int constraintIterations)
    {
        mConstraintIterations = constraintIterations;
    }

    /**
     * Sets the number of position constraint iterations.
     * The default value is 5.
     * Raising this value increases the simulation precision - in particular resolution of body penetration, but costs performance.
     * @param positionConstraintIterations number of iterations for position constraints (default: 5)
     */
    public void setPositionConstraintIterations(int positionConstraintIterations)
    {
        mPositionConstraintIterations = positionConstraintIterations;
    }

    /**
     * Returns constraint iteration number.
     */
    public int getConstraintIterations()
    {
        return mConstraintIterations;
    }

    /**
     * Returns position constraint iteration number.
     */
    public int getPositionConstraintIterations()
    {
        return mPositionConstraintIterations;
    }

    /**
     * Sets the constraint iteration mode to dynamic/fixed.
     * The default is false.
     * Fixed iteration mode iterates a fixed number of times (See {@link #mConstraintIterations}).
     * The dynamic mode stops when all constraints are staisfied.
     * The iteration count still works as an upper limit.
     * @param isDynamic whether the constraint iteration length is dynamic
     */
    public void setConstraintIterationDynamic(boolean isDynamic)
    {
        mDynamicConstraintIteration = isDynamic;
    }


    /**
     * Sets the event listener.
     * @param listener the listener.
     */
    //#NoBasic /*
    public void setPhysicsEventListener(PhysicsEventListener listener)
    {
        this.mListener = listener;
    }
    //#NoBasic */

    /**
     * Sets the simulation area.
     * This defines an area along the x-axis, where the simulation is performed.
     * Bodies outside that area are ignored.
     * It can significantly increase simulation performance for large worlds.
     * Only the relevant part, that is rendered to the screen is simulated.
     * Always the whole y-dimension is computed due to the underlying optimizations.
     * @param start the start coordinate of the simulation area (along the x-axis)
     * @param end the end coordinate of the simulation area (along the x-axis)
     */
    public void setSimulationArea(int start, int end)
    {
        mAreaStartFX = start << FXUtil.DECIMAL;
        mAreaEndFX = end << FXUtil.DECIMAL;
    }

    /**
     * Gets the shape set.
     * @return the shape set for this world.
     */
    public ShapeSet getShapeSet()
    {
        return mShapeSet;
    }

    /**
     * Adds a a complete world.
     * Bodies, Shapes, constraints and scripts form the world are added.
     * @param world the elements to add to this world
     */
    public Body[] addWorld(World world)
    {
        int maxId = world.mCurrentBodyId;
        Body[] bodyMapping = new Body[maxId];

        for( int i = 0; i < world.mBodyCount; i++)
        {
            Body b = world.mBodies[i].copy();
            addBody( b );
            b.calculateAABB(0);
            bodyMapping[world.mBodies[i].mId] = b;
        }

        //#NoEco /*
        for( int i = 0; i < world.mConstraintCount; i++)
        {
            addConstraint( world.mConstraints[i].copy(bodyMapping) );
        }
        //#NoEco */

        //#NoBasic /*
        for( int i = 0; i < world.mScriptCount; i++)
        {
            addScript( world.mScripts[i].copy() );
        }
        for( int i = 0; i < world.mScriptBodyCount; i++)
        {
            Body b = bodyMapping[world.mScriptBodies[i].mId];
            mScripts[world.mScriptIndex[i]].applyToBody(b, this);
            mScriptElementIndex[i] = world.mScriptElementIndex[i];
            mScriptExecutionIndex[i] = world.mScriptExecutionIndex[i];
        }
        //#NoBasic */

        //#NoBasic /*
        for( int i = 0;  i < world.mParticles.size(); i++)
        {
            addParticleEmitter( ((ParticleEmitter) world.mParticles.elementAt(i)).copy(bodyMapping) );
        }
        //#NoBasic */

        //#NoBasic /*
        for( int i = 0;  i < world.mForces.size(); i++)
        {
            addExternalForce( ((ExternalForce) world.mForces.elementAt(i)).copy(bodyMapping) );
        }
        //#NoBasic */

        //#NoBasic /*
        Vector events = world.mEventSet.getEvents();
        int eventCount = events.size();
        for( int i = 0;  i < eventCount; i++)
        {
            addEvent( ((Event) events.elementAt(i)).copy(bodyMapping) );
        }
        //#NoBasic */

        //#NoBasic /*
        //Landscape
        Landscape otherLandscape = world.getLandscape();
        for( int i = 0; i < otherLandscape.mSegmentCount; i++)
        {
            mLandscape.addSegment(
                    new FXVector( otherLandscape.mStartpoints[i] ),
                    new FXVector( otherLandscape.mEndpoints[i] ),
                    otherLandscape.mFaces[i]);
        }
        //#NoBasic */


        return bodyMapping;
    }

    /**
     * Adds a body to the world.
     * The body must not be registered on any other world.
     * @param body new Body.
     */
    public void addBody(Body body)
    {
        if (body != null)
        {
            body.mId = mCurrentBodyId++;

            mBodies = checkVector(mBodies, mBodyCount);
            mBodies[mBodyCount++] = body;

            mShapeSet.registerShape(body.mShape);

            sortBodyList();

            body.forceUpdate(mTimestepFX);
        }
    }

    /**
     * Sets the landscape for the world.
     * Only one landscape exists, containing all landscape elements.
     * @param landscape the landscape object.
     */
    //#NoEco /*
    public void setLandscape(Landscape landscape)
    {
        this.mLandscape = landscape;
    }
    //#NoEco */

    /**
     * Gets the landscape object.
     * @return the landscape object.
     */
    //#NoEco /*
    public Landscape getLandscape()
    {
        return mLandscape;
    }
    //#NoEco */

    /**
     * Adds a particle emitter to the world.
     * @param particleEmitter new particle emitter.
     */
    //#NoBasic /*
    public void addParticleEmitter(ParticleEmitter particleEmitter)
    {
        if (particleEmitter != null)
        {
            mParticles.addElement(particleEmitter);
        }
    }
    //#NoBasic */

    /**
     * Adds an external force to the world.
     * @param externalForce new external force.
     */
    //#NoBasic /*
    public void addExternalForce(ExternalForce externalForce)
    {
        if (externalForce != null)
        {
            mForces.addElement(externalForce);
        }
    }
    //#NoBasic */


    /**
     * Removes a body from the world.
     * This also removes all constraints, contacts and scripts relating to this body.
     * @param body the body to remove.
     */
    public void removeBody(Body body)
    {
        if (body.mId < 0)
        {
            return;
        }

        for( int i = 0; i < mBodyCount; i++)
        {
            if (mBodies[i] == body)
            {
                mBodies[i] = null;
                break;
            }
        }
        mBodyCount = compactVector(mBodies, mBodyCount);
        sortBodyList();

        //#NoBasic /*
        for( int i = 0; i < mScriptBodyCount; i++)
        {
            if (mScriptBodies[i] == body)
            {
                mScriptElementIndex[i] = -1;
                mScriptExecutionIndex[i] = -1;
                mScriptIndex[i] = -1;
                mScriptBodies[i] = null;

                compactVector(mScriptElementIndex, mScriptBodyCount);
                compactVector(mScriptExecutionIndex, mScriptBodyCount);
                compactVector(mScriptIndex, mScriptBodyCount);
                mScriptBodyCount = compactVector(mScriptBodies, mScriptBodyCount);
            }
        }
        //#NoBasic */

        //#NoEco /*
        int numConstrains = mConstraintCount;
        for( int i = 0; i < numConstrains; i++)
        {
            if (mConstraints[i].concernsBody(body))
            {
                mConstraints[i] = null;
            }
        }
        mConstraintCount = compactVector(mConstraints, mConstraintCount);
        //#NoEco */

        //#NoBasic /*
        int numParticles = mParticles.size();
        for( int i = numParticles - 1; i >= 0; i--)
        {
            ParticleEmitter emitter = (ParticleEmitter) mParticles.elementAt(i);
            if (emitter.getEmitter() == body)
            {
                mParticles.removeElementAt(i);
            }
        }
        //#NoBasic */

        int numContacts = mContactCount;
        for( int i = 0; i < numContacts; i++)
        {
            if (mContacts[i].concernsBody(body))
            {
                mContacts[i] = null;
            }
        }
        mContactCount = compactVector(mContacts, mContactCount);

    }

    /**
     * Removes a constraint from the world.
     * @param c the constraint to remove.
     */
    //#NoEco /*
    public void removeConstraint(Constraint c)
    {
        int numConstrains = mConstraintCount;
        for( int i = 0; i < numConstrains; i++)
        {
            if (mConstraints[i] == c)
            {
                mConstraints[i] = null;
            }
        }
        mConstraintCount = compactVector(mConstraints, mConstraintCount);
    }
    //#NoEco */

    /**
     * Removes a script from the world.
     * This also removes all current instances of the script.
     * @param script the script to remove.
     */
    //#NoBasic /*
    public void removeScript(Script script)
    {
        int scriptIndex = -1;
        for( int i = 0; i < mScriptCount; i++)
        {
            if (mScripts[i] == script)
            {
                mScripts[i] = null;
                scriptIndex = i;

                mScriptCount--;
                for (int j = i; j < mScriptCount; j++)
                {
                    mScripts[j] = mScripts[j+1];
                }

                break;
            }
        }

        for( int i = 0; i < mScriptBodyCount; i++)
        {
            if (mScriptElementIndex[i] == scriptIndex)
            {
                mScriptElementIndex[i] = -1;
                mScriptExecutionIndex[i] = -1;
                mScriptIndex[i] = -1;
                mScriptBodies[i] = null;

                compactVector(mScriptElementIndex, mScriptBodyCount);
                compactVector(mScriptExecutionIndex, mScriptBodyCount);
                compactVector(mScriptIndex, mScriptBodyCount);
                mScriptBodyCount = compactVector(mScriptBodies, mScriptBodyCount);
                break;
            }
        }
    }
    //#NoBasic */

    /**
     * Removes an event from the world.
     * @param e the event to remove.
     */
    //#NoBasic /*
    public void removeEvent(Event e)
    {
        mEventSet.removeEvent(e);
    }
    //#NoBasic */

    /**
     * Removes a particle emitter from the world.
     * @param particleEmitter particle emitter to remove.
     */
    //#NoBasic /*
    public void removeParticleEmitter(ParticleEmitter particleEmitter)
    {
        if (particleEmitter != null)
        {
            mParticles.removeElement(particleEmitter);
        }
    }
    //#NoBasic */

    /**
     * Removes an external force from the world.
     * @param externalForce external force to remove.
     */
    //#NoBasic /*
    public void removeExternalForce(ExternalForce externalForce)
    {
        if (externalForce != null)
        {
            mForces.removeElement(externalForce);
        }
    }
    //#NoBasic */

    /**
     * Increases vector size if required.
     * Internal Usage - Check an array for size and resize if required
     * @param vector the array to check
     * @param checkSize the current size of the vector
     * @return the new vector if it was resized, the same if it was okay
     */
    static final int[] checkVector(int[] vector, int checkSize)
    {
        if (vector.length <= checkSize)
        {
            int[] newVector = new int[vector.length + M_WORLD_ARRAY_INCREMENT];
            System.arraycopy(vector, 0, newVector, 0, vector.length);
            return newVector;
        }
        return vector;
    }

    /**
     * Increases vector size if required.
     * Internal Usage - Check an array for size and resize if required
     * @param vector the array to check
     * @param checkSize the current size of the vector
     * @return the new vector if it was resized, the same if it was okay
     */
    static final short[] checkVector(short[] vector, int checkSize)
    {
        if (vector.length <= checkSize)
        {
            short[] newVector = new short[vector.length + M_WORLD_ARRAY_INCREMENT];
            System.arraycopy(vector, 0, newVector, 0, vector.length);
            return newVector;
        }
        return vector;
    }

    /**
     * Increases vector size if required.
     * Internal Usage - Check an array for size and resize if required
     * @param vector the array to check
     * @param checkSize the current size of the vector
     * @return the new vector if it was resized, the same if it was okay
     */
    static final Body[] checkVector(Body[] vector, int checkSize)
    {
        if (vector.length <= checkSize)
        {
            Body[] newVector = new Body[vector.length + M_WORLD_ARRAY_INCREMENT];
            System.arraycopy(vector, 0, newVector, 0, vector.length);
            return newVector;
        }
        return vector;
    }

    /**
     * Increases vector size if required.
     * Internal Usage - Check an array for size and resize if required
     * @param vector the array to check
     * @param checkSize the current size of the vector
     * @return the new vector if it was resized, the same if it was okay
     */
    static final Contact[] checkVector(Contact[] vector, int checkSize)
    {
        if (vector.length <= checkSize)
        {
            Contact[] newVector = new Contact[vector.length + M_WORLD_ARRAY_INCREMENT];
            System.arraycopy(vector, 0, newVector, 0, vector.length);
            return newVector;
        }
        return vector;
    }

    /**
     * Increases vector size if required.
     * Internal Usage - Check an array for size and resize if required
     * @param vector the array to check
     * @param checkSize the current size of the vector
     * @return the new vector if it was resized, the same if it was okay
     */
    //#NoEco /*
    static final Constraint[] checkVector(Constraint[] vector, int checkSize)
    {
        if (vector.length <= checkSize)
        {
            Constraint[] newVector = new Constraint[vector.length + M_WORLD_ARRAY_INCREMENT];
            System.arraycopy(vector, 0, newVector, 0, vector.length);
            return newVector;
        }
        return vector;
    }
    //#NoEco */

    /**
     * Increases vector size if required.
     * Internal Usage - Check an array for size and resize if required
     * @param vector the array to check
     * @param checkSize the current size of the vector
     * @return the new vector if it was resized, the same if it was okay
     */
    //#NoBasic /*
    static final Script[] checkVector(Script[] vector, int checkSize)
    {
        if (vector.length <= checkSize)
        {
            Script[] newVector = new Script[vector.length + M_WORLD_ARRAY_INCREMENT];
            System.arraycopy(vector, 0, newVector, 0, vector.length);
            return  newVector;
        }
        return vector;
    }
    //#NoBasic */

    /**
     * Increases vector size if required.
     * Internal Usage - Check an array for size and resize if required
     * @param vector the array to check
     * @param checkSize the current size of the vector
     * @return the new vector if it was resized, the same if it was okay
     */
    static final FXVector[] checkVector(FXVector[] vector, int checkSize)
    {
        if (vector.length <= checkSize)
        {
            FXVector[] newVector = new FXVector[vector.length + M_WORLD_ARRAY_INCREMENT];
            System.arraycopy(vector, 0, newVector, 0, vector.length);
            return  newVector;
        }
        return vector;
    }

    /**
     * Find reference to the body
     * @return reference to the body
     */
    public Body findBodyById(int id)
    {
        int index = bodyIndexOf(id);
        if (index < 0)
        {
            return null;
        }

        return mBodies[index];
    }

    /**
     * Find reference to the body
     * @return reference to the body or null if the body does not exist in theis world
     */
    protected Body findBody(Body b)
    {
        if (b == null)
        {
            return null;
        }
        int index = bodyIndexOf(b.mId);
        if (index < 0)
        {
            return null;
        }

        return mBodies[index];
    }

    /**
     * Finds the body at position x,y. <b>
     * Note: If more than one body are occupying that place the result is not defined.
     * @param xFX the x-position (FX)
     * @param yFX the y-position (FX)
     * @return a body at that position or null if none is there
     */
    public Body findBodyAt(int xFX, int yFX )
    {
        //TODO: optimize this!
        Body[] bodies = getBodies();
        FXVector pos = new FXVector();

        for( int i = 0; i < getBodyCount(); i++)
        {
            Body b = bodies[i];
            pos.assignFX(xFX, yFX);
            if (xFX < b.getAABBMinXFX() ||
                xFX > b.getAABBMaxXFX() ||
                yFX < b.getAABBMinYFX() ||
                yFX > b.getAABBMaxYFX() )
            {
                continue;
            }

            FXVector[] axes = b.getAxes();
            FXVector[] vertices = b.getVertices();
            boolean inside = true;
            for( int j = 0; j < axes.length; j++)
            {
                long refProjectionFX = pos.dotFX(axes[j]);
                long minFX = vertices[0].dotFX(axes[j]);
                long maxFX = minFX;
                for( int k = 1; k < vertices.length; k++)
                {
                    long dotFX = vertices[k].dotFX(axes[j]);
                    if (minFX > dotFX) minFX = dotFX;
                    if (maxFX < dotFX) maxFX = dotFX;
                }
                if (minFX > refProjectionFX || maxFX < refProjectionFX)
                {
                    inside = false;
                    break;
                }
            }
            if (inside)
                return b;
        }

        return null;
    }


    /**
     * Gets the index of the body. <b>
     * Note: This index can vary throughout the simulation!
     * @param id identifier of the body
     * @return the current index of the body.
     */
    public int bodyIndexOf(int id)
    {
        //TODO: test and use binary search with sorting info
        /*int lowerIdx = 0;
        int upperIdx = bodyCount - 1;
        int newIdx = 0;
        while(lowerIdx + 1 < upperIdx)
        {
            newIdx = (upperIdx + lowerIdx) / 2;
            if (bodies[newIdx].positionFX.xFX < b.positionFX.xFX)
            {
                lowerIdx = newIdx + 1;
            }
            else
            {
                upperIdx = newIdx;
            }
        }

        if (bodies[newIdx].positionFX.xFX == b.positionFX.xFX &&
            bodies[newIdx].positionFX.yFX < b.positionFX.yFX)
        {
            return bodies[newIdx];
        }*/

        for( int i = 0; i < mBodyCount; i++)
        {
            if (mBodies[i].mId == id)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets reference to the constraint.
     * Finds an equal constraint in this world.
     * @return reference to the constraint or null if none was found.
     */
    //#NoEco /*
    protected Constraint findConstraint(Constraint c)
    {
        int index = indexOf(c);
        if (index < 0)
        {
            return null;
        }

        return mConstraints[index];
    }
    //#NoEco */

    /**
     * Gets the index of the constraint.
     * @param c the constraint.
     * @return the index of the constraint.
     */
    //#NoEco /*
    public int indexOf(Constraint c)
    {
        if (c == null)
        {
            return -1;
        }

        for( int i = 0; i < mConstraintCount; i++)
        {
            if (mConstraints[i].equals(c))
            {
                return i;
            }
        }
        return -1;
    }
    //#NoEco */

    /**
     * Adds a constraint to the world.
     * @param constraint the constrain.
     */
    //#NoEco /*
    public void addConstraint(Constraint constraint)
    {
        if (constraint != null)
        {
            mConstraints = checkVector(mConstraints, mConstraintCount);
            mConstraints[mConstraintCount++] = constraint;
        }
    }
    //#NoEco */

    /**
     * Adds a script definition to the world.
     * @param script the script definition.
     */
    //#NoBasic /*
    public void addScript(Script script)
    {
        if (script != null)
        {
            mScripts = checkVector(mScripts, mScriptCount);
            mScripts[mScriptCount++] = script;
        }
    }
    //#NoBasic */

    /**
     * Adds a body for script execution.
     * @param index the index of the script.
     * @param b the body to which the script applies to.
     */
    //#NoBasic /*
    protected void addScriptBody(int index, Body b)
    {
        mScriptIndex[mScriptBodyCount] = index;
        mScriptBodies[mScriptBodyCount++] = b;
    }
    //#NoBasic */

    /**
     * Adds an event definition to the world.
     * @param event the event definition.
     */
    //#NoBasic /*
    public void addEvent(Event event)
    {
        mEventSet.registerEvent(event);
    }
    //#NoBasic */

    /**
     * Sets the gravity.
     * The unit is in pixel/sec^2.
     * @fx
     * @param gravity the new gravity. The vector will be in downward direction.
     */
    public void setGravity(int gravity)
    {
        this.mGravity = new FXVector(0, gravity << FXUtil.DECIMAL);
    }

    /**
     * Sets the gravity vector.
     * The gravity can point in any direction.
     * The unit is in pixel/sec^2.
     * @param gravity the new gravity.
     */
    public void setGravity(FXVector gravity)
    {
        this.mGravity = gravity;
    }

    /**
     * Sets lateral damping factor (FX).
     * The damping factor controls background damping of all motion. <br>
     * Each lateral velocity is decelerated by that factor each timestep.
     * Reasonable values range from 0.0..0.005.
     * @fx
     * @param dampingFX damping factor
     * @see FXUtil
     */
    public void setDampingLateralFX(int dampingFX)
    {
        this.mDampingLinearFX = dampingFX;
    }

    /**
     * Sets rotational damping factor (FX).
     * The damping factor controls background damping of all motion. <br>
     * Each rotational velocity is decelerated by that factor each timestep.
     * Reasonable values range from 0.0..0.005.
     * @fx
     * @param dampingFX damping factor
     * @see FXUtil
     */
    public void setDampingRotationalFX(int dampingFX)
    {
        this.mDampingRotationalFX = dampingFX;
    }


    /**
     * Performs a single step in the simulation.
     * The following steps are performed.
     * <ul>
     * <li>Integrate Positions. </li>
     * <li>Check for collisions. </li>
     * <li>Add Forces (Gravity). </li>
     * <li>Solve constraint equations iteratively. </li>
     * </ul>
     */
    public void tick()
    {
        Body[] bodies = this.mBodies;
        Constraint[] constraints= this.mConstraints;                //#NoEco

        //execute scripts
        //#NoBasic /*
        for( int i = 0; i < mScriptBodyCount; i++)
        {
            if (mScriptBodies[i] != null)
            {
                mScripts[mScriptIndex[i]].executeScript(i, this);
            }
        }
        //#NoBasic */

        //integrate forces to get first estimation velocity
        //apply external forces
        //#NoBasic /*
        for(int i = 0; i < mForces.size(); i++)
        {
            ((ExternalForce) mForces.elementAt(i)).applyForce(bodies, mBodyCount, mTimestepFX);
        }
        //#NoBasic */
        for(int i = mBodyStartIndex; i < mBodyEndIndex; i++)
        {
            Body b = bodies[i];
            if (b.mDynamic && b.mGravityAffected)
            {
                b.applyAcceleration(mGravity, mTimestepFX);
            }
            b.calculateAABB(mTimestepFX);
        }

        //check for collisions
        checkCollisions();

        Contact.applyAccumImpulses(mContacts, mContactCount);

        //#NoEco /*
        //check contacts
        Contact.checkAllContacts(mContacts, mContactCount, mLandscape);

        //#NoEco */

        //#NoBasic /*
        for( int i = 0; i < mParticles.size(); i++)
        {
            ((ParticleEmitter)mParticles.elementAt(i)).applyAcceleration(mGravity, mTimestepFX);
        }
        collideParticles();
        //#NoBasic */

        //precaculate contacts
        for( int i = 0; i < mContactCount; i++ )
        {
            mContacts[i].precalculate(mInvTimestepFX);
        }

        //#NoEco /*
        for( int i = 0; i < mConstraintCount; i++ )
        {
            constraints[i].precalculate(mInvTimestepFX);
        }
        //#NoEco */

        //solve and apply constraint forces (collision)
        boolean iterationDone = false;
        for( M_iteration = 0;
            M_iteration < mConstraintIterations &&
            (! iterationDone || ! mDynamicConstraintIteration);
            //&& (mIterationTimeMillis <= 0 || iterationStart + mIterationTimeMillis > System.nanoTime() || M_iteration < mMinConstraintIterations);
            ++M_iteration)
        {
            iterationDone = true;
            //#NoEco /*
            for( int j = 0; j < mConstraintCount; j++ )
            {
                iterationDone &= constraints[j].applyMomentum(mInvTimestepFX);
            }
            //#NoEco */
            //Contacts have higher priority than other constraints
            for( int j = 0; j < mContactCount; j++)
            {
                iterationDone &= mContacts[j].applyMomentum();
            }
        }


        //#NoEco /*
        for( int i = 0; i < mConstraintCount; i++ )
        {
            constraints[i].postStep();
        }
        //#NoEco */


        ////////////////////////////////////////////////////////////
        int dampingFactorLinearFX = FXUtil.ONE_FX - mDampingLinearFX;
        int dampingFactorRotationalFX = FXUtil.ONE_FX - mDampingRotationalFX;
        //integrate positions
        for(int i = mBodyStartIndex; i < mBodyEndIndex; i++)
        {
            bodies[i].integrateVelocity(mTimestepFX);
            bodies[i].updateVelocity(dampingFactorLinearFX, dampingFactorRotationalFX);
            //bodies[i].checkResting();
        }

        /////////correct position

        for( int i = 0; i < mContactCount; i++ )
        {
            mContacts[i].precalculatePositionCorrection(mTimestepFX, mInvTimestepFX);
        }

        long maxCorrectFX = 0, currCorrectFX = 0;
        for( M_iteration = 0; M_iteration < mPositionConstraintIterations; ++M_iteration)
        {
            for( int j = 0; j < mContactCount; j++)
            {
                currCorrectFX = mContacts[j].applyMomentumPositionCorrectionFX();
                maxCorrectFX = maxCorrectFX < currCorrectFX ? currCorrectFX : maxCorrectFX;
            }

            if (maxCorrectFX < M_CONTACT_positionCorrectThresholdFX)
            {
                break;
            }
        }

        //adjust positions
        for(int i = mBodyStartIndex; i < mBodyEndIndex; i++)
        {
            //System.out.println("E before: " + getBodyTotalEnergyFX(bodies[i]));
            bodies[i].integrateVirtualVelocity(mTimestepFX, mGravity);
            //System.out.println("E after:  " + getBodyTotalEnergyFX(bodies[i]));
        }

        //#NoBasic /*
        for( int i = 0; i < mParticles.size(); i++)
        {
            ((ParticleEmitter)mParticles.elementAt(i)).integrateParticles(mTimestepFX);
        }
        //#NoBasic */

        //
        //long kinE = bodies[0].velocityFX().lengthSquareFX() / 2;
        //long potE = -FXUtil.multFX(bodies[0].positionFX().yFX, mGravity.yFX);
        //System.out.println("Energy : " + kinE + " - "+ potE + " => " + (kinE + potE));
        //////////////////////////////////////////////////////

        //check all events
        //#NoBasic /*
        if (mListener != null)
        {
            mEventSet.checkEvents(this, mListener);
        }
        //#NoBasic */

    }

    /**
     * Checks all bodies for collisions.
     */
    protected void checkCollisions()
    {
        //check existing contacts and add new ones
        for( int i = 0; i < mContactCount;i++)
        {
            mContacts[i] = null;
        }
        mContactCount = 0;
        int startContactCount = mContactCount;

        mLandscape.initCollision();          //#NoEco

        //sorting of body Vector
        //works almost O(n) due to minimal changes with small timesteps
        sortBodyList();

        //walk through sorted body vector and add open bodies/collide pairs
        //long start = System.nanoTime();
        int openCnt = 0;
        int openCheckSize = 0;
        for( int i = mBodyStartIndex; i < mBodyEndIndex; i++)
        {
            int currValFX = mBodies[i].mAABBMinXFX;
            boolean toInsert = true;
            mLandscape.collisionCheckBody(this, mBodies[i]);      //#NoEco

            //delete entries from openlist, add curr entry
            for( int j = 0; j < openCheckSize; j++)
            {
                if ( mCurrentOpen[j] == null)
                {
                    if (!toInsert)
                    {
                        mCurrentOpen[j] = mBodies[i];
                        openCnt++;
                        toInsert = false;
                    }
                    continue;
                }
                if (mCurrentOpen[j].mAABBMaxXFX < currValFX)
                {
                    openCnt--;
                    mCurrentOpen[j] = null;
                }
                //check other dimension of AABB
                else if (! (mBodies[i].mAABBMinYFX > mCurrentOpen[j].mAABBMaxYFX
                         || mCurrentOpen[j].mAABBMinYFX > mBodies[i].mAABBMaxYFX) )
                {
                    //detailed collide
                    checkBodyPair(mBodies[i], mCurrentOpen[j]);
                }
            }
            if (toInsert)
            {
                mCurrentOpen = checkVector(mCurrentOpen, openCheckSize);
                mCurrentOpen[openCheckSize++] = mBodies[i];
                openCnt++;
            }
            if (openCheckSize > openCnt * 2)
            {
                openCheckSize = compactVector(mCurrentOpen, openCheckSize);
            }
        }

        //delete and update contacts stored in bodies
        mLandscape.resetContacts();                              //#NoEco
        for(int i = mBodyStartIndex; i < mBodyEndIndex; i++)
        {
            mBodies[i].resetContacts();
        }


        Body landscapeBody = mLandscape.getBody();   //#NoEco
        for( int i = startContactCount; i < mContactCount; i++)
        {
            Contact c = mContacts[i];
            c.mIsNew = false;        //mark contacts as old -> used for next step -> so we do not have to create so many objects
            c.mBody1.addContact(c);
            c.mBody2.addContact(c);
        }


        //_x_ order contacts
        //#ContactOrdering
        /*for( int i = 0; i < mContactCount; i++)
        {
            for( int j = i; j < mContactCount; j++)
            {
                int val1 = mContacts[i].getContactPosition1().yFX;
                int val2 = mContacts[j].getContactPosition1().yFX;

                if (val1 < val2)
                {
                    Contact c = mContacts[i];
                    mContacts[i] = mContacts[j];
                    mContacts[j] = c;
                }
            }
        }*/
    }

    //#NoBasic /*
    protected void collideParticles()
    {
        for( int particle = 0; particle < mParticles.size(); particle++)
        {
            ParticleEmitter particleEmitter = ((ParticleEmitter)mParticles.elementAt(particle));
            if (! particleEmitter.mCanCollide)
            {
                continue;
            }
            int particleStartIdx = 0;
            int particleIdx = 0;
            for( int i = mBodyStartIndex; i < mBodyEndIndex; i++)
            {
                while ( particleStartIdx < particleEmitter.mMaxParticleCount &&
                       (particleEmitter.mLife[particleStartIdx] <= 0 ||
                       (mBodies[i].mAABBMinXFX > particleEmitter.mXFX[particleStartIdx])) )
                {
                    particleStartIdx++;
                }

                particleIdx = particleStartIdx;
                while ( particleIdx < particleEmitter.mMaxParticleCount &&
                       (particleEmitter.mLife[particleIdx] <= 0 ||
                       (mBodies[i].mAABBMaxXFX > particleEmitter.mXFX[particleIdx])) )
                {
                    if (particleEmitter.mLife[particleIdx] > 0 &&
                        mBodies[i].mAABBMinYFX < particleEmitter.mYFX[particleIdx] &&
                        mBodies[i].mAABBMaxYFX > particleEmitter.mYFX[particleIdx] &&
                        mBodies[i].mInteracting)
                    {
                        FXVector normal = Collision.detectCollision(mBodies[i], particleEmitter.mXFX[particleIdx], particleEmitter.mYFX[particleIdx]);
                        if (normal != null)
                        {
                            particleEmitter.collide(normal, particleIdx);
                        }
                    }
                    particleIdx++;
                }
            }
            //collide with landscape
            mLandscape.collideParticles(particleEmitter);
        }
    }
    //#NoBasic */

    /**
     * Compacts a vector.
     * Internal Usage - fill null pointer in open vector
     * @param vectorSize current size of the vector
     * @return the reduced size
     */
    static final int compactVector(Object[] vector, int vectorSize)
    {
        int finalSize = vectorSize;
        int i, j;
        for(i = 0, j = vectorSize -1; i < j; j--)
        {
            while(vector[i] != null && i < j) i++;
            while(vector[j] == null && i < j) j--;

            if (i < j)
            {
                vector[i] = vector[j];
                vector[j] = null;
            }
            if (vector[j] == null)
            {
            finalSize = j;
            }
        }

        while (finalSize > 0 && vector[finalSize-1] == null)
        {
            finalSize--;
        }
        return finalSize;
    }

    /**
     * Compacts a vector.
     * Internal Usage - fill -1 in open vector
     * @param vectorSize current size of the vector
     * @return the reduced size
     */
    static final int compactVector(int[] vector, int vectorSize)
    {
        int finalSize = vectorSize;
        int i, j;
        for(i = 0, j = vectorSize -1; i < j; j--)
        {
            while(vector[i] != -1 && i < j) i++;
            while(vector[j] == -1 && i < j) j--;

            if (i < j)
            {
                vector[i] = vector[j];
                vector[j] = -1;
                finalSize = j;
            }
        }
        return finalSize;
    }

    /**
     * Sorts the body list.
     * Internal Usage - sort the body list <br>
     * A simple linear sort is used.
     * In this specific case - almost sorted lists - the algorithm scales nearly with O(n)
     */
    private final void sortBodyList()
    {
        Body currBody;
        int  j = 0;
        for( int i = 1; i < mBodyCount; i++)
        {
            currBody = mBodies[i];
            for( j = i - 1; j >= 0 && mBodies[j].mAABBMinXFX > currBody.mAABBMinXFX; j--)
            {
                //swap (j + 1, j)
                mBodies[j + 1] = mBodies[j];
            }
            mBodies[j + 1] = currBody;
        }

        //set the simulation area correctly
        //TODO: optimize using binary search
        mBodyStartIndex = -1;
        mBodyEndIndex = mBodyCount;
        for( int i = 0; i < mBodyCount; i++)
        {
            if (mBodyStartIndex < 0 && mBodies[i].mAABBMaxXFX > mAreaStartFX )
            {
                mBodyStartIndex = i;
            }
            if (mBodies[i].mAABBMinXFX < mAreaEndFX)
            {
                mBodyEndIndex = i + 1;
            }
        }
        if (mBodyStartIndex < 0)
        {
            mBodyStartIndex = 0;
        }
    }

    /**
     * Checks whether two bodies touch (or intersect).
     * Usually the calling routine already has checked the AABBs
     * @param body1 Body 1 to check
     * @param body2 Body 2 to check
     */
    private final void checkBodyPair(Body body1, Body body2)
    {
        if ( (! body1.mDynamic && ! body2.mDynamic) ||
              (body1.mColissionBitFlag & body2.mColissionBitFlag) != 0 ||
              ! body1.mInteracting || ! body2.mInteracting)
        {
            return;
        }

        //check for multishape
        if (body1.mShape instanceof MultiShape || body2.mShape instanceof MultiShape)
        {
            int shapeCount1 = 1;
            int shapeCount2 = 1;
            //check for multishape
            if (body1.mShape instanceof MultiShape )
            {
                shapeCount1 = ((MultiShape) body1.mShape).getShapeCount();
            }
            if (body2.mShape instanceof MultiShape )
            {
                shapeCount2 = ((MultiShape) body2.mShape).getShapeCount();
            }
            Contact newContact;
            for( int i = 0; i < shapeCount1; i++)
            {
                for( int j = 0; j < shapeCount2; j++)
                {
                    newContact = Collision.detectCollision( body1, i, body2, j);
                    if (newContact != null)
                    {
                        mContacts = checkVector(mContacts, mContactCount);
                        mContacts[mContactCount] = newContact;
                        mContactCount++;
                    }
                }
            }
        }
        else
        {
            Contact newContact = Collision.detectCollision( body1, body2);
            if (newContact != null)
            {
                mContacts = checkVector(mContacts, mContactCount);
                mContacts[mContactCount] = newContact;
                mContactCount++;
            }
        }
    }

    /**
     * Gets the gravity vector.
     * @return the gravity vector.
     */
    public FXVector getGravity()
    {
        return mGravity;
    }

    /**
     * Gets the lateral damping factor.
     * @return the damping factor (FX)
     */
    public int getDampingLateralFX()
    {
        return mDampingLinearFX;
    }

    /**
     * Gets the rotational damping factor.
     * @return the damping factor (FX)
     */
    public int getDampingRotationalFX()
    {
        return mDampingRotationalFX;
    }

    /**
     * Loads a constraint from a stream.
     * The method decides which constraint to load and calls into the respective class.
     * @param reader the file reader.
     * @param bodies the body vector for correct referencing of bodies in the Constraint.
     * @return the loaded Constraint.
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static Constraint loadConstraint(PhysicsFileReader reader, Vector bodies, UserData userData)
    {
        int type = reader.next();
        switch(type)
        {
        case Constraint.JOINT:
            return Joint.loadJoint(reader, bodies, userData);
        case Constraint.SPRING:
            return Spring.loadSpring(reader, bodies, userData);
        case Constraint.MOTOR:
            return Motor.loadMotor(reader, bodies, userData);
        default:
            return null;
        }
    }
    //#WorldLoadingOFF */
    //#NoBasic */

   /**
    * Gets a vector containing all events.
    * @return a vector containing all events.
    */
    //#NoBasic /*
    public Vector getEvents()
    {
        return mEventSet.getEvents();
    }
    //#NoBasic */

    /**
     * Gets a vector containing all particle emitters.
     * @return a vector containing all particle emitters.
     */
     //#NoBasic /*
     public Vector getParticleEmitters()
     {
         return mParticles;
     }
     //#NoBasic */

    /**
     * Gets the start position of the area simulation.
     * @return start x coordinate (FX) of the area simulation.
     */
    protected int getAreaStartFX()
    {
        return mAreaStartFX;
    }

    /**
     * Gets the end position of the area simulation.
     * @return end x coordinate (FX) of the area simulation.
     */
    protected int getAreaEndFX()
    {
        return mAreaEndFX;
    }

    /**
     * Gets the number of bodies.
     * @return the number of bodies in the world.
     */
    public int getBodyCount()
    {
        return mBodyCount;
    }

    /**
     * Gets all bodies.
     * @return a list containing bodies.
     */
    public Body[] getBodies()
    {
        return mBodies;
    }

    /**
     * Gets the start index of the currently active bodies.
     * @return the start index of the currently active bodies.
     */
    public int getBodyStartIndex()
    {
        return mBodyStartIndex;
    }

    /**
     * Gets the end index of the currently active bodies.
     * @return the end index of the currently active bodies.
     */
    public int getBodyEndIndex()
    {
        return mBodyEndIndex;
    }

    /**
     * Gets the number of constraints.
     * @return the numberof constraints.
     */
    //#NoEco /*
    public int getConstraintCount()
    {
        return mConstraintCount;
    }
    //#NoEco */

    /**
     * Gets all constraints.
     * @return A list containing all constraints.
     */
    //#NoEco /*
    public Constraint[] getConstraints()
    {
        return mConstraints;
    }
    //#NoEco */

    /**
     * Gets the number of current contacts.
     * @return the number of all current contacts.
     */
    public int getContactCount()
    {
        return mContactCount;
    }

    /**
     * Gets all current contacts.
     * @return a list containing all current contacts.
     */
    public Contact[] getContacts()
    {
        return mContacts;
    }

    /**
     * Gets the number of scripts.
     * @return the number of scripts.
     */
    //#NoBasic /*
    public int getScriptCount()
    {
        return mScriptCount;
    }
    //#NoBasic */

    /**
     * Gets all scripts.
     * @return a list containing all scripts.
     */
    //#NoBasic /*
    public Script[] getScripts()
    {
        return mScripts;
    }
    //#NoBasic */

    /**
     * Gets a script.
     * @param index index of the script
     * @return the script at the index.
     */
    //#NoBasic /*
    public Script getScript(int index)
    {
        return mScripts[index];
    }
    //#NoBasic */

    /**
     * Gets the number of bodies that scripts are applied to.
     * @return the number of bodies that scripts are applied to.
     */
    //#NoBasic /*
    protected int getScriptBodyCount()
    {
        return mScriptBodyCount;
    }
    //#NoBasic */

    /**
     * Gets a list of all bodies that scripts are applied to.
     * @return a list of all bodies that scripts are applied to.
     */
    //#NoBasic /*
    protected Body[] getScriptBodies()
    {
        return mScriptBodies;
    }
    //#NoBasic */

    /**
     * Gets a list of all applied script indices.
     * This list runs along with the script body list and determines which script is applied to teh body.
     * @return a list of all applied script indices.
     */
    //#NoBasic /*
    protected int[] getScriptIndices()
    {
        return mScriptIndex;
    }
    //#NoBasic */

    /**
     * Gets all current contacts for a body.
     * Note: this method is currently expensive due to object creation.
     * @param b the body.
     * @return a list containing all contacts.
     */
    public Contact[] getContactsForBody(Body b)
    {
        Contact[] contacts = new Contact[M_BODY_MAX_CONTACTS];
        int contactsCount = 0;

        Contact[] direct = b.getContacts();
        for( int i = 0; i < direct.length && direct[i] != null; i++)
        {
            contacts[contactsCount++] = direct[i];
        }

        //is not required anymore: all contact references are kept within the bodies
        /*
        //#NoEco
        Contact[] landscapeContacts = landscape.contacts;
        for( int i = 0; i < landscapeContacts.length && landscapeContacts[i] != null; i++)
        {
            if (landscapeContacts[i].concernsBody(b))
            {
                contacts = checkVector(contacts, contactsCount);
                contacts[contactsCount++] = landscapeContacts[i];
            }
        }

        */

        return contacts;
    }

    /**
     * Method to displace all bodies in the world by a given vector.
     * @param translation vector for body translation
     */
    public void translate(FXVector translation)
    {
        int bodyCount = getBodyCount();
        Body[] bodies = getBodies();
        for( int i = 0; i < bodyCount; i++)
        {
            bodies[i].positionFX().add( translation);
            bodies[i].calculateAABB(0);
        }
    }

    /**
     * Get the body energy.
     * Returns the total energy (potential and kinetic) for a body.
     * Position (0/0) is used as base line for potential energy.
     * @fx
     * @param b the body
     * @return the energy
     */
    public long getBodyTotalEnergyFX(Body b)
    {
        long kinEFX = b.velocityFX().lengthSquareFX() / 2;
        long potEFX = -FXUtil.multFX(b.positionFX().yFX, mGravity.yFX);
        potEFX += -FXUtil.multFX(b.positionFX().xFX, mGravity.xFX);
        return (kinEFX + potEFX);
    }
}
