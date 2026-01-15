package at.emini.physics2DSimulationTests;
/**
*
check that no energy is pumped into the system by the restitution.
TODO: set elastictiy of objects to 1.0 (instead of 0.99)*/
public class ElasticityTest extends SimulationTest {
    public ElasticityTest (String name) {
        super(name);
        filename = "ElasticityTest.world";
        simTime = 500;
    }

    public void testSimulation()
    {
        addTestCriterium(0, TestEventListener.MUST_NOT_OCCUR, 0, 1000);
        performSimTest();
    }
    public static void main(String[] args)
    {
        ElasticityTest test = new ElasticityTest("");
        test.startVisualTest();
    }
}
