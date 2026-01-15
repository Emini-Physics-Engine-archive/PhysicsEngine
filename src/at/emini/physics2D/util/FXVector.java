package at.emini.physics2D.util;


/**
 * The FXVector class represents a 2D Vector.
 * 2D Vector uses fixpoint-math.<br>
 *
 * @author Alexander Adensamer
 * @see FXUtil
 */
public class FXVector
{

    /**
     * X value of the Vector (FX)
     * @fx
     */
    public int xFX = 0;
    /**
     * Y value of the Vector (FX)
     * @fx
     */
    public int yFX = 0;

    /**
     * Temporary variables used in some methods to avoid costly object creation
     */
    private static FXVector M_TmpVec1 = new FXVector();
    private static FXVector M_TmpVec2 = new FXVector();

    /**
     * Unity vector.
     */
    public static final FXVector M_UNITY = new FXVector(FXUtil.ONE_FX, 0);

    /**
     * Empty Constructor.
     */
    public FXVector()
    {
    }

    /**
     * Constructor with x and y coordinates.
     * @fx
     * @param xFx x Value of the vector (FX).
     * @param yFx y Value of the vector (FX).
     */
    public FXVector(int xFx, int yFx)
    {
        this.xFX = xFx;
        this.yFX = yFx;
    }

    /**
     * Copy constructor.
     * @param other vector
     */
    public FXVector(FXVector other)
    {
        this.xFX = other.xFX;
        this.yFX = other.yFX;
    }

    /**
     * Creates a new vector.
     * The parameters are supplied as non-FX values.
     * @param x x Value of the vector.
     * @param y y Value of the vector.
     * @return newly created vector.
     */
    public static FXVector newVector(int x, int y)
    {
        return new FXVector( x << FXUtil.DECIMAL, y << FXUtil.DECIMAL);
    }

    /**
     * Copies the values of another vector.
     * @param other source vector
     */
    public final void assign (FXVector other)
    {
        this.xFX = other.xFX;
        this.yFX = other.yFX;
    }

    /**
     * Assign values to the vector.
     * @fx
     * @param xFX x Value of the vector (FX).
     * @param yFX y Value of the vector (FX).
     */
    public final void assignFX (int xFX, int yFX)
    {
        this.xFX = xFX;
        this.yFX = yFX;
    }


    /**
     * Assigns the difference vector between two points/vectors.
     * Calculates: v1 - v2.
     * @param v1 end point of the vector.
     * @param v2 start point of the vector.
     */
    public final void assignDiff (FXVector v1, FXVector v2)
    {
        this.xFX = v1.xFX - v2.xFX;
        this.yFX = v1.yFX - v2.yFX;
    }

    /**
     * Copies and scaled the values of another vector.
     * @fx
     * @param other source vector
     * @param scaleFX the scale factor
     */
    public final void assignScaledFX (FXVector other, long scaleFX)
    {
        xFX = (int)((scaleFX * (long) other.xFX ) >> FXUtil.DECIMAL);
        yFX = (int)((scaleFX * (long) other.yFX ) >> FXUtil.DECIMAL);
    }

    /**
     * Equals operator for vectors.
     * @return true if the vectors are euqal.
     */
    public boolean equals(Object other)
    {
        if (other instanceof FXVector)
        {
            return xFX == ((FXVector) other).xFX &&
                   yFX == ((FXVector) other).yFX;
        }
        return false;
    }

    /**
     * Adds a vector.
     * @param other vector to add.
     */
    public final void add( FXVector other)
    {
        xFX += other.xFX;
        yFX += other.yFX;
    }

    /**
     * Adds a vector after scaling it.
     * @fx
     * @param vector vector to add.
     * @param scaleFX scaling factor of the add Vector (FX).
     */
    public final void add( FXVector vector, long scaleFX)
    {
        xFX += (int)((scaleFX * (long) vector.xFX ) >> FXUtil.DECIMAL);
        yFX += (int)((scaleFX * (long) vector.yFX ) >> FXUtil.DECIMAL);
    }

