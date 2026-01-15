package at.emini.physics2DSimulationTests;
/**
*
Tests the correct behaviour of the fix joint*/
public class FixJointTest extends SimulationTest {
    public FixJointTest (String name) {
        super(name);
        filename = "FixJointTest.world";
        simTime = 100;
    }

    public void testSimulation()
    {
        addTestCriterium(0, TestEventListener.MUST_NOT_OCCUR, 0, 80);
        addTestCriterium(1, TestEventListener.MUST_NOT_OCCUR, 0, 80);
        addTestCriterium(2, TestEventListener.MUST_NOT_OCCUR, 0, 80);
        addTestCriterium(3, TestEventListener.MUST_OCCUR, 70, 80);
        performSimTest();
    }
    public static void main(String[] args)
    {
        FixJointTest test = new FixJointTest("");
        test.startVisualTest();
    }
}
