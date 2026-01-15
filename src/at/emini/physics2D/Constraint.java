package at.emini.physics2D;


/**
 * Interface for additional constraints.
 * The constraint has to be added to the world after creation.
 * It is considered in the simulation by calling the following methods
 * in the @link {@link World#tick()}.
 * <ul>
 * <li> {@link Constraint#precalculate(long)} is called once initially</li>
 * <li> {@link Constraint#applyMomentum(long)} is called at each solving iteration</li>
 * <li> {@link Constraint#postStep()} is called once after the solving iteration</li>
 * </ul>
 *
 * The constraint can apply to any number of bodies.
 *
 * @author Alexander Adensamer
 */
public interface Constraint
{

    /**
     * Type indicator for joints. (used for loading/saving world)
     */
    public static final int JOINT = 0;
    /**
     * Type indicator for springs. (used for loading/saving world)
     */
    public static final int SPRING = 1;
    /**
     * Type indicator for scripts. (used for loading/saving world)
     */
    public static final int SCRIPT = 2;
    /**
     * Type indicator for motors. (used for loading/saving world)
     */
    public static final int MOTOR = 3;

    /**
     * Precomputes static values.
     * The method is called before the constraint iteration to pre-compute static values.
     * @param invTimestepFX the inverse timestep of the simulation
     */
    public void precalculate(long invTimestepFX);

    /**
     * Applies the momentum to the body/bodies.
     * This method is called for each constraint iteratively.
     * It applies the constraint to the involved body (bodies).
     * @param invTimestepFX the inverse timestep of the simulation
     */
    public boolean applyMomentum(long invTimestepFX);

    /**
     * Cleans up after constraint iteration.
     * Method is called after the iteration to clean up and perform post-iteration calculations.
     */
    public void postStep();


    /**
     * Gets the virtual work of the constraint.
     * @fx
     * @return the last acting impulse on the constraint
     */
    public int getImpulseFX();

    /**
     * Copies the constraint.
     * This includes update of body reference(s) of the new world.
     * @param bodyMapping A mapping of the body indices from the old world to the new
     * @return a deep copy of the constraint
     */
    public Constraint copy(Body[] bodyMapping);

    /**
     * Checks whether the constraint applies to a body.
     * @param b the body to check
     * @return true if the constraint applies to the body.
     */
    public boolean concernsBody(Body b);

    /**
     * Checks for equality of two constraints.
     * @param other the constraint to compare
     * @return true if the constraints are equal.
     */
    public boolean equals(Constraint other);

    public UserData getUserData();
}
