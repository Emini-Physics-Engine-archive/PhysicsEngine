package at.emini.physics2DUnitTests;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import junit.framework.TestCase;

public class VectorTest extends TestCase {

    public VectorTest (String name)
    {
        super(name);
    }

    /**
     * vector creation
     */
    public void testBasic()
    {
        //Constructor
        FXVector v = new FXVector( 10 << FXUtil.DECIMAL, 15 << FXUtil.DECIMAL );
        assertEquals(10 * FXUtil.ONE_FX, v.xFX );
        assertEquals(15 * FXUtil.ONE_FX, v.yFX );

        //test asInt() getters
        v = new FXVector( FXUtil.ONE_FX * 10, FXUtil.ONE_FX * 15);
        assertEquals(10, v.xAsInt() );
        assertEquals(15, v.yAsInt() );

        v = new FXVector( - FXUtil.ONE_FX * 10, - FXUtil.ONE_FX * 15);
        assertEquals(-10, v.xAsInt() );
        assertEquals(-15, v.yAsInt() );

        v = new FXVector( FXUtil.ONE_FX * 10 / 4, FXUtil.ONE_FX * 15 / 4);
        assertEquals(2, v.xAsInt() );
        assertEquals(3, v.yAsInt() );

        v = new FXVector( - FXUtil.ONE_FX * 10 / 4, - FXUtil.ONE_FX * 15 / 4);
        assertEquals(-2, v.xAsInt() );
        assertEquals(-3, v.yAsInt() );

        //Factory method
        FXVector v1 = new FXVector( 10 << FXUtil.DECIMAL, 15 << FXUtil.DECIMAL );
        FXVector v2 = FXVector.newVector(10,15);
        assertEquals(v1.xFX, v2.xFX );
        assertEquals(v1.yFX, v2.yFX );

        FXVector v3 = new FXVector(v1);
        assertEquals(v1.xFX, v3.xFX );
        assertEquals(v1.yFX, v3.yFX );

        //assignment
        FXVector v4 = new FXVector();
        v4.assign(v1);
        assertEquals(v1.xAsInt(), v4.xAsInt() );
        assertEquals(v1.yAsInt(), v4.yAsInt() );

        FXVector v5 = new FXVector();
        v5.assignFX(v1.xFX, v1.yFX);
        assertEquals(v1.xAsInt(), v5.xAsInt() );
        assertEquals(v1.yAsInt(), v5.yAsInt() );

        FXVector v6 = new FXVector();
        v6.assignDiff(v1, FXVector.newVector(5, 20));
        assertEquals( 5, v6.xAsInt() );
        assertEquals(-5, v6.yAsInt() );

    }

    /**
     * Tests vector addition (and subtraction)
     */
    public void testAdd()
    {
        //Test batch 1: basic add/subtract
        FXVector v1 = FXVector.newVector( 10, 15);
        FXVector v2 = FXVector.newVector( 8, 3);

        v1.add(v2);
        assertEquals(18, v1.xAsInt());
        assertEquals(18, v1.yAsInt());

        v1.subtract(v2);
        assertEquals(10, v1.xAsInt());
        assertEquals(15, v1.yAsInt());

        v1.add(v2, FXUtil.ONE_FX * 2);
        assertEquals(26, v1.xAsInt());
        assertEquals(21, v1.yAsInt());

        v1.add2FX(v2, FXUtil.ONE_2FX * 2);
        assertEquals(42, v1.xAsInt());
        assertEquals(27, v1.yAsInt());

        //Test batch 2: negative signs
        FXVector v3 = FXVector.newVector( 8, 6);
        FXVector v4 = FXVector.newVector( -2, -2);

        v3.add(v4);
        assertEquals(6, v3.xAsInt());
        assertEquals(4, v3.yAsInt());

        v3.add(v4, 0);
        assertEquals(6, v3.xAsInt());
        assertEquals(4, v3.yAsInt());

        v3.add(v4, FXUtil.ONE_FX * 10);
        assertEquals(-14, v3.xAsInt());
        assertEquals(-16, v3.yAsInt());

        v3.add(v4, FXUtil.ONE_FX / 2);
        assertEquals(-15, v3.xAsInt());
        assertEquals(-17, v3.yAsInt());

       //Test batch 3: non integer results
        FXVector v5 = FXVector.newVector(1, 1);
        FXVector v6 = FXVector.newVector(1, 1);

        v5.add(v6);
        assertEquals(FXUtil.ONE_FX * 2, v5.xFX);
        assertEquals(FXUtil.ONE_FX * 2, v5.yFX);

        v5.add(v6, FXUtil.ONE_FX / 2);
        assertEquals(FXUtil.ONE_FX * 5 / 2, v5.xFX);
        assertEquals(FXUtil.ONE_FX * 5 / 2, v5.yFX);

        v5.add2FX(v6, FXUtil.ONE_2FX / 4);
        assertEquals(FXUtil.ONE_FX * 11 / 4, v5.xFX);
        assertEquals(FXUtil.ONE_FX * 11 / 4, v5.yFX);

    }