    /**
     * Adds a vector (given as two coordinates) after scaling it.
     * @fx
     * @param xFX x coordinate (FX) to add.
     * @param yFX y coordinate (FX) to add.
     * @param scaleFX scaling factor of the add Vector.
     */
    public final void addFX( int xFX, int yFX, long scaleFX)
    {
        this.xFX += (int)((scaleFX * (long) xFX ) >> FXUtil.DECIMAL);
        this.yFX += (int)((scaleFX * (long) yFX ) >> FXUtil.DECIMAL);
    }

    /**
     * Adds a vector after scaling it (2FX).
     * The scaling factor is supplied as double precision FX int.
     * @fx
     * @param other vector to add.
     * @param scale2FX scaling factor (2FX) of the add Vector.
     */
    public final void add2FX( FXVector other, long scale2FX)
    {
        xFX += (int)((scale2FX * (long) other.xFX ) >> FXUtil.DECIMAL2);
        yFX += (int)((scale2FX * (long) other.yFX ) >> FXUtil.DECIMAL2);
    }

    /**
     * Subtract a vector.
     * @param vector vector to subtract
     */
    public final void subtract( FXVector vector)
    {
        xFX -= vector.xFX;
        yFX -= vector.yFX;
    }

    /**
     * Scales the vector (FX).
     * @fx
     * @param valFX scale factor (FX).
     */
    public final void multFX( long valFX )
    {
        xFX = (int)((valFX * (long) xFX ) >> FXUtil.DECIMAL);
        yFX = (int)((valFX * (long) yFX ) >> FXUtil.DECIMAL);
    }

    /**
     * Scales the vector.
     * @param val scale factor.
     */
    public final void mult( int val )
    {
        xFX = xFX * val;
        yFX = yFX * val;
    }

    /**
     * Creates a copy multiplied by a factor (FX).
     * @fx
     * @param valFX scale factor (FX).
     */
    public final FXVector timesFX( long valFX )
    {
        FXVector newvec = new FXVector();
        newvec.xFX = (int)((valFX * (long) xFX ) >> FXUtil.DECIMAL);
        newvec.yFX = (int)((valFX * (long) yFX ) >> FXUtil.DECIMAL);
        return newvec;
    }

    /**
     * Creates a copy multiplied by a factor.
     * @param val scale factor
     */
    public final FXVector times( int val )
    {
        FXVector newvec = new FXVector();
        newvec.xFX = xFX * val;
        newvec.yFX = yFX * val;
        return newvec;
    }

    /**
     * Creates a copy divided by a factor (FX).
     * @fx
     * @param valFX divide factor (FX)
     */
    public final FXVector dividedByFX( int valFX )
    {
        FXVector newvec = new FXVector();
        newvec.xFX = (int)(((long)xFX << FXUtil.DECIMAL)/ valFX);
        newvec.yFX = (int)(((long)yFX << FXUtil.DECIMAL)/ valFX);
        return newvec;
    }

    /**
     * Divides the vector (FX).
     * @fx
     * @param valFX divide factor (FX).
     */
    public final void divideByFX( int valFX )
    {
        xFX = (int)(((long)xFX << FXUtil.DECIMAL)/ valFX);
        yFX = (int)(((long)yFX << FXUtil.DECIMAL)/ valFX);
    }

    /**
     * Divides the vector.
     * @fx
     * @param val scale factor.
     */
    public final void divideBy( int val )
    {
        xFX = xFX / val;
        yFX = yFX / val;
    }

    /**
     * Transposes the vector.
     */
    public final void transpose()
    {
        xFX = xFX ^ yFX;            //#FX2F float tmp = xFX;
        yFX = xFX ^ yFX;            //#FX2F xFX = yFX;
        xFX = xFX ^ yFX;            //#FX2F yFX = tmp;
    }

    /**
     * Turns the vector to the right by 90 degrees.
     */
    public final void turnRight()
    {
        int tmpFX = xFX;
        xFX = yFX;
        yFX = - tmpFX;
    }

