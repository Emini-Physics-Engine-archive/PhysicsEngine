package at.emini.physics2DUnitTests;

import junit.framework.TestCase;
import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class MatrixTest extends TestCase {

    public MatrixTest(String name) {
        super(name);
    }
    
    public void testBasic()
    {
        int angle2FX = FXUtil.PI_2FX / 2;
        FXMatrix matrix = FXMatrix.createRotationMatrix(angle2FX);
        
        assertEquals(  0.0, (float) matrix.mCol1xFX / (float) (1 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL)), 0.01);
        assertEquals(  1.0, (float) matrix.mCol1yFX / (float) (1 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL)), 0.01);
        assertEquals( -1.0, (float) matrix.mCol2xFX / (float) (1 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL)), 0.01);
        assertEquals(  0.0, (float) matrix.mCol2yFX / (float) (1 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL)), 0.01);
        
        FXVector vector = new FXVector( 2 << FXUtil.DECIMAL, 0);
        
        FXVector rv = matrix.mult(vector);
        
        assertEquals( 0.0, rv.xAsFloat(), 0.01);
        assertEquals( 2.0, rv.yAsFloat(), 0.01);
    }

    
    public void testMult()
    {
        FXMatrix m1 = new FXMatrix();
        m1.mCol1xFX = 1 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol1yFX = 2 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol2xFX = 3 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol2yFX = 4 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        
        FXVector v1 = FXVector.newVector(2, 3);
       
        v1 = m1.mult(v1);
       
        assertEquals( 11.0, v1.xAsFloat(), 0.01);
        assertEquals( 16.0, v1.yAsFloat(), 0.01);
        
        FXVector v2 = FXVector.newVector(2, 3);
        FXVector v3 = new FXVector();
        
        m1.mult(v2, v3);
       
        assertEquals( 11.0, v3.xAsFloat(), 0.01);
        assertEquals( 16.0, v3.yAsFloat(), 0.01);
        assertEquals(  2.0, v2.xAsFloat(), 0.01);
        assertEquals(  3.0, v2.yAsFloat(), 0.01);
        
    }
    
    public void testInversion()
    {
        FXMatrix m1 = new FXMatrix();
        m1.mCol1xFX = 2 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol1yFX = 3 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol2xFX = 4 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol2yFX = 50 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        
        FXMatrix m2 = new FXMatrix();
        m2.mCol1xFX = 2 << (FXUtil.DECIMAL);
        m2.mCol1yFX = 3 << (FXUtil.DECIMAL);
        m2.mCol2xFX = 4 << (FXUtil.DECIMAL);
        m2.mCol2yFX = 50 << (FXUtil.DECIMAL);
        
        m1.invert();
        
        FXVector col1 = m1.mult(new FXVector((int)m2.mCol1xFX, (int)m2.mCol1yFX) );
        FXVector col2 = m1.mult(new FXVector((int)m2.mCol2xFX, (int)m2.mCol2yFX));
        int epsilon = 10;
        
        assertTrue( FXUtil.ONE_FX - epsilon <= col1.xFX && col1.xFX <= FXUtil.ONE_FX + epsilon);
        assertTrue( -epsilon <= col1.yFX && col1.yFX <= epsilon);
        assertTrue( -epsilon <= col2.xFX && col2.xFX <= epsilon);
        assertTrue( FXUtil.ONE_FX - epsilon <= col2.yFX && col2.yFX <= FXUtil.ONE_FX + epsilon);
    }
    
    public void testMax()
    {
        FXMatrix m1 = new FXMatrix();
        m1.mCol1xFX = 2 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol1yFX = 3 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol2xFX = 4 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
        m1.mCol2yFX = 50 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL);
       
        assertEquals( 54 << (FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL), m1.normMatFX());
        
    }
}