    /**
     * Tests vector multiplication (and division)
     */
    public void testMult()
    {
        FXVector v1 = FXVector.newVector(10,15);

        //multiplication
        v1.multFX( 2 << FXUtil.DECIMAL );
        assertEquals(20, v1.xAsInt());
        assertEquals(30, v1.yAsInt());

        v1.mult( 2 );
        assertEquals(40, v1.xAsInt());
        assertEquals(60, v1.yAsInt());

        FXVector v2 = v1.timesFX( 2 << FXUtil.DECIMAL );
        assertEquals(80, v2.xAsInt());
        assertEquals(120, v2.yAsInt());

        FXVector v3 = v1.times( 2 );
        assertEquals(80, v3.xAsInt());
        assertEquals(120, v3.yAsInt());

        assertEquals(40, v1.xAsInt());
        assertEquals(60, v1.yAsInt());

        //division
        v1 = FXVector.newVector(40, 60);
        v1.divideByFX( 2 << FXUtil.DECIMAL );
        assertEquals(20, v1.xAsInt());
        assertEquals(30, v1.yAsInt());

        v1.divideBy( 2 );
        assertEquals(10, v1.xAsInt());
        assertEquals(15, v1.yAsInt());

        //FX division
        v1.divideByFX( FXUtil.ONE_FX * 3 / 2 );
        assertEquals( FXUtil.ONE_FX * 20 / 3, v1.xFX);
        assertEquals( FXUtil.ONE_FX * 10, v1.yFX, 0.01 );

    }

    /**
     * Tests the transpose
     */
    public void testAddTranspose()
    {
        FXVector v1 = FXVector.newVector( 10, 15);

        v1.transpose();

        assertEquals(15, v1.xAsInt());
        assertEquals(10, v1.yAsInt());

        v1.turnRight();

        assertEquals( 10, v1.xAsInt());
        assertEquals(-15, v1.yAsInt());
    }


    /**
     * Tests the length methods
     */
    public void testLength()
    {
        FXVector v1 = FXVector.newVector( 10, 15);

        int lengthSquare = (10 * 10 + 15 * 15);
        assertEquals( lengthSquare, v1.lengthSquare());
        assertEquals( lengthSquare * FXUtil.ONE_FX, v1.lengthSquareFX());

        double relativeDelta1 = 0.005;
        double relativeDelta2 = 0.05;
        double length = Math.sqrt((10 * 10 + 15 * 15));
        int lengthFX = (int) (length * FXUtil.ONE_FX);
        assertEquals( lengthFX, v1.lengthFX(), lengthFX * relativeDelta1);
        assertEquals( lengthFX, v1.fastLengthFX(), lengthFX * relativeDelta2);


        FXVector v2 = FXVector.newVector( 5 , 3 );

        length = (float) Math.sqrt((5 * 5 + 12 * 12));
        lengthFX = (int) (length * FXUtil.ONE_FX);
        assertEquals( lengthFX, v1.distFX(v2), lengthFX * relativeDelta1);
        assertEquals( lengthFX, v2.distFX(v1), lengthFX * relativeDelta1);

        //normalize
        v1.normalize();
        assertEquals( FXUtil.ONE_FX, v1.lengthFX(), FXUtil.ONE_FX * relativeDelta1);

        v2.normalize();
        assertEquals( FXUtil.ONE_FX, v2.lengthFX(), FXUtil.ONE_FX * relativeDelta1);

        FXVector v3 = new FXVector();
        v3.normalize();
        assertEquals( 0, v3.xFX);
        assertEquals( 0, v3.yFX);

        FXVector v4 = FXVector.newVector( -5 , -3 );
        v4.normalize();
        assertEquals( FXUtil.ONE_FX, v4.lengthFX(), FXUtil.ONE_FX * relativeDelta1);

    }

    /**
     * Tests dot/cross methods
     */
    public void testDotCross()
    {
        FXVector a = FXVector.newVector(10, 0);
        FXVector b = FXVector.newVector(0, 2);

        assertEquals( 0, a.dotFX(b));
        assertEquals( FXUtil.ONE_FX * 20, a.crossFX(b));

        FXVector c = FXVector.newVector(2, 1);
        FXVector d = FXVector.newVector(3, 2);

        assertEquals( FXUtil.ONE_FX * 8, c.dotFX(d));
        assertEquals( FXUtil.ONE_FX * 1, c.crossFX(d));
    }