    /**
     * Calculates the square of the length of the vector.
     * @return the square of the length.
     */
    public final int lengthSquare()
    {
        return (int) (((long) xFX * (long) xFX + (long) yFX * (long) yFX) >> (FXUtil.DECIMAL2));  //#FX2F return (int) (xFX * xFX + yFX * yFX);
    }

    /**
     * Calculates the square of the length of the vector (FX).
     * @fx
     * @return the square of the length (FX).
     */
    public final int lengthSquareFX()
    {
        return (int) (((long) xFX * (long) xFX + (long) yFX * (long) yFX) >> (FXUtil.DECIMAL));
    }

    /**
     * Calculates the length of the vector.
     * This method is more precise, but slower than the {@link FXVector#fastLengthFX()}.
     * @fx
     * @return the length of the vector(FX).
     */
    public final int lengthFX()
    {
        int ix = (xFX<0 ? -xFX : xFX);                              //#FX2F return (float) Math.sqrt(xFX * xFX + yFX * yFX);
        int iy = (yFX<0 ? -yFX : yFX);                              //#FX2F

        if(ix<iy)                                                   //#FX2F
        {                                                           //#FX2F
          ix=ix^iy;                                                 //#FX2F
          iy=ix^iy;                                                 //#FX2F
          ix=ix^iy;                                                 //#FX2F
        }                                                           //#FX2F

        int t = iy + (iy>>1);                                       //#FX2F

        int currGuess = (ix - (ix>>5) - (ix>>7)  + (t>>2) + (t>>6));//#FX2F
        //add a single newton step to improve the squareroot        //#FX2F
        if (currGuess == 0) return 0;                               //#FX2F
        currGuess = (int) ((( ((long) xFX * (long) xFX + (long) yFX * (long) yFX) / currGuess) ) + currGuess) >> 1; //#FX2F
        return currGuess;                                           //#FX2F
    }

    /**
     * Calculates the length of the vector.
     * This method is more precise, but slower than the {@link FXVector#fastLengthFX()}.
     * @fx
     * @return the length of the vector(FX).
     */
    public final int preciseLengthFX()
    {
        int ix = (xFX<0 ? -xFX : xFX);                              //#FX2F return (float) Math.sqrt(xFX * xFX + yFX * yFX);
        int iy = (yFX<0 ? -yFX : yFX);                              //#FX2F

        if(ix<iy)                                                   //#FX2F
        {                                                           //#FX2F
          ix=ix^iy;                                                 //#FX2F
          iy=ix^iy;                                                 //#FX2F
          ix=ix^iy;                                                 //#FX2F
        }                                                           //#FX2F

        int t = iy + (iy>>1);                                       //#FX2F

        int currGuess = (ix - (ix>>5) - (ix>>7)  + (t>>2) + (t>>6));//#FX2F
        //add two newton steps to improve the squareroot            //#FX2F
        if (currGuess == 0) return 0;                               //#FX2F
        currGuess = (int) ((( ((long) xFX * (long) xFX + (long) yFX * (long) yFX) / currGuess) ) + currGuess) >> 1; //#FX2F
        currGuess = (int) ((( ((long) xFX * (long) xFX + (long) yFX * (long) yFX) / currGuess) ) + currGuess) >> 1; //#FX2F
        return currGuess;                                           //#FX2F
    }

    /**
     * Calculates the length of a vector, supplied as x and y coordinates (fast).
     * Convenience method to avoid creating a vector.
     * @fx
     * @param xFX the x coordinate (FX).
     * @param yFX the y coordinate (FX).
     * @return the length of the vector(FX).
     */
    public static final int fastLengthFX(int xFX, int yFX)
    {
        int ix = (xFX<0 ? -xFX : xFX);                              //#FX2F return (float) Math.sqrt(xFX * xFX + yFX * yFX);
        int iy = (yFX<0 ? -yFX : yFX);                              //#FX2F

        if(ix<iy)                                                   //#FX2F
        {                                                           //#FX2F
          ix=ix^iy;                                                 //#FX2F
          iy=ix^iy;                                                 //#FX2F
          ix=ix^iy;                                                 //#FX2F
        }                                                           //#FX2F

        int t = iy + (iy>>1);                                       //#FX2F

        return (ix - (ix>>5) - (ix>>7)  + (t>>2) + (t>>6));         //#FX2F
    }

