package at.emini.physics2DSimulationTests;
/**
*
Objects must not pass through the box*/
public class BoxTunnelTest extends SimulationTest {
    public BoxTunnelTest (String name) {
        super(name);
        filename = "BoxTunnelTest.world";
        simTime = 400;
    }

    public void testSimulation()
    {
        addTestCriterium(0, TestEventListener.MUST_NOT_OCCUR, 0, 400);
        performSimTest();
    }
    public static void main(String[] args)
    {
        BoxTunnelTest test = new BoxTunnelTest("");
        test.startVisualTest();
    }
}