    /**
     * Tests distance method
     */
    public void testDistance()
    {
        FXVector point = new FXVector( 2 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);

        FXVector a = new FXVector(1 << FXUtil.DECIMAL, 1 << FXUtil.DECIMAL);
        FXVector b = new FXVector(1 << FXUtil.DECIMAL, 3 << FXUtil.DECIMAL);

        FXVector normal = new FXVector(b);
        normal.subtract(a);
        normal.normalize();

        assertEquals( 1.0, point.distanceFX(a, b, normal, 2 << FXUtil.DECIMAL) / (float) (1 << FXUtil.DECIMAL), 0.01);

        FXVector c = new FXVector(2 << FXUtil.DECIMAL, 1 << FXUtil.DECIMAL);
        FXVector d = new FXVector(4 << FXUtil.DECIMAL, 3 << FXUtil.DECIMAL);

        normal = new FXVector(d);
        normal.subtract(c);
        normal.normalize();

        assertEquals( Math.sqrt(0.5), point.distanceFX(c, d, normal, (int) (Math.sqrt(8) * (1 << FXUtil.DECIMAL)) ) / (float) (1 << FXUtil.DECIMAL), 0.01);

        FXVector e = new FXVector(1 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        FXVector f = new FXVector(0 << FXUtil.DECIMAL, 4 << FXUtil.DECIMAL);

        normal = new FXVector(f);
        normal.subtract(e);
        normal.normalize();

        assertTrue( point.distanceFX(e, f, normal, (int) (Math.sqrt(5) * (1 << FXUtil.DECIMAL)) ) > (1000 << FXUtil.DECIMAL));
    }

    /**
     * Tests leftof
     */
    public void testLeftOf()
    {
        FXVector p = new FXVector( 1 << FXUtil.DECIMAL, 1 << FXUtil.DECIMAL);

        FXVector a = new FXVector( 0 << FXUtil.DECIMAL, 0 << FXUtil.DECIMAL);
        FXVector b = new FXVector( 1 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        FXVector c = new FXVector( 2 << FXUtil.DECIMAL, 1 << FXUtil.DECIMAL);

        assertFalse( p.leftOf(a, b));
        assertTrue(  p.leftOf(a, c));

    }

    /**
     * Tests intersctions of lines
     */
    public void testIntersection()
    {
        FXVector a1 = new FXVector( 1 << FXUtil.DECIMAL, 0 << FXUtil.DECIMAL);
        FXVector b1 = new FXVector( 1 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        FXVector a2 = new FXVector( 0 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        FXVector b2 = new FXVector( 2 << FXUtil.DECIMAL, 3 << FXUtil.DECIMAL);
        FXVector a3 = new FXVector( 0 << FXUtil.DECIMAL, 1 << FXUtil.DECIMAL);
        FXVector b3 = new FXVector( 2 << FXUtil.DECIMAL, 1 << FXUtil.DECIMAL);

        FXVector result = new FXVector();

        assertFalse(FXVector.intersect(a1, b1, a2, b2, result));

        assertTrue(FXVector.intersect(a1, b1, a3, b3, result));
        assertEquals(1.0, result.xAsFloat(), 0.01);
        assertEquals(1.0, result.yAsFloat(), 0.01);

        FXVector a4 = new FXVector( 0 << FXUtil.DECIMAL, 0 << FXUtil.DECIMAL);
        FXVector b4 = new FXVector( 3 << FXUtil.DECIMAL, 3 << FXUtil.DECIMAL);
        FXVector a5 = new FXVector( 0 << FXUtil.DECIMAL, 2 << FXUtil.DECIMAL);
        FXVector b5 = new FXVector( 2 << FXUtil.DECIMAL, 0 << FXUtil.DECIMAL);

        assertTrue(FXVector.intersect(a1, b1, a3, b3, result));
        assertEquals(1.0, result.xAsFloat(), 0.01);
        assertEquals(1.0, result.yAsFloat(), 0.01);

    }

    /**
     * Tests max/min
     */
    public void testMax()
    {
        FXVector a = FXVector.newVector(10, 0);
        FXVector b = FXVector.newVector(1, 14);
        FXVector c = FXVector.newVector(-1, -14);

        a = FXVector.newVector(10, 0);
        a.maxFX(FXUtil.ONE_FX * 2);
        assertEquals(10, a.xAsInt());
        assertEquals( 2, a.yAsInt());

        a = FXVector.newVector(10, 10);
        a.max(b);
        assertEquals(10, a.xAsInt());
        assertEquals(14, a.yAsInt());

        a = FXVector.newVector(10, 10);
        a.min(b);
        assertEquals(1, a.xAsInt());
        assertEquals(10, a.yAsInt());

        a = FXVector.newVector(10, 10);
        a.max(c);
        assertEquals(10, a.xAsInt());
        assertEquals(10, a.yAsInt());

        a = FXVector.newVector(10, 10);
        a.min(c);
        assertEquals(-1, a.xAsInt());
        assertEquals(-14, a.yAsInt());

        a = FXVector.newVector(-10, 10);
        a.max(c);
        assertEquals(-1, a.xAsInt());
        assertEquals(10, a.yAsInt());

        a = FXVector.newVector(-10, 10);
        a.min(c);
        assertEquals(-10, a.xAsInt());
        assertEquals(-14, a.yAsInt());

    }
}