    /**
     * Calculates the length of the vector (fast).
     * Convenience method to avoid creating a vector.
     * This method is faster but less precise than the {@link FXVector#lengthFX()}.
     * @fx
     * @return the length of the vector(FX).
     */
    public final int fastLengthFX()
    {
        int ix = (xFX<0 ? -xFX : xFX); /* absolute values */        //#FX2F return (float) Math.sqrt(xFX * xFX + yFX * yFX);
        int iy = (yFX<0 ? -yFX : yFX);                              //#FX2F

        if(ix<iy)          /* swap ix and iy if (ix < iy) */        //#FX2F
        {                  /* See Wyvill (G1, 436)        */        //#FX2F
          ix=ix^iy;                                                 //#FX2F
          iy=ix^iy;                                                 //#FX2F
          ix=ix^iy;                                                 //#FX2F
        }                                                           //#FX2F

        int t = iy + (iy>>1);                                       //#FX2F

        return (ix - (ix>>5) - (ix>>7)  + (t>>2) + (t>>6));         //#FX2F
    }

    /**
     * Distance to another vector.
     * @fx
     * @param other other vector.
     * @return the distance (FX).
     */
    public final int distFX(FXVector other)
    {
        M_TmpVec1.assignDiff(other, this);
        return M_TmpVec1.lengthFX();
    }

    /**
     * Normalizes the vector.
     * The vector is scaled so that its length becomes 1.
     */
    public final void normalize()
    {
        int tmpLongFX = lengthFX();
        if (tmpLongFX == 0) return;
        xFX = (int) (((long)xFX << FXUtil.DECIMAL)/ tmpLongFX);
        yFX = (int) (((long)yFX << FXUtil.DECIMAL)/ tmpLongFX);
    }

    /**
     * Normalizes the vector.
     * The vector is scaled so that its length becomes 1.
     */
    public final void normalizePrecise()
    {
        int tmpLongFX = preciseLengthFX();
        if (tmpLongFX == 0) return;
        xFX = (int) (((long)xFX << FXUtil.DECIMAL)/ tmpLongFX);
        yFX = (int) (((long)yFX << FXUtil.DECIMAL)/ tmpLongFX);
    }

    /**
     * Normalizes the vector (fast).
     * This uses the fast length method ({@link FXVector#fastLengthFX()}).
     */
    public final void normalizeFast()
    {
        int tmpLongFX = fastLengthFX();
        if (tmpLongFX == 0) return;
        xFX = (int) (((long)xFX << FXUtil.DECIMAL)/ tmpLongFX);
        yFX = (int) (((long)yFX << FXUtil.DECIMAL)/ tmpLongFX);
    }

    /**
     * Computes the dot product of two vectors.
     * @fx
     * @param other vector to compute the dot product with.
     * @return the dot product (FX).
     */
    public final long dotFX(FXVector other)
    {
        return ((((long) xFX * (long) other.xFX) + ((long) yFX * (long) other.yFX )) >> FXUtil.DECIMAL);
    }

    /**
     * Computes the cross product of two vectors.<br>
     * Note: The 2D cross product returns the z coordinate of the 3D vector (0,0,z).
     * @fx
     * @param other vector to compute the cross product with.
     * @return the cross product (FX).
     */
    public final long crossFX(FXVector other)
    {
        return ((((long) xFX * (long) other.yFX) - ((long) yFX * (long) other.xFX )) >> FXUtil.DECIMAL);
    }

    /**
     * Computes cross product with a scalar (FX).
     * @fx
     * @param valFX scale factor (FX) for the unit vector of the cross product.
     */
    public final void crossScalarFX( long valFX )
    {
        long tmpLongFX = xFX;
        xFX =  (int)((valFX * (long) yFX ) >> FXUtil.DECIMAL);
        yFX = -(int)((valFX * tmpLongFX ) >> FXUtil.DECIMAL);
    }

