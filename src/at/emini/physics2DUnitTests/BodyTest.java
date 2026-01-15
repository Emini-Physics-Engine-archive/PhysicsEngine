package at.emini.physics2DUnitTests;

import java.util.Vector;

import junit.framework.TestCase;
import at.emini.physics2D.Body;
import at.emini.physics2D.Shape;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class BodyTest extends TestCase {

    private Vector contacts = new Vector();
    Shape s;

    public BodyTest(String name) {
        super(name);
        FXVector[] rectCorners = new FXVector[4];
        rectCorners[0] = new FXVector(-2 << FXUtil.DECIMAL,-2 << FXUtil.DECIMAL);
        rectCorners[1] = new FXVector(-2 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        rectCorners[2] = new FXVector( 2 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        rectCorners[3] = new FXVector( 2 << FXUtil.DECIMAL,-2 << FXUtil.DECIMAL);
        s = new Shape(rectCorners);
    }

    public void setUp()
    {
        contacts.clear();
    }

    public void testBasic()
    {
        Body b = new Body( 10, 10, s, true);
        b.setRotationDeg(90);

        assertEquals(b.rotation2FX(), FXUtil.PI_2FX / 2);

        FXVector relative1 = new FXVector();
        FXVector absolute1 = b.getAbsoluePoint(relative1);

        assertEquals(10.00, absolute1.xAsFloat(), 0.01);
        assertEquals(10.00, absolute1.yAsFloat(), 0.01);

        FXVector relative2 = FXVector.newVector(2, 0);
        FXVector absolute2 = b.getAbsoluePoint(relative2);

        assertEquals(10.00, absolute2.xAsFloat(), 0.01);
        assertEquals(12.00, absolute2.yAsFloat(), 0.01);
    }

    public void testGetVertices()
    {
        Body b = new Body( 0, 0, s, true);
        b.setPositionFX( FXVector.newVector(10, 10));
        b.setRotationDeg(45);

        FXVector[] vertices = b.getVertices();

        assertEquals(4, vertices.length);

        assertEquals(10.00, vertices[0].xAsFloat(), 0.01);
        assertEquals( 7.17, vertices[0].yAsFloat(), 0.01);

        assertEquals( 7.17, vertices[1].xAsFloat(), 0.01);
        assertEquals(10.00, vertices[1].yAsFloat(), 0.01);

        assertEquals(10.00, vertices[2].xAsFloat(), 0.01);
        assertEquals(12.83, vertices[2].yAsFloat(), 0.01);

        assertEquals(12.83, vertices[3].xAsFloat(), 0.01);
        assertEquals(10.00, vertices[3].yAsFloat(), 0.01);

    }

    public void testGetAxes()
    {
        Body b = new Body( 10, 10, s, true);
        b.setRotationDeg(45);

        FXVector[] axes = b.getAxes();

        assertEquals(2, axes.length);

        assertEquals(-0.71, axes[0].xAsFloat(), 0.01);
        assertEquals( 0.71, axes[0].yAsFloat(), 0.01);

        assertEquals( 0.71, axes[1].xAsFloat(), 0.01);
        assertEquals( 0.71, axes[1].yAsFloat(), 0.01);
    }

    public void testVelocityAt()
    {
        Body b1 = new Body(0, 0, s, true);
        /*
        b1.setVelocityFX(new FXVector(1 << FXUtil.DECIMAL, 0));
        b1.angularVelocity2FX() = FXUtil.PI_2FX / 2;

        FXVector v1 = b1.getVelocity( FXVector.newVector(1, 1));
        assertEquals( 2.57 , v1.xAsFloat(), 0.01);
        assertEquals(-1.57 , v1.yAsFloat(), 0.01);*/
    }

    public void testIsFast()
    {
        /*Body b1 = new Body(0, 0, s, 1, true);

        b1.setVelocityFX(new FXVector(1 << FXUtil.DECIMAL, 0));
        assertFalse(b1.isFast());

        b1.setVelocityFX(new FXVector(1000 << FXUtil.DECIMAL, 0));
        assertTrue(b1.isFast());*/
    }

    public void testAcceleration()
    {
        Body b1 = new Body(0, 0, s, true);
        b1.applyAcceleration(FXVector.newVector(10, 0), FXUtil.ONE_FX);

        assertEquals( 10.00, b1.velocityFX().xAsFloat(), 0.01);
        assertEquals(  0.00, b1.velocityFX().yAsFloat(), 0.01);
    }

    public void testApplyImpulse()
    {
        Body b1 = new Body(0, 0, s, true);
        b1.applyMomentumAt(FXVector.newVector(10, 0), FXVector.newVector(-2, 0));

        assertEquals( 10.00, b1.velocityFX().xAsFloat(), 0.01);
        assertEquals(  0.00, b1.velocityFX().yAsFloat(), 0.01);
        assertEquals(  0, b1.angularVelocity2FX() );

        b1.applyMomentumAt(FXVector.newVector(1, 0), FXVector.newVector(-2, 2));

        assertEquals( 11.00, b1.velocityFX().xAsFloat(), 0.01);
        assertEquals(  0.00, b1.velocityFX().yAsFloat(), 0.01);
        assertEquals(  0.75, b1.angularVelocity2FX() / (float) (1 << FXUtil.DECIMAL2), 0.01);
    }

    public void testIntegrateVelocities()
    {
        /* Body b1 = new Body(0, 0, s, 1, true);
        b1.setVelocityFX(FXVector.newVector(10, 0));

        b1.integrateVelocity(FXUtil.ONE_FX);
        assertEquals( 10.00, b1.positionFX().xAsFloat(), 0.01);
        assertEquals(  0.00, b1.positionFX().yAsFloat(), 0.01);
        assertEquals(  0.00, b1.rotation2FX() / (float) (1 << FXUtil.DECIMAL2), 0.01);

        b1.integrateVelocity(FXUtil.ONE_FX);
        assertEquals( 20.00, b1.positionFX().xAsFloat(), 0.01);
        assertEquals(  0.00, b1.positionFX().yAsFloat(), 0.01);
        assertEquals(  0.00, b1.rotation2FX() / (float) (1 << FXUtil.DECIMAL2), 0.01);

        b1.angularVelocity2FX = (1 << FXUtil.DECIMAL2) / 2;

        b1.integrateVelocity(FXUtil.ONE_FX);
        assertEquals( 30.00, b1.positionFX().xAsFloat(), 0.01);
        assertEquals(  0.00, b1.positionFX().yAsFloat(), 0.01);
        assertEquals(  Math.PI * 2 - 0.50, b1.rotation2FX() / (float) (1 << FXUtil.DECIMAL2), 0.01);*/

    }
}
