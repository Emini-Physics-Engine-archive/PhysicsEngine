package at.emini.physics2DUnitTests;

import junit.framework.TestCase;
import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class ShapeTest extends TestCase {

    public ShapeTest(String name) {
        super(name);
    }

    public void testPolygonArea()
    {
        FXVector[] rectCorners = new FXVector[4];
        rectCorners[0] = new FXVector(-2 << FXUtil.DECIMAL,-2 << FXUtil.DECIMAL);
        rectCorners[1] = new FXVector(-2 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        rectCorners[2] = new FXVector( 2 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        rectCorners[3] = new FXVector( 2 << FXUtil.DECIMAL,-2 << FXUtil.DECIMAL);
        Shape rect = new Shape(rectCorners);

        assertEquals(16 << FXUtil.DECIMAL, rect.getAreaFX());
    }


}