    /**
     * Computes cross product with a scalar (2FX).
     * @fx
     * @param val2FX scale factor (2FX) for the unit vector of the cross product.
     */
    public final void crossScalar2FX( long val2FX )
    {
        long tmpLongFX = xFX;
        xFX =  (int)((val2FX * (long) yFX ) >> FXUtil.DECIMAL2);
        yFX = -(int)((val2FX * tmpLongFX ) >> FXUtil.DECIMAL2);
    }

    /**
     * Calculates the distance to a line (supplying normal and distance).
     * This method is faster than {@link FXVector#distanceFX(FXVector, FXVector)}
     * if the normal and distance are available and do not have to be computed.
     * @fx
     * @param a start point of the line.
     * @param b end point of the line.
     * @param n the normalized vector between a and b (b - a)/|b - a|.
     * @param abDistFX distance between a and b.
     * @return the distance of the point to the line if the projection lies between a and b,
     * otherwise maxint.
     */
    public final int distanceFX( FXVector a, FXVector b, FXVector n, int abDistFX)
    {
        M_TmpVec1.assignDiff(this, a);
        long tmpLongFX = M_TmpVec1.dotFX(n); //projection

        if (tmpLongFX < 0 )
        {
            return (1 << 31) - 1;
        }

        if (tmpLongFX > abDistFX)
        {
            return (1 << 31) - 1;
        }

        return (int) Math.abs(M_TmpVec1.crossFX(n));
    }

    /**
     * Calculates the distance to a line.
     * @fx
     * @param a start point of the line.
     * @param b end point of the line.
     * @return the distance of the point to the line if the projection lies between a and b,
     * otherwise maxint.
     */
    public final int distanceFX( FXVector a, FXVector b)
    {
        M_TmpVec2.assignDiff(b, a);
        M_TmpVec2.normalize();
        return distanceFX( a, b, M_TmpVec2, a.distFX(b));
    }

    /**
     * Determines the hemisphere of a point with respect to a line.
     * @param a start point of line segment.
     * @param b end point of line segment.
     * @return true if this point lies left of the line segment a-b.
     */
    public final boolean leftOf(FXVector a, FXVector b)
    {
        //this = c
        M_TmpVec1.assignDiff(b, a);
        M_TmpVec2.assignDiff(this, a);

        return M_TmpVec1.crossFX(M_TmpVec2) > 0;
    }

    /**
     * Intersects two lines.
     * @param a1 start of line 1.
     * @param b1 end of line 1.
     * @param a2 start of line 2.
     * @param b2 end of line 2.
     * @param x target vector for the intersection point.
     * @return triue if two lines intersect.
     */
    public static boolean intersect(FXVector a1, FXVector b1, FXVector a2, FXVector b2, FXVector x)
    {
        FXVector tmpVec1 = FXVector.M_TmpVec1;
        FXVector tmpVec2 = FXVector.M_TmpVec2;
        tmpVec1.assignDiff(b1, a1);
        tmpVec2.assignDiff(b2, a2);

        long normFX = tmpVec2.crossFX(tmpVec1);
        if (normFX == 0)
        {
            return false;
        }

        long sFX = a1.crossFX(tmpVec1) - a2.crossFX(tmpVec1);
        long tFX = a1.crossFX(tmpVec2) - a2.crossFX(tmpVec2);

        if (normFX > 0 && (sFX < -normFX || sFX > normFX || tFX < -normFX || tFX > normFX )
            || normFX < 0 && (sFX > -normFX || sFX < normFX || tFX > -normFX || tFX < normFX ) )
        {
            return false;
        }

        int refFX  = a1.xFX + (int) (((long) tFX * (long) tmpVec1.xFX ) / normFX);
        int ref2FX = a2.xFX + (int) (((long) sFX * (long) tmpVec2.xFX ) / normFX);
        x.xFX = (refFX + ref2FX)/2;

        refFX  = a1.yFX + (int) (((long) tFX * (long) tmpVec1.yFX ) / normFX);
        ref2FX = a2.yFX + (int) (((long) sFX * (long) tmpVec2.yFX ) / normFX);
        x.yFX = (refFX + ref2FX)/2;

        return true;

    }

