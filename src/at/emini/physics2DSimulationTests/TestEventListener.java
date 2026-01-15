package at.emini.physics2DSimulationTests;

import java.util.Vector;

import at.emini.physics2D.Event;
import at.emini.physics2D.PhysicsEventListener;

public class TestEventListener implements PhysicsEventListener {

    public static final int MUST_OCCUR = 1;
    public static final int MUST_NOT_OCCUR = 2;

    private Vector testCriteria = new Vector();
    private Vector triggeredEvents = new Vector();

    private int currentTime = 0;

    private class TestCriterium
    {
        public TestCriterium(int eventId, int criteriumType, int startTime, int endTime)
        {
            this.eventId = eventId;
            this.criteriumType = criteriumType;
            this.startTime = startTime;
            this.endTime = endTime;
        }
        public int eventId;
        public int criteriumType;
        public int startTime;
        public int endTime;
    }

    private class EventTriggered
    {
        public EventTriggered(int eventId, int triggerTime)
        {
            this.eventId = eventId;
            this.triggerTime = triggerTime;
        }
        public int eventId;
        public int triggerTime;
    }

    public TestEventListener()
    {
    }

    public void setTime(int currTime)
    {
        currentTime = currTime;
    }

    public boolean checkTest()
    {
        boolean testSuccess = true;

        //check each criterium
        for( int i = 0; i < testCriteria.size(); i++)
        {
            TestCriterium criterium = ((TestCriterium) testCriteria.elementAt(i));
            boolean eventOccurs = false;
            //check range
            for( int j = 0; j < triggeredEvents.size(); j++)
            {
                EventTriggered event = ((EventTriggered) triggeredEvents.elementAt(j));
                if (event.eventId == criterium.eventId &&
                    event.triggerTime >= criterium.startTime &&
                    event.triggerTime <= criterium.endTime)
                {
                    eventOccurs = true;
                    break;
                }
            }

            if (criterium.criteriumType == MUST_OCCUR && ! eventOccurs)
            {
                System.err.println("Exepected Event " + criterium.eventId +
                        " in [" + criterium.startTime + ", " + criterium.endTime + "]");
                testSuccess = false;
                break;
            }
            else if (criterium.criteriumType == MUST_NOT_OCCUR && eventOccurs)
            {
                System.err.println("Not Exepected Event " + criterium.eventId +
                        " in [" + criterium.startTime + ", " + criterium.endTime + "]");
                testSuccess = false;
                break;
            }
        }

        return testSuccess;
    }

    public void addTestCriterium(int eventId, int criteriumType, int startTime, int endTime)
    {
        testCriteria.addElement(new TestCriterium(eventId, criteriumType, startTime, endTime) );
    }

    public void eventTriggered(Event e, Object param) {
        triggeredEvents.addElement(new EventTriggered(e.getIdentifier(), currentTime) );
    }


}
