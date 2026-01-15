package at.emini.physics2DSimulationTests;
/**
*
Tests correct collision of polygon and Circle*/
public class CirclePolyCollisionTest extends SimulationTest {
    public CirclePolyCollisionTest (String name) {
        super(name);
        filename = "CirclePolyCollisionTest.world";
        simTime = 40;
    }

    public void testSimulation()
    {
        addTestCriterium(0, TestEventListener.MUST_OCCUR, 0, 40);
        performSimTest();
    }

    public static void main(String[] args)
    {
        CirclePolyCollisionTest test = new CirclePolyCollisionTest("");
        test.startVisualTest();
    }
}