    /**
     * Maximizes both x and y component.
     * @fx
     * @param cFX minimum value for each coordinate
     */
    public final void maxFX(int cFX)
    {
        xFX = Math.max(xFX, cFX);
        yFX = Math.max(yFX, cFX);
    }

    /**
     * Minimizes both x and y component.
     * @fx
     * @param cFX maximum value for each coordinate.
     */
    public final void minFX(int cFX)
    {
        xFX = Math.min(xFX, cFX);
        yFX = Math.min(yFX, cFX);
    }

    /**
     * Maximizes the vector to a reference vector.
     * Each coordinate is treated separately.
     * @param vector minimum value for each coordinate.
     */
    public final void max(FXVector vector)
    {
        xFX = Math.max(xFX, vector.xFX);
        yFX = Math.max(yFX, vector.yFX);
    }

    /**
     * Minimizes the vector to a reference vector.
     * Each coordinate is treated separately.
     * @param vector maximum value for each coordinate.
     */
    public final void min(FXVector vector)
    {
        xFX = Math.min(xFX, vector.xFX);
        yFX = Math.min(yFX, vector.yFX);
    }

    /**
     * Checks if vector is within a rectangle.
     * @param ul the upper left corner of the rectangle.
     * @param lr the lower right corner of the rectangle.
     * @return true if the vector is within the rectangle.
     */
    public final boolean isInRect(FXVector ul, FXVector lr)
    {
        return xFX >= ul.xFX &&
               yFX >= ul.yFX &&
               xFX <= lr.xFX &&
               yFX <= lr.yFX;
    }

    /**
     * Gets the x coordinate as int.
     * @return the x coordinate as int.
     */
    public final int xAsInt()
    {
        if (xFX >= 0)
            return xFX >> FXUtil.DECIMAL;   //#FX2F return (int) xFX;
        else
            return (xFX + FXUtil.ONE_FX - 1) >> FXUtil.DECIMAL;   //#FX2F return (int) xFX;
    }

    /**
     * Gets the y coordinate as int.
     * @return the y coordinate as int.
     */
    public final int yAsInt()
    {
        if (yFX >= 0)
            return yFX >> FXUtil.DECIMAL;   //#FX2F return (int) yFX;
        else
            return (yFX + FXUtil.ONE_FX - 1) >> FXUtil.DECIMAL;   //#FX2F return (int) yFX;
    }

    //debug methods                                                                 //#NoJ2ME
    public float xAsFloat()                                                         //#NoJ2ME
    {                                                                               //#NoJ2ME
        return (float) (xFX) / (1 << FXUtil.DECIMAL);                               //#NoJ2ME
    }                                                                               //#NoJ2ME

    public float yAsFloat()                                                         //#NoJ2ME
    {                                                                               //#NoJ2ME
        return (float) (yFX) / (1 << FXUtil.DECIMAL);                               //#NoJ2ME
    }                                                                               //#NoJ2ME

    public void dumpToConsole()                                                     //#NoJ2ME
    {                                                                               //#NoJ2ME
        System.out.print("("+ xAsFloat() + ", " + yAsFloat()+ ")");                 //#NoJ2ME
    }                                                                               //#NoJ2ME

    public void dumpToConsole(String arg)                                           //#NoJ2ME
    {                                                                               //#NoJ2ME
        System.out.print("("+ xAsFloat() + ", " + yAsFloat()+ ")" + arg);           //#NoJ2ME
    }                                                                               //#NoJ2ME

    public void dumpToConsole(String arg1, String arg2)                             //#NoJ2ME
    {                                                                               //#NoJ2ME
        System.out.print(arg1 + "("+ xAsFloat() + ", " + yAsFloat()+ ")" + arg2);   //#NoJ2ME
    }                                                                               //#NoJ2ME
}
