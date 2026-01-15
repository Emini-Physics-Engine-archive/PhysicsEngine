package at.emini.physics2D;

/**
 * Interface for an event listener. <br>
 * Listener that registers on the world and receives all triggered events there.
 *
 * @author Alexander Adensamer
 * @see Event
 */
public interface PhysicsEventListener
{
    /**
     * Callback method for event triggers.
     * @param e the triggered event.
     * @param parameter the object (typically body, constraint or contact) that triggered the event.
     */
    public void eventTriggered(Event e, Object parameter);

}
