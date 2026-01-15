package at.emini.physics2DSimulationTests;

import javax.swing.JFrame;
import javax.swing.UIManager;

import junit.framework.TestCase;
import at.emini.physics2D.World;
import at.emini.physics2D.util.PhysicsFileReader;
import at.emini.physics2DDesigner.Designer;

/**
 * Base class for the automated simulation test: <br>
 * 
 * Derive from this class and set the members
 * filename and simTime in the test method <br>
 * 
 * This class is derived from testcase class without implementing any test cases.
 * This is to function as base class for simulation tests. 
 * 
 * @author Alexander Adensamer
 *
 */
public class SimulationTest extends TestCase 
{

    protected String filename = ""; 
    protected int simTime = 0;
    protected TestEventListener listener;
    
    public SimulationTest(String name) {
        super(name);
    }
    
    public void setUp()
    {
        listener = new TestEventListener();
    }

    protected void addTestCriterium(int eventId, int criteriumType, int startTime, int endTime)
    {
        listener.addTestCriterium(eventId, criteriumType, startTime, endTime);
    }
    
    public void performSimTest()
    {
        String file = "/tests/" + filename; 
        PhysicsFileReader reader = new PhysicsFileReader(file);
        World world = World.loadWorld(reader);
        
        assertTrue(world != null);        
            
        world.setPhysicsEventListener(listener);
        
        for( int i = 0; i < simTime; i++)
        {
            listener.setTime(i);
            world.tick();
        }
        
        assertTrue(listener.checkTest());
    }
    
    /**
     * Dummy test method to avoid "no test method found" warning
     */
    public void testDummy()
    {        
    }
    
    
    public void startVisualTest()
    {        
        String file = "/tests/" + filename; 
        PhysicsFileReader reader = new PhysicsFileReader(file);
        
        /*Designer designer = new Designer(reader);
        designer.setSize( 800, 600 );
        designer.setVisible(true);*/      
        
        final JFrame frame = new JFrame("Emini Physics 2D Designer - " + getName());
        frame.setSize( 800, 600 );
        
        final Designer designer = new Designer(reader);
        
        frame.addWindowListener(new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) {
                if ( ! designer.checkSaved() )
                {
                    return;
                } 
                frame.dispose();
                System.exit(0);
            }
        });
        
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        frame.setContentPane(designer);
        frame.setLocation( 100, 100);
        frame.setVisible( true );
    }
    
}
