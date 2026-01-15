package at.emini.physics2DVisualTest;

import at.emini.physics2D.Body;
import at.emini.physics2D.ExternalForce;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXVector;

public class BalloonForce implements ExternalForce
{

    private Body balloon;
    private FXVector force;

    public BalloonForce(Body balloon, FXVector force)
    {
        this.balloon = balloon;
        this.force = force;
    }

    @Override
    public void applyForce(Body[] bodies, int bodyCount, int timestepFX)
    {
        balloon.applyForce(force, timestepFX);
    }

    @Override
    public ExternalForce copy(Body[] bodyMapping)
    {
        return new BalloonForce(bodyMapping[balloon.getId()], force);
    }

}
