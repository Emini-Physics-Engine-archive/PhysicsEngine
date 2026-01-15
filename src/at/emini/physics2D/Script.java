package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.PhysicsFileReader;    //#NoBasic

/**
 * The script class represents a sequence of actions for a body. <br>
 * It represents a sequence of "hard constraints"
 * that can be repeated indefinitely. <br>
 * Each script consists of simple script elements.
 * Each element can influence the position, velocity or acceleration
 * of the body.
 *
 * @author Alexander Adensamer
 */
public class Script
{

    //Types
    /**
     * Element Type none
     */
    public static final int NONE                    = 0;
    /**
     * Element Type position
     */
    public static final int POSITION                = 1;
    /**
     * Element Type velocity
     */
    public static final int VELOCITY                = 2;
    /**
     * Element Type acceleration
     */
    public static final int ACCELERATION            = 3;
    /**
     * Element Type angle
     */
    public static final int ANGLE                   = 4;
    /**
     * Element Type velocity
     */
    public static final int ROTATIONAL_VELOCITY     = 5;
    /**
     * Element Type acceleration
     */
    public static final int ROTATIONAL_ACCELERATION = 6;


    /**
     * Vector containing the scripting elements .
     */
    protected Vector mElements;

    /**
     * Flag if the script is restarted after finishing.
     */
    protected boolean mRestart = false;

    /**
     * Single action element for a script.
     * Each element has a type and target values, depending on the type.
     * @author Alexander Adensamer
     */
    public class ScriptElement
    {
        /**
         * The type of the body modification (e.g: position, position, ...)
         */
        public int mType;
        /**
         * first target value
         */
        public int mTargetAFX;
        /**
         * second target value
         */
        public int mTargetBFX;
        /**
         * number of timesteps to execute this step
         */
        public int mTimeSteps;

        /**
         * Constructor.
         * The type and values are supplied directly.
         * @fx
         * @param type the type.
         * @param target1FX the first target value.
         * @param target2FX the second target value.
         * @param timeSteps number of timesteps.
         */
        protected ScriptElement(int type, int target1FX, int target2FX, int timeSteps)
        {
            this.mType = type;
            switch (type)
            {
            case POSITION:
            case VELOCITY:
            case ACCELERATION:
                this.mTargetAFX = target1FX;
                this.mTargetBFX = target2FX;
                break;
            case ANGLE:
            case ROTATIONAL_VELOCITY:
            case ROTATIONAL_ACCELERATION:
                this.mTargetAFX = target1FX;
                break;
            default: break;
            }

            this.mTimeSteps = timeSteps;
        }

        /**
         * Copies a script element.
         * @param element the source element.
         */
        ScriptElement(ScriptElement element)
        {
            mType = element.mType;
            mTargetAFX = element.mTargetAFX;
            mTargetBFX = element.mTargetBFX;
            mTimeSteps = element.mTimeSteps;
        }
    }

    /**
     * Constructor.
     * Creates an empty script.
     * @param restart the flag, if the script is restarted after finishing.
     */
    public Script(boolean restart)
    {
        mElements = new Vector();
        this.mRestart = restart;
    }

    /**
     * Copy constructor.
     * @param script the source script object
     */
    public Script(Script script)
    {
        mElements = new Vector();
        mRestart = script.mRestart;

        for( int i = 0; i < script.mElements.size();i++)
        {
            addElement( ((ScriptElement) script.mElements.elementAt(i)) );
        }
    }

    /**
     * Copy method for the script object
     * @return the copy
     */
    public Script copy()
    {
        Script script = new Script(this);

        return script;
    }

    /**
     * Applies the script to a body.
     * @param b the body
     * @param w the world managing the scripts
     */
    public void applyToBody(Body b, World w)
    {
        //check if script already applies to body...

        w.mScriptBodies[w.mScriptBodyCount] = b;
        for( int i = 0; i < w.mScriptCount; i++ )
        {
            if (w.mScripts[i] == this)
            {
                w.mScriptIndex[w.mScriptBodyCount] = i;
                break;
            }
        }

        w.mScriptElementIndex[w.mScriptBodyCount] = 0;
        w.mScriptExecutionIndex[w.mScriptBodyCount] = 0;

        w.mScriptBodyCount++;
    }

