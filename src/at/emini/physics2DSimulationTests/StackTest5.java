package at.emini.physics2DSimulationTests;
/**
*
Tests stability of a stack of 5 boxes*/
public class StackTest5 extends SimulationTest {
    public StackTest5 (String name) {
        super(name);
        filename = "StackTest5.world";
        simTime = 4000;
    }

    public void testSimulation()
    {
        addTestCriterium(0, TestEventListener.MUST_NOT_OCCUR, 0, 4000);
        addTestCriterium(1, TestEventListener.MUST_NOT_OCCUR, 0, 4000);
        performSimTest();
    }
    
    public static void main(String[] args)
    {
        StackTest5 test = new StackTest5("");
        test.startVisualTest();
    }
}
