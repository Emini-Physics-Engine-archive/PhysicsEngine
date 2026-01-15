package at.emini.physics2DSimulationTests;
/**
*
Tests the Performance of the engine*/
public class PerformanceTest extends SimulationTest {
    public PerformanceTest (String name) {
        super(name);
        filename = "PerformanceTest.world";
        simTime = 200;
    }

    public void testSimulation()
    {
        addTestCriterium(0, TestEventListener.MUST_NOT_OCCUR, 0, 20);
        performSimTest();
    }
    public static void main(String[] args)
    {
        PerformanceTest test = new PerformanceTest("");
        test.startVisualTest();
    }
}