    /**
     * Adds a new element to the script.
     * @param type the type of the element (position, velocity, etc.).
     * @param target1 the first target value.
     * @param target2 the second target value (only relevant for the vector types).
     * @param timeSteps the number of timesteps.
     */
    public void addElement(int type, int target1, int target2, int timeSteps)
    {
        switch(type)
        {
        case POSITION:
        case VELOCITY:
        case ACCELERATION:
            mElements.addElement(new ScriptElement(type, target1 << FXUtil.DECIMAL, target2 << FXUtil.DECIMAL , timeSteps));
            break;
        case ANGLE:
        case ROTATIONAL_VELOCITY:
        case ROTATIONAL_ACCELERATION:
            mElements.addElement(new ScriptElement(type, (target1 * 2 * FXUtil.PI_2FX) / 360 , 0, timeSteps));
            break;
        default:
            break;
        }
    }

    /**
     * Adds a new element to the script (only rotational elements).
     * @param type the type of the element (position, velocity, etc.).
     * @param target1 the first target value.
     * @param timeSteps the number of timesteps.
     */
    public void addElement(int type, int target1, int timeSteps)
    {
        addElement(type, target1, 0, timeSteps);
    }

    /**
     * Adds a script element directly.
     * @param element the script element.
     */
    public void addElement(ScriptElement element)
    {
        mElements.addElement(new ScriptElement(element) );
    }

    /**
     * Executes the script.
     * @param index the execution index (referring to the corresponding body, stored in the world)
     * @param w the world
     */
    protected void executeScript(int index, World w)
    {
        if (w.mScriptElementIndex[index] < mElements.size())
        {
            ScriptElement currElement = (ScriptElement) mElements.elementAt(w.mScriptElementIndex[index]);

            if (currElement.mTimeSteps > 0)
            {
                w.mScriptExecutionIndex[index]++;
                if (w.mScriptExecutionIndex[index] > currElement.mTimeSteps)
                {
                    w.mScriptElementIndex[index]++;
                    w.mScriptExecutionIndex[index] = 0;
                }
            }
        }
        else if (mRestart)
        {
            w.mScriptElementIndex[index] = 0;
        }
        else
        {
            //remove body
            w.mScriptBodies[index] = null;
            return;
        }

        //perform script
        if (w.mScriptElementIndex[index] < mElements.size())
        {
            ScriptElement currElement = (ScriptElement)  mElements.elementAt(w.mScriptElementIndex[index]);
            Body body = w.mScriptBodies[index];

            switch (currElement.mType)
            {
            case POSITION:
                body.mPositionFX.xFX = currElement.mTargetAFX;
                body.mPositionFX.yFX = currElement.mTargetBFX;
                break;
            case VELOCITY:
                body.mVelocityFX.assignFX(currElement.mTargetAFX, currElement.mTargetBFX);
                break;
            case ACCELERATION:
                body.mVelocityFX.addFX(currElement.mTargetAFX, currElement.mTargetBFX, w.getTimestepFX());
                break;
            case ANGLE:
                body.setRotation2FX(currElement.mTargetAFX);
                break;
            case ROTATIONAL_VELOCITY:
                body.mAngularVelocity2FX = currElement.mTargetAFX;
                break;
            case ROTATIONAL_ACCELERATION:
                body.mAngularVelocity2FX += (int) (((long)currElement.mTargetAFX * (long) w.getTimestepFX() ) >> FXUtil.DECIMAL);
                break;
            default: break;
            }
        }
    }


    /**
     * Loads a script from a stream.
     * @param reader the file reader.
     * @return the loaded script
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static Script loadScript(PhysicsFileReader reader)
    {
        boolean restart = reader.next() > 0;
        Script script = new Script(restart);

        int elementCnt = reader.next();

        for( int i = 0; i < elementCnt; i++)
        {
            ScriptElement element = script.new ScriptElement(reader.next(),
                    reader.nextIntFX(),
                    reader.nextIntFX(),
                    reader.next());
            script.mElements.addElement( element ) ;
        }

        return script;
    }
    //#WorldLoadingOFF */
    //#NoBasic */
}


