package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;


/**
 * The Event class represents a generic event that can be triggered in the world.
 * <br>
 * The world checks all registered event for its triggers.
 * The event can have filters:
 * <ul>
 * <li>body</li>
 * <li>shape</li>
 * </ul>
 * The event has a trigger target (see below):
 * <ul>
 * <li>velocity</li>
 * <li>position</li>
 * </ul>
 *
 * depending on type, the meaning of the targets is different <br>
 * <pre>
 * - TYPE_BODY_POSITION
 *   - target1, target2 -> upper left edge of check area
 *   - target3, target4 -> lower right edge of check area
 * - TYPE_BODY_VELOCITY
 *   - velocity in pixel per second
 *   - target1, target2 -> lower upper limit of speed
 *   - target3, target4 -> irrelevant
 * - TYPE_BODY_ROTATION
 *   - target1, target2 -> lower and upper limit for the rotation (rotation is always [0;360]
 *   - target3, target4 -> irrelevant
 * - TYPE_BODY_ANGULARVELOCITY
 *   - target1, target2 -> lower and upper limit for the angular velocity (in degrees per second), can be negative
 *   - target3, target4 -> irrelevant
 * - TYPE_BODY_COLLISION
 *   -  target1 > 0 -> trigger upon collisions north (negative y)
 *   -  target2 > 0 -> trigger upon collisions east (positive x)
 *   -  target3 > 0 -> trigger upon collisions south (position y)
 *   -  target4 > 0 -> trigger upon collisions west (negative x)
 * - TYPE_BODY_COLLISION_RELATIVE (this event is created using its particular method)
 *   -  target1, target2 -> the default direction
 *   -  target3 -> the allowed deviation from the direction
 *</pre>
 *
 * An event can be "trigger once" which causes the event to be raised exactly
 * once as long as the condition is met.
 * When at some time the condition is not met anymore, the event can be raised again for this parameter. <br>
 * Note: The trigger once functionality uses up memory and computational resources.
 *
 * @author Alexander Adensamer
 */
public class Event
{

    //Event Types
    /**
     * Event type position.
     */
    public static final int TYPE_BODY_POSITION = 0;
    /**
     * Event type velocity.
     */
    public static final int TYPE_BODY_VELOCITY = 1;

    /**
     * Event type rotation.
     */
    public static final int TYPE_BODY_ROTATION = 2;
    /**
     * Event type angular velocity.
     */
    public static final int TYPE_BODY_ANGULARVELOCITY = 3;
    /**
     * Event type collision.
     */
    public static final int TYPE_BODY_COLLISION = 4;
    /**
     * Event type collision.
     * Used for relative collision.
     */
    public static final int TYPE_BODY_COLLISION_RELATIVE = 6;
    /**
     * Event type area collision.
     * This event type is triggered when a body enters a region defined by another body.
     */
    public static final int TYPE_BODY_SENSOR = 5;

    /**
     * Event type constraint force.
     */
    public static final int TYPE_CONSTRAINT_FORCE = 10;

    /**
     * The type of the event.
     */
    private int mType;
    /**
     * Event Identifier.
     * As long as the event is not registered on the world, this is -1.
     * Do not use the same event in multiple world simultaneously (unless exactly the same events are used).
     */
    int mId =  -1;

    /**
     * Flag whether event should be triggered more than once in a row.
     * Turn off if it is not really required, as it uses memory and performance.
     */
    boolean mTriggerOnce = false;

    /**
     * Target A.
     */
    private int mTargetAFX;
    /**
     * Target B.
     */
    private int mTargetBFX;
    /**
     * Target C.
     */
    private int mTargetCFX;
    /**
     * Target D.
     */
    private int mTargetDFX;

    /**
     * Target Object.
     */
    Object mTargetObject;

    //Filter
    /**
     * Body filter.
     */
    Body mBodyFilter;
    /**
     * Shape filter.
     */
    Shape mShapeFilter;
    /**
     * Constraint filter.
     */
    Constraint mConstraintFilter;

    /**
     * Script to execute if the event is triggered.
     */
    private Script mScript;

