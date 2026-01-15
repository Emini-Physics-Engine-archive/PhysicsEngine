package at.emini.physics2D;

import java.util.Vector;

/**
 * The event set manages the events within a world.
 * All events of a world are referenced here.
 * It ensures that each event can be accessed directly or by index(id)
 *
 * @author Alexander Adensamer
 */
public class EventSet
{
    /**
     * A list of all registered events
     */
    protected Vector mEvents = new Vector();

    /**
     * Default Constructor.
     */
    public EventSet()
    {
    }

    /**
     * Copy constructor.
     * @param other the event set to copy
     */
    public EventSet(EventSet other)
    {
        for( int i = 0; i < other.mEvents.size(); i++)
        {
            mEvents.addElement(other.mEvents.elementAt(i));
        }
    }

    /**
     * Copies the event set.
     * Creates a copy of the event set.
     * @return the copy of the event set.
     */
    public EventSet copy()
    {
        return new EventSet(this);
    }

    /**
     * Registers a new event.
     * @param e the event to register
     */
    public void registerEvent(Event e)
    {
        //check if the event is existing
        if (e.mId >= 0)
        {
            return;
        }

        e.mId = mEvents.size();
        mEvents.addElement(e);
    }

    /**
     * Registers new events from a vector.
     * Only events that are not already registered (anywhere), are registered.
     * @param newEvents vector of new events
     */
    public void registerEvents(Vector newEvents)
    {
        for( int i = 0; i < newEvents.size(); i++)
        {
            if ( newEvents.elementAt(i) instanceof Event);
            {
                Event e = (Event)newEvents.elementAt(i);
                if (e.mId < 0)
                {
                    e.mId = mEvents.size();
                    mEvents.addElement(e);
                }
            }
        }
    }

    /**
     * Removes an event form the event set.
     * @param e the event to remove
     */
    public void removeEvent(Event e)
    {
        //check if the event is existing
        if (e.mId < 0)
        {
            return;
        }

        int id = e.mId;
        mEvents.removeElement(e);
        for( int i = id; i < mEvents.size(); i++)
        {
            ((Event) mEvents.elementAt(i)).mId = i;
        }
        e.mId = -1;
    }

    /**
     * Checks all events for triggering.
     * @param world the world.
     * @param listener the listener to inform about the triggers.
     */
    public void checkEvents(World world, PhysicsEventListener listener)
    {
        int eventSize = mEvents.size();
        for(int i = eventSize - 1; i >= 0 ; i--)    //walk through array in reverse to allow event handling code to remove itself
        {
            Event e = (Event) mEvents.elementAt(i);
            e.checkEvent(world, listener);
        }
    }

    /**
     * Checks whether an event is registered in this set.
     * @param e the event to check.
     * @return true if the event is registered here.
     */
    public boolean isRegistered(Event e)
    {
        return mEvents.contains(e);
    }

    /**
     * Gets the complete list of events.
     * @return a list of all events.
     */
    public Vector getEvents()
    {
        return mEvents;
    }

}
