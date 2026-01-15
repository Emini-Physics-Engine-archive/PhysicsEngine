package at.emini.physics2D;

import java.util.Vector;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;       //#NoBasic

/**
 * The multishape contains several shapes.
 *
 * It is an extension of the standard shape that is treated accordingly by the rest of the code.
 *
 * @author Alexander Adensamer
 */
public class MultiShape extends Shape
{
    /**
     * List of shapes the shape consists of
     */
    protected Shape[] mShapes;

    /**
     * Start indices for the vertices for each shape in the common vertex vector.
     */
    protected int mVertexStartIndices[];

    /**
     * Start indices for the unique axes for each shape in the common axes vector.
     */
    protected int mAxesStartIndices[];

    /**
     * Constructor.
     * Creates a complex multi shape from several parts.
     * Each part is a shape.
     * The parts do not have to be centered.
     * @param shapes a vector of shapes that the multi shape consists of
     */
    public MultiShape(Vector shapes)
    {
        initShapeMembers(shapes);

    }

    /**
     * Sets up the shape vectors.
     * It uses the mVertices[] array to store all shapes sequentially.
     * Its own member indices are used to distinguish the individual shapes.
     * @param shapes
     */
    protected void initShapeMembers(Vector shapes)
    {
        this.mShapes = new Shape[shapes.size()];
        mVertexStartIndices = new int[shapes.size() + 1];
        mAxesStartIndices = new int[shapes.size() + 1];

        int totalVertexCount = 0;
        int totalMassFX = 0;
        for( int i = 0; i < shapes.size(); i++)
        {
            this.mShapes[i] = ((Shape) shapes.elementAt(i));
            mVertexStartIndices[i] = totalVertexCount;
            totalVertexCount += this.mShapes[i].mVertices.length;
            totalMassFX += this.mShapes[i].mMassFX;
        }
        mVertexStartIndices[shapes.size()] = totalVertexCount;

        this.mVertices = new FXVector[totalVertexCount];
        int cornerIdx = 0;
        for( int i = 0; i < shapes.size(); i++)
        {
            for( int j = 0; j < this.mShapes[i].mVertices.length; j++)
            {
                mVertices[cornerIdx++] = this.mShapes[i].mVertices[j];
            }
        }

        updateInternals();
        setMassFX(totalMassFX);
    }

    /**
     * Copy Constructor.
     */
    public MultiShape(MultiShape other)
    {
        mVertices = new FXVector[other.mVertices.length];
        System.arraycopy(other.mVertices, 0, mVertices, 0, mVertices.length);

        mShapes = new Shape[other.mShapes.length];
        System.arraycopy(other.mShapes, 0, mShapes, 0, mShapes.length);

        mVertexStartIndices = new int[other.mVertexStartIndices.length];
        System.arraycopy(other.mVertexStartIndices, 0, mVertexStartIndices, 0, mVertexStartIndices.length);

        mAxesStartIndices = new int[other.mAxesStartIndices.length];
        System.arraycopy(other.mAxesStartIndices, 0, mAxesStartIndices, 0, mAxesStartIndices.length);

        setElasticityFX(other.mElasticityFX);
        setFrictionFX(other.mFrictionFX);

        updateInternals();
        setMassFX(other.mMassFX);
        //correctCentroid();

        if (other.mUserData != null)
        {
            mUserData = other.mUserData.copy();
        }
    }

    /**
     * Returns the subshape at a given index.
     * @param index the index of the subshape.
     * @return the sub shape
     */
    public Shape getShape(int index)
    {
        return mShapes[index];
    }

    /**
     * Returns the number of subshapes.
     * @return the shape count
     */
    public int getShapeCount()
    {
        return mShapes.length;
    }

    /**
     * Overloaded method to treat merged vertex arrays correctly.
     */
    void determineUniqueAxes()
    {
        int axesCount = 0;

        for( int i = 0; i < mShapes.length; i++)
        {
            axesCount += mShapes[i].mUniqueAxesIndicesCount;
        }

        mUniqueAxesIndicesCount = axesCount;
        mUniqueAxesIndices = new int[axesCount];
        int cnt = 0;
        for( int i = 0; i < mShapes.length; i++)
        {
            mAxesStartIndices[i] = cnt / 2;
            for( int j = 0; j < mShapes[i].mUniqueAxesIndicesCount; j++)
            {
                mUniqueAxesIndices[cnt++] = mVertexStartIndices[i] + mShapes[i].mUniqueAxesIndices[j];
            }
        }
        mAxesStartIndices[mShapes.length] = cnt / 2;
    }
    /**
     * Calculates the area of the shape.
     */
    void computeAreaFX()
    {
        mAreaFX = 0;
        mInertiaFX = 0;
        //currently simple area and inertia calculation
        for( int i = 0; i< mShapes.length; i++)
        {
            mAreaFX += mShapes[i].mAreaFX;
            mInertiaFX += mShapes[i].mInertiaFX; //+ mass * offset of centroid
        }

        if (mInertiaFX == 0)
        {
            mInvInertia2FX = (1 << 31) - 1;
        }
        else
        {
            mInvInertia2FX = (((long)FXUtil.ONE_FX << (FXUtil.DECIMAL2 ))/ mInertiaFX);
        }
        //depending on area
        //calcInertia();
    }

    /**
     * Loads a multi shape from a stream.
     * @param reader a physics file reader.
     * @return the loaded shape.
     */
    //#NoBasic /*
    //#WorldLoadingOFF /*
    public static MultiShape loadShape( PhysicsFileReader reader, UserData userData, Vector stdShapes)
    {
        int version = reader.getVersion();

        int shapesCnt = reader.next();

        Vector shapes = new Vector();
        for( int i = 0; i < shapesCnt; i++)
        {
            shapes.addElement(stdShapes.elementAt(reader.next()));
        }

        MultiShape shape = new MultiShape(shapes);

        String userDataString = reader.nextString();
        if (userData != null)
        {
            shape.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_SHAPE);
        }

        return shape;
    }
    //#WorldLoadingOFF */
    //#NoBasic */
}