    /**
     * Stores the parameter of previous event triggers.
     * Only required for trigger once events.
     */
    private Vector mTriggeredObjects = null;


    /**
     * User data
     */
    protected UserData mUserData = null;


    private static FXMatrix mTmpMatrix = new FXMatrix();
    private static FXVector mTmpVector1 = new FXVector();
    private static FXVector mTmpVector2 = new FXVector();

    /**
     * Creates a body event.
     * @param bodyFilter the body to which this event applies, null if all.
     * @param shapeFilter the shape (=group of bodies) to which this event applies, null if all.
     * @param type    the type of the event (can only be a BODY event type).
     * @param target1 the target 1
     * @param target2 the target 2
     * @param target3 the target 3
     * @param target4 the target 4
     */
    public static Event createBodyEvent(Body bodyFilter, Shape shapeFilter, int type, int target1, int target2, int target3, int target4)
    {
        return new Event(bodyFilter, shapeFilter, null, type, target1, target2, target3, target4, null);
    }

    /**
     * Creates a relative collision event.
     * The event will trigger if a collision happens between the two passed angles.
     * @param bodyFilter the body to which this event applies, null if all.
     * @param shapeFilter the shape (=group of bodies) to which this event applies, null if all.
     * @param startAngle start angle
     * @param endAngle  end angle
     */
    public static Event createCollisionRelativeEvent(Body bodyFilter, Shape shapeFilter, int startAngle, int endAngle)
    {
        long startAngle2FX = (long) startAngle * FXUtil.PI_2FX / 180;
        long endAngle2FX = (long) endAngle * FXUtil.PI_2FX / 180;
        int spread2FX = FXUtil.wrapAngleFX( (int) (endAngle2FX - startAngle2FX) / 2);
        int avgAngle2FX = FXUtil.wrapAngleFX((int) (startAngle2FX + spread2FX) );

        mTmpMatrix.setRotationMatrix(avgAngle2FX);
        mTmpMatrix.mult(FXVector.M_UNITY, mTmpVector1);

        mTmpMatrix.setRotationMatrix((int) startAngle2FX);
        mTmpMatrix.mult(FXVector.M_UNITY, mTmpVector2);
        int deviationFX = (int) mTmpVector1.dotFX(mTmpVector2);

        return new Event(bodyFilter, shapeFilter, null, TYPE_BODY_COLLISION_RELATIVE, avgAngle2FX, deviationFX, 0, 0, null);
    }

    /**
     * Creates an area collide event.
     * Triggers when a body collides with the sensor body.
     * @param bodyFilter the body to which this event applies, null if all.
     * @param shapeFilter the shape (=group of bodies) to which this event applies, null if all.
     * @param sensor The sensor body that is not physically present, but is used only for event checking.
     */
    public static Event createBodySensorEvent(Body bodyFilter, Shape shapeFilter, Body sensor)
    {
        sensor.calculateAABB(0);
        return new Event(bodyFilter, shapeFilter, null, TYPE_BODY_SENSOR, 0, 0, 0, 0, sensor);
    }

    /**
     * Creates a constraint event.
     * @param constraintFilter the constraint to which this event applies, null if all.
     * @param type    the type of the event (can only be a CONSTRAINT event type).
     * @param target1 the target 1
     * @param target2 the target 2
     */
    public static Event createConstraintEvent(Constraint constraintFilter, int type, int target1, int target2)
    {
        return new Event(null, null, constraintFilter, type, target1, target2, 0, 0, null);
    }

