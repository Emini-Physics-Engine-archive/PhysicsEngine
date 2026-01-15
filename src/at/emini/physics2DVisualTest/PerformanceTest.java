package at.emini.physics2DVisualTest;

import at.emini.physics2D.Body;
import at.emini.physics2D.Shape;
import at.emini.physics2D.World;

public class PerformanceTest
{
    private static final int bodies = 120;
    private static final int simTime = 1000;

    private int contactCount = 0;

    private World world;

    private void initWorld()
    {
        world = new World();
    }

    private void addBody(Body b)
    {
        world.addBody(b);
    }

    private void createSzenario()
    {
        //Stack
        initWorld();
        //world.setGravity(0);

        int boxSize = 40;
        Shape rectangle = Shape.createRectangle(4000,10);
        Shape box = Shape.createRectangle(boxSize,boxSize);

        Body body0 = new Body( 0, 475, rectangle, false);
        addBody(body0);

        Body body1 = new Body( -2000, 0, rectangle, false);
        body1.setRotationDeg(90);
        addBody(body1);

        Body body2 = new Body( 2000, 0, rectangle, false);
        body2.setRotationDeg(90);
        addBody(body2);

        for( int i = 0; i < bodies; i++)
        {
            Body body = new Body( 200, 475 - boxSize / 2 - i * (boxSize), box, true);
            addBody(body);
        }
    }

    public void simulate(int iterations)
    {
        contactCount = 0;
        for( int i = 0; i < iterations; i++)
        {
            world.tick();
            contactCount += world.getContactCount();
        }
    }

    public static void main(String[] args)
    {
        PerformanceTest test = new PerformanceTest();
        test.createSzenario();

        long startTime = System.nanoTime();
        System.out.println("Start: ");
        System.out.println("Steps, " + simTime);
        System.out.println("Bodies: " + bodies);
        test.simulate(simTime);

        long endTime = System.nanoTime();
        System.out.println("Contacts: " + test.contactCount);
        System.out.println("End: " + ((endTime - startTime) / 1000000.0) + " ms");
    }

}

