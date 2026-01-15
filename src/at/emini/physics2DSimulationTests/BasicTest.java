package at.emini.physics2DSimulationTests;

/**
 * First automated test - ensuring the capabilities of the automated simulation test system
 * @author Alexander Adensamer
 *
 */
public class BasicTest extends SimulationTest {

    
    public BasicTest(String name) {
        super(name);
        
        filename = "test_basic.world"; 
        simTime = 1;        
    }

    
    public void testSimulation()
    {
        addTestCriterium(0, TestEventListener.MUST_OCCUR, 0, 1);
        addTestCriterium(0, TestEventListener.MUST_NOT_OCCUR, 2, 100);
        addTestCriterium(1, TestEventListener.MUST_NOT_OCCUR, 0, 100);
        
        performSimTest();
    }
    
    public static void main(String[] args)
    {
        BasicTest test = new BasicTest("");
        test.startVisualTest();
    }
    
}