    /**
     * Constructor.
     * Direct constructor for the event
     * @param bodyFilter the body to which this event applies, null if all.
     * @param shapeFilter the shape (=group of bodies) to which this event applies, null if all.
     * @param constraintFilter the constraint to which this event applies, null if all.
     * @param type    the type of the event (must be consistent to the used filter).
     * @param target1FX the target 1
     * @param target2FX the target 2
     * @param target3FX the target 3
     * @param target4FX the target 4

     */
    protected Event(Body bodyFilter, Shape shapeFilter, Constraint constraintFilter, int type, int target1FX, int target2FX, int target3FX, int target4FX, Object target)
    {
        this.mId = -1;
        this.mType = type;
        this.mTargetObject = target;

        switch(type)
        {
        case TYPE_BODY_ROTATION:
        case TYPE_BODY_ANGULARVELOCITY:
            mTargetAFX = (target1FX * FXUtil.PI_2FX) / 180;
            mTargetBFX = (target2FX * FXUtil.PI_2FX) / 180;
            break;
        case TYPE_BODY_VELOCITY:
            mTargetAFX = (target1FX * target1FX) << FXUtil.DECIMAL;
            mTargetBFX = (target2FX * target2FX) << FXUtil.DECIMAL;
            break;
        case TYPE_BODY_COLLISION_RELATIVE:
            mTargetAFX = target1FX;
            mTargetBFX = target2FX;
            break;
        case TYPE_BODY_COLLISION:
        case Event.TYPE_BODY_POSITION:
        default:
            mTargetAFX = target1FX << FXUtil.DECIMAL;
            mTargetBFX = target2FX << FXUtil.DECIMAL;
            mTargetCFX = target3FX << FXUtil.DECIMAL;
            mTargetDFX = target4FX << FXUtil.DECIMAL;
            break;
        }


        this.mBodyFilter = bodyFilter;
        this.mShapeFilter = shapeFilter;
        this.mConstraintFilter = constraintFilter;
    }

    /**
     * Copy Constructor.
     * @param other the event to copy.
     * @param bodyMapping the mapping of bodies in the new world (null if not used).
     */
    protected Event( Event other, Body[] bodyMapping)
    {
        this((bodyMapping != null && other.mBodyFilter != null) ?  bodyMapping[other.mBodyFilter.mId] : other.mBodyFilter,
             other.mShapeFilter,
             other.mConstraintFilter,
             other.mType, 0, 0, 0, 0,
             (other.mTargetObject != null && other.mTargetObject instanceof Body &&
                     bodyMapping != null && ((Body) other.mTargetObject).mId >= 0 ) ? bodyMapping[((Body) other.mTargetObject).mId] : other.mTargetObject) ;

        mTargetAFX = other.mTargetAFX;
        mTargetBFX = other.mTargetBFX;
        mTargetCFX = other.mTargetCFX;
        mTargetDFX = other.mTargetDFX;

        if (other.mUserData != null)
        {
            mUserData = other.mUserData.copy();
        }

        setTriggerOnce(other.mTriggerOnce);
    }

    /**
     * Empty Constructor.
     * This is used for loading the event.
     * @param type type of the event.
     */
    protected Event( int type)
    {
        this.mType = type;
    }


    /**
     * Copies the event.
     * Creates a deep copy of the event.
     * The shape is not copied, but used as reference (see @link {@link Body#Body(Body)}).
     * @param bodyMapping the mapping of bodies in the new world (null if not used).
     * @return a deep copy of the event.
     */
    public Event copy(Body[] bodyMapping)
    {
        Event event = new Event(this, bodyMapping);
        return event;
    }

    /**
     * Loads an event from stream.
     * @param reader the file reader representing the data stream
     * @param world the world for correct referencing of bodies/constraints used by the event
     * @return the loaded event
     */
    //#WorldLoadingOFF /*
    public static Event loadEvent(PhysicsFileReader reader, World world, UserData userData)
    {
        //read the event type
        int type = reader.next();
        int version = reader.getVersion();

        if ( (version & World.MASK_VERSION) < World.VERSION_4)
        {
            byte id = (byte) reader.next(); //read the byte for compatibility reasons
        }

        Event event = new Event(type);

        //read trigger once
        //since version 4
        if ( (version & World.MASK_VERSION) >= World.VERSION_4)
        {
            event.setTriggerOnce(reader.next() > 0);
        }

        int bodyIndex = reader.next();
        if (bodyIndex >= 0 && bodyIndex != 255)
        {
            event.mBodyFilter = world.getBodies()[bodyIndex];
        }

        int shapeIndex = reader.next();
        if (shapeIndex >= 0 && shapeIndex != 255)
        {
            event.mShapeFilter = (Shape) world.getShapeSet().getShapes().elementAt(shapeIndex);
        }

        if ( (version & World.MASK_VERSION) >= World.VERSION_4)
        {
            int constraintIndex = reader.next();
            if (constraintIndex >= 0 && constraintIndex != 255)
            {
                event.mConstraintFilter = world.getConstraints()[constraintIndex];
            }
        }

        event.mTargetAFX = reader.nextIntFX();
        event.mTargetBFX = reader.nextIntFX();
        event.mTargetCFX = reader.nextIntFX();
        event.mTargetDFX = reader.nextIntFX();

        //load the additional body definition
        if (event.mType == TYPE_BODY_SENSOR)
        {
            Body sensor = Body.loadBody(reader, world.mShapeSet.getShapes(), userData);
            event.mTargetObject = sensor;
        }

        if (reader.getVersion() > World.VERSION_7)
        {
            String userDataString = reader.nextString();
            if (userData != null)
            {
                event.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_EVENT);
            }
        }

