package at.emini.physics2D;

import java.util.Vector;

/**
 * The shapeset manages the shapes within a world.
 * All shapes of a world are referenced here.
 * Whenever the {@link World#addBody(Body)} adds a body,
 * it registers the shape in its ShapeSet.
 * If the shape is already registered nothing happens. <br>
 *
 * @author Alexander Adensamer
 */
public class ShapeSet
{

    /**
     * A list of all registered shapes.
     */
    protected Vector mShapes = new Vector();

    /**
     * Empty Constructor.
     */
    public ShapeSet()
    {
    }

    /**
     * Copy constructor.
     * @param other the shape set to copy.
     */
    public ShapeSet(ShapeSet other)
    {
        for( int i = 0; i < other.mShapes.size(); i++)
        {
            mShapes.addElement(other.mShapes.elementAt(i));
        }
    }

    /**
     * Copies the shape set.
     * @return the copy of the shape set
     */
    public ShapeSet copy()
    {
        return new ShapeSet(this);
    }

    /**
     * Registers a shape in the set.
     * If the shape is already registered (here or somewhere else), it is ignored.
     * @param s the shape to register.
     */
    public void registerShape(Shape s)
    {
        //check if the shape is existing
        if (s.mId >= 0)
        {
            //this should not be required -> unless multiple worlds are involved,
            //but in this case shapes should be copied anyway.
            /*
            for( int i = 0; i < shapes.size(); i++)
            {
                if ( shapes.elementAt(i) == s);
                {
                    return;
                }
            }*/
            return;
        }

        s.mId = mShapes.size();
        mShapes.addElement(s);
    }

    /**
     * Registers a vector of shapes.
     * All shapes in the vector that are not registered anywhere else are registered here.
     * @param newShapes vector of shapes to register.
     */
    public void registerShapes(Vector newShapes)
    {
        for( int i = 0; i < newShapes.size(); i++)
        {
            if ( newShapes.elementAt(i) instanceof Shape);
            {
                Shape s = (Shape)newShapes.elementAt(i);
                if (s.mId < 0)
                {
                    s.mId = mShapes.size();
                    mShapes.addElement(s);
                }
            }
        }
    }

    /**
     * Checks if a shape is registered.
     * @param s the shape to check for.
     * @return true if the shape is registered here.
     */
    public boolean isRegistered(Shape s)
    {
        return mShapes.contains(s);
    }

    /**
     * Corrects the shape id for the shape at a given index
     * This can be required if the vector was meddled with (like deleting, inserting, etc)
     * @param index the index
     */
    protected void correctShapeId(int index)
    {
        ((Shape) mShapes.elementAt(index)).mId = index;
    }

    /**
     * Gets the vector of all shapes.
     * @return the vector of all registered shapes.
     */
    public Vector getShapes()
    {
        return mShapes;
    }

}
