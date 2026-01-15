package at.emini.physics2DSimulationTests;
/**
*
Shows that the objects stay always on the facing side of a landscape element. */
public class LandscapeFacesTest extends SimulationTest {
    public LandscapeFacesTest (String name) {
        super(name);
        filename = "LandscapeFacesTest.world";
        simTime = 60;
    }

    public void testSimulation()
    {
        addTestCriterium(0, TestEventListener.MUST_OCCUR, 0, 60);
        addTestCriterium(1, TestEventListener.MUST_NOT_OCCUR, 0, 60);
        addTestCriterium(2, TestEventListener.MUST_OCCUR, 0, 60);
        addTestCriterium(3, TestEventListener.MUST_NOT_OCCUR, 0, 60);
        addTestCriterium(4, TestEventListener.MUST_OCCUR, 0, 60);
        addTestCriterium(5, TestEventListener.MUST_NOT_OCCUR, 0, 60);
        addTestCriterium(6, TestEventListener.MUST_OCCUR, 0, 60);
        performSimTest();
    }
    public static void main(String[] args)
    {
        LandscapeFacesTest test = new LandscapeFacesTest("");
        test.startVisualTest();
    }
}