        return event;
    }
    //#WorldLoadingOFF */

    /**
     * Gets the event identifier.
     * This is -1 as long as the event is not registered on the world.
     * @return the event identifier.
     */
    public int getIdentifier()
    {
        return mId;
    }

    /**
     * Gets the event type.
     * @return the event type.
     */
    public int type()
    {
        return mType;
    }

    /**
     * Sets the execution script.
     * The script is executed whenever the event triggers.
     * This applies only to body events.
     * Note that the script has to be registered on the world to be executed.
     * @param script the script to execute.
     */
    public void setScript(Script script)
    {
        this.mScript = script;
    }

    /**
     * Sets the body filter.
     * @param b the body for which the event applies to.
     */
    public void setBodyFilter(Body b)
    {
        mBodyFilter = b;
    }

    /**
     * Gets the body filter.
     * @return the body filter.
     */
    public Body getBodyFilter()
    {
        return mBodyFilter;
    }

    /**
     * Sets the shape filter.
     * @param s the shape (= group of bodies) for which the event applies to.
     */
    public void setShapeFilter(Shape s)
    {
        mShapeFilter = s;
    }

    /**
     * Gets the shape filter.
     * @return the shape filter.
     */
    public Shape getShapeFilter()
    {
        return mShapeFilter;
    }


    /**
     * Return the target object.
     * The target object is a relevnat object depending on teh event type.
     * For sensor collision events, this is the sensor body.
     * @return the target object.
     */
    public Object getTargetObject()
    {
        return mTargetObject;
    }

    /**
     * Sets the constraint filter.
     * @param c the constraint filter for event triggering.
     */
    protected void setConstraintFilter(Constraint c)
    {
        mConstraintFilter = c;
    }

    /**
     * Gets the constraint filter.
     * @return the constraint filter.
     */
    public Constraint getConstraintFilter()
    {
        return mConstraintFilter;
    }

    /**
     * Gets the targetA
     * @return the target A
     * @fx
     */
    protected int targetAFX()
    {
        return mTargetAFX;
    }

    /**
     * Gets the targetB
     * @return the target B
     * @fx
     */
    protected int targetBFX()
    {
        return mTargetBFX;
    }

    /**
     * Gets the targetC
     * @return the target C
     * @fx
     */
    protected int targetCFX()
    {
        return mTargetCFX;
    }

    /**
     * Gets the targetD
     * @return the target D
     * @fx
     */
    protected int targetDFX()
    {
        return mTargetDFX;
    }


    /**
     * Sets all targets.
     * The meaning of targets depend on the event type.
     * @param target1FX the target 1
     * @param target2FX the target 2
     * @param target3FX the target 3
     * @param target4FX the target 4
     * @fx
     */
    protected void setTargetsFX(int target1FX, int target2FX, int target3FX, int target4FX)
    {
        mTargetAFX = target1FX;
        mTargetBFX = target2FX;
        mTargetCFX = target3FX;
        mTargetDFX = target4FX;
    }

    /**
     * Flag whether event should be triggered more than once in a row.
     * Turn off is not really required, uses memory and performance
     * @param triggerOnce new value of trigger once
     */
    public void setTriggerOnce(boolean triggerOnce)
    {
        this.mTriggerOnce = triggerOnce;
        if (triggerOnce)
        {
            mTriggeredObjects = new Vector();
        }
        else
        {
            mTriggeredObjects = null;
        }
    }

    /**
     * Gets the trigger once flag.
     * @return the trigger once flag.
     */
    public boolean getTriggerOnce()
    {
        return mTriggerOnce;
    }

    /**
     * Checks if the event is currently active.
     * Triggers the event on the event listener for each object (constraint or body)
     * that fulfills the condition of the event.
     * @param world the world
     * @param listener the event listener
     * @return true if the event triggered at least once.
     */
    public boolean checkEvent(World world, PhysicsEventListener listener)
    {
        if (listener == null) return false;

        boolean triggered = false;
        switch(mType)
        {
        case TYPE_CONSTRAINT_FORCE:
            if (mConstraintFilter != null)
            {
                triggered = checkConstraint(world, mConstraintFilter, listener);
            }
            else
            {
                for( int i = 0; i < world.mConstraintCount; i++)
                {
                    triggered |= checkConstraint( world, world.mConstraints[i], listener);
                }
            }
            break;
        case TYPE_BODY_ANGULARVELOCITY:
        case TYPE_BODY_COLLISION:
        case TYPE_BODY_POSITION:
        case TYPE_BODY_ROTATION:
        case TYPE_BODY_VELOCITY:
        case TYPE_BODY_COLLISION_RELATIVE:
            if (mBodyFilter != null)
            {
                triggered = checkBody(world, mBodyFilter, listener);
            }
            else
            {
                for( int i = 0; i < world.mBodyCount; i++)
                {
                    triggered |= checkBody( world, world.mBodies[i], listener);
                }
            }
            break;
        case TYPE_BODY_SENSOR:
            if (mBodyFilter != null)
            {
                triggered = checkBody(world, mBodyFilter, listener);
            }
            else
            {
                Body[] bodies = world.mBodies;
                Body checkBody = (Body) mTargetObject;
                for( int i = 0; i < world.mBodyCount; i++)
                {
                    if (bodies[i].mDynamic &&
                        ! (bodies[i].mAABBMinYFX > checkBody.mAABBMaxYFX || checkBody.mAABBMinYFX > bodies[i].mAABBMaxYFX) &&
                        ! (bodies[i].mAABBMinXFX > checkBody.mAABBMaxXFX || checkBody.mAABBMinXFX > bodies[i].mAABBMaxXFX))
                    {
                        triggered |= checkBody( world, bodies[i],listener);
                    }
                }
            }
            break;
        default:
            break;
        }

        return triggered;
    }

    /**
     * Checks a single body for event triggering.
     * Applies the script (if any) to the body if the event is triggered.
     * @param world the world
     * @param b the body
     * @param listener the event listener
     * @return true if the event triggered on that body.
     */
    private final boolean checkBody(World world, Body b, PhysicsEventListener listener)
    {
        if (mShapeFilter != null && b.mShape != mShapeFilter)
        {
            return false;
        }
        Object triggerObject = null;       //required for contact events

        boolean triggered = false;
        switch(mType)
        {
        case Event.TYPE_BODY_POSITION:
            if (    mTargetAFX <= b.mPositionFX.xFX && b.mPositionFX.xFX <= mTargetCFX
                 && mTargetBFX <= b.mPositionFX.yFX && b.mPositionFX.yFX <= mTargetDFX)
            {
                triggered = true;
                triggerObject = b;
            }
            break;
        case TYPE_BODY_VELOCITY:
            int speedFX = b.mVelocityFX.lengthSquareFX();
            if ( mTargetAFX <= speedFX && speedFX <= mTargetBFX )
            {
                triggered = true;
                triggerObject = b;
            }
            break;
        case TYPE_BODY_ROTATION:
            if ( mTargetAFX <= b.mRotation2FX && b.mRotation2FX <= mTargetBFX )
            {
                triggered = true;
                triggerObject = b;
            }
            break;
        case TYPE_BODY_ANGULARVELOCITY:
            if ( mTargetAFX <= b.mAngularVelocity2FX && b.mAngularVelocity2FX <= mTargetBFX )
            {
                triggered = true;
                triggerObject = b;
           }
            break;
        case TYPE_BODY_COLLISION:
            {
                Contact[] contacts = b.getContacts();
                for( int i = 0; i < contacts.length; i++)
                {
                    if (contacts[i] == null)
                    {
                        break;
                    }
                    if (  (mTargetAFX > 0 && contacts[i].mContactPosition1.yFX < b.mPositionFX.yFX)
                       || (mTargetBFX > 0 && contacts[i].mContactPosition1.xFX > b.mPositionFX.xFX)
                       || (mTargetCFX > 0 && contacts[i].mContactPosition1.yFX > b.mPositionFX.yFX)
                       || (mTargetDFX > 0 && contacts[i].mContactPosition1.xFX < b.mPositionFX.xFX) )
                    {
                        triggered = true;
                        //set the trigger
                        triggerObject = contacts[i];
                        break;
                    }
                }
            }
            break;
        case TYPE_BODY_COLLISION_RELATIVE:
            {
                int angle2FX = FXUtil.wrapAngleFX(mTargetAFX + b.rotation2FX());
                mTmpMatrix.setRotationMatrix(angle2FX);
                mTmpMatrix.mult(FXVector.M_UNITY, mTmpVector1);

                Contact[] contacts = b.getContacts();
                for( int i = 0; i < contacts.length; i++)
                {
                    if (contacts[i] == null)
                    {
                        break;
                    }
                    mTmpVector2.assignDiff(b.mPositionFX, contacts[i].mContactPosition1);
                    mTmpVector2.normalize();
                    if (  mTmpVector1.dotFX(mTmpVector2) >= mTargetBFX )    //check if we are inside the allowed deviation
                    {
                        triggered = true;
                        triggerObject = contacts[i];
                        break;
                    }
                    else if (! contacts[i].isSingle())
                    {
                        mTmpVector2.assignDiff(b.mPositionFX, contacts[i].mContactPosition2);
                        mTmpVector2.normalize();
                        if (  mTmpVector1.dotFX(mTmpVector2) >= mTargetBFX )    //check if we are inside the allowed deviation
                        {
                            triggered = true;
                            triggerObject = contacts[i];
                            break;
                        }
                    }

                }
            }
            break;
        case TYPE_BODY_SENSOR:
            //check collision
            if ( Collision.detectCollision(b, (Body) mTargetObject) != null)
            {
                triggered = true;
                triggerObject = b;
            }
            break;
        default:
            break;
        }

        if (triggered)
        {
            if (! mTriggerOnce || checkTrigger(triggerObject))
            {
                if (mScript == null)
                {
                    listener.eventTriggered(this, triggerObject);
                }
                else
                {
                    mScript.applyToBody(b, world);
                }
            }
        }
        else
        {
            if (mTriggerOnce) removeTrigger(triggerObject);
        }

        return triggered;
    }

    /**
     * Checks a single constraint for event triggering.
     * @param world the world
     * @param c the constraint
     * @param listener the event listener
     * @return true if the event triggered on that constraint.
     */
    private final boolean checkConstraint(World world, Constraint c, PhysicsEventListener listener)
    {
        switch(mType)
        {
        case Event.TYPE_CONSTRAINT_FORCE:
            int impulseFX = c.getImpulseFX();
            if ( mTargetAFX <= impulseFX && impulseFX <= mTargetBFX )
            {
                if (! mTriggerOnce || checkTrigger(c))
                {
                    listener.eventTriggered(this, c);
                }
                return true;
            }
            if (mTriggerOnce) removeTrigger(c);
            break;
        }
        return false;
    }

    /**
     * Check if the trigger object exists and updates the triggeronce vector.
     * Assumes that triggeronce was checked before.
     */
    private boolean checkTrigger(Object o)
    {
        if (mTriggeredObjects.contains(o))
        {
            return false;
        }
        else
        {
            mTriggeredObjects.addElement(o);
            return true;
        }
    }

    /**
     * Removes the trigger from the objects list.
     * Is used for retriggering.
     * Assumes that triggeronce was checked before.
     */
    private void removeTrigger(Object o)
    {
        mTriggeredObjects.removeElement(o);
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
