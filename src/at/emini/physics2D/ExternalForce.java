package at.emini.physics2D;

/**
 * Represents an external force applying to one or more bodies.
 * The external force is applied at each step in the simulation.
 *
 * @author Alexander Adensamer
 *
 */
public interface ExternalForce
{

    /**
     * Method to apply the external force.
     * The force is applied to all bodies that are affected by it.
     * Force can be applied to a body using the {@link Body#applyForce(at.emini.physics2D.util.FXVector, int)} method.
     * @param bodies the caller supplies an array containing all bodies in the world.
     * @param bodyCount the number of bodies in the world (not necessarily equal to length of the array)
     * @param timestepFX the current simulation timestep is passed
     */
    public void applyForce(Body[] bodies, int bodyCount, int timestepFX);

    /**
     * Copy method for the force.
     * This is required when the world is copied or added to another world.
     * @param bodyMapping a vector containing a mapping from bodies in the original world to the new world.
     * This will only be used when the class depends on bodies. The reference to the copy of a body is found by bodyMapping[body.getId()].
     * @return a deep copy of the force.
     */
    public ExternalForce copy(Body[] bodyMapping);
}
