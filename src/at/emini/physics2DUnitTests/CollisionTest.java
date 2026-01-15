package at.emini.physics2DUnitTests;

import junit.framework.TestCase;
import at.emini.physics2D.Body;
import at.emini.physics2D.Collision;
import at.emini.physics2D.Contact;
import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXUtil;

public class CollisionTest extends TestCase {

    Shape rect;
    Shape box;
    public CollisionTest(String name) {
        super(name);

        rect = Shape.createRectangle(10, 20);
        box = Shape.createRectangle(10, 10);
    }

    public void testNoCollision()
    {

        Body b1 = new Body(0, 0, rect, true );
        Body b2 = new Body(40, 30, rect, true );
        b2.setRotationDeg(90);

        Contact c = Collision.detectCollision(b1, b2);

        assertEquals(null, c);
    }

    public void testSingleCollision()
    {

        Body b1 = new Body(0, 0, rect, true );
        Body b2 = new Body(0, 15, box, true );
        b2.setRotationDeg(45);

        Contact c = Collision.detectCollision(b1, b2);

        assertFalse(null == c);
        assertTrue(c.isSingle());

        assertEquals( 0.00, c.getNormal().xAsFloat(), 0.01);
        assertEquals(-1.00, c.getNormal().yAsFloat(), 0.01);

        assertEquals( 0.00, c.getContactPosition1().xAsFloat(), 0.01);
        assertEquals( 7.93, c.getContactPosition1().yAsFloat(), 0.01);

        assertEquals( 2.07, c.getDepth1FX() / (float) (1 <<FXUtil.DECIMAL), 0.01);

    }

    public void testDoubleCollision()
    {

    }

}
