package at.emini.physics2D.util;

/**
 * The FXMatrix class represents a 2x2 matrix. <br>
 * The matrix uses Fixpoint arithmetic.
 * In order to optimize rotation matrices, which are its main use,
 * the values are shifted by {@link FXUtil#DECIMAL} + {@link FXUtil#ADD_MATRIX_DECIMAL}.<br>
 *
 * @author Alexander Adensamer
 * @see FXUtil
 */
public class FXMatrix
{
    /**
     * a11 (MatFX)
     * @fx
     */
    public long mCol1xFX = 0;
    /**
     * a12 (MatFX)
     * @fx
     */
    public long mCol1yFX = 0;
    /**
     * a21 (MatFX)
     * @fx
     */
    public long mCol2xFX = 0;
    /**
     * a22 (MatFX)
     * @fx
     */
    public long mCol2yFX = 0;

    private int mPrecision = FXUtil.DECIMAL + FXUtil.ADD_MATRIX_DECIMAL;

    /**
     * Empty constructor.
     */
    public FXMatrix()
    {
    }

    public FXMatrix(int precision)
    {
        this.mPrecision = precision;
    }

    /**
     * Copy constructor.
     * @param other the matrix to copy.
     */
    public FXMatrix(FXMatrix other)
    {
        mCol1xFX = other.mCol1xFX;
        mCol1yFX = other.mCol1yFX;
        mCol2xFX = other.mCol2xFX;
        mCol2yFX = other.mCol2yFX;
        mPrecision = other.mPrecision;
    }

    public void assign(FXMatrix other)
    {
        mCol1xFX = other.mCol1xFX;
        mCol1yFX = other.mCol1yFX;
        mCol2xFX = other.mCol2xFX;
        mCol2yFX = other.mCol2yFX;
        mPrecision = other.mPrecision;
    }


    /**
     * Creates a 2D rotation matrix.
     * @fx
     * @param rotation2FX rotation angle in radians (2FX).
     */
    public static FXMatrix createRotationMatrix(int rotation2FX)
    {
        FXMatrix matrix = new FXMatrix();
        matrix.setRotationMatrix(rotation2FX);
        return matrix;
    }

    /**
     * Sets the matrix to a rotation matrix.
    * @param rotation2FX rotation angle in radians (2FX).
    */
    public void setRotationMatrix(int rotation2FX)
    {
        //int cFX = FXUtil.cosFX[rotationFX >> 4];
        int cosRot2FX = (rotation2FX + FXUtil.PI_2FX / 2) >> 16;                    //#FX2F
        if ( cosRot2FX > FXUtil.PI_2FX >> 15) cosRot2FX -= FXUtil.PI_2FX >> 15;     //#FX2F
        int cFX = FXUtil.M_sinMatFX[cosRot2FX];                                       //#FX2F float cFX = (float) Math.cos(rotation2FX);
        int sFX = FXUtil.M_sinMatFX[rotation2FX >> 16];                               //#FX2F float sFX = (float) Math.sin(rotation2FX);
        mCol1xFX = cFX;   mCol2xFX = -sFX;
        mCol1yFX = sFX;   mCol2yFX = cFX;
    }

    /**
     * Multiplies the matrix with vector.
     * @param vector the vector to multiply
     * @return a new vector, that is the result of the matrix multiplication.
     */
    public final FXVector mult(FXVector vector)
    {
        FXVector rv = new FXVector();
        int precision = this.mPrecision;
        rv.xFX = (int) (((mCol1xFX * (long)vector.xFX) >> (precision)) +          //#FX2F rv.xFX = mCol1xFX * vector.xFX + mCol2xFX * vector.yFX;
                        ((mCol2xFX * (long)vector.yFX) >> (precision)));          //#FX2F
        rv.yFX = (int) (((mCol1yFX * (long)vector.xFX) >> (precision)) +          //#FX2F rv.yFX = mCol1yFX * vector.xFX + mCol2yFX * vector.yFX;
                        ((mCol2yFX * (long)vector.yFX) >> (precision)));          //#FX2F
        return rv;
    }

    /**
     * Multiplies the matrix with vector given as two coordinates (FX).
     * @fx
     * @param xFX x coordinate (FX).
     * @param yFX y coordinate (FX).
     * @return a new vector, that is the result of the matrix multiplication.
     */
    public final FXVector mult(int xFX, int yFX)
    {
        FXVector rv = new FXVector();
        int precision = this.mPrecision;
        rv.xFX = (int) (((mCol1xFX * (long)xFX) >> (precision)) +         //#FX2F rv.xFX = mCol1xFX * xFX + mCol2xFX * yFX;
                        ((mCol2xFX * (long)yFX) >> (precision)));         //#FX2F
        rv.yFX = (int) (((mCol1yFX * (long)xFX) >> (precision)) +         //#FX2F rv.yFX = mCol1yFX * xFX + mCol2yFX * yFX;
                        ((mCol2yFX * (long)yFX) >> (precision)));         //#FX2F
        return rv;
    }

    /**
     * Multiplies the matrix with vector given as two coordinates (FX).
     * @fx
     * @param xFX x coordinate (FX).
     * @param yFX y coordinate (FX).
     * @param target the result vector.
     */
    public final void mult(int xFX, int yFX, FXVector target)
    {
        int precision = this.mPrecision;
        target.xFX = (int) (((mCol1xFX * (long)xFX) >> (precision)) +         //#FX2F target.xFX = mCol1xFX * xFX + mCol2xFX * yFX;
                        ((mCol2xFX * (long)yFX) >> (precision)));         //#FX2F
        target.yFX = (int) (((mCol1yFX * (long)xFX) >> (precision)) +         //#FX2F target.yFX = mCol1yFX * xFX + mCol2yFX * yFX;
                        ((mCol2yFX * (long)yFX) >> (precision)));         //#FX2F

    }

    /**
     * Multiplies the matrix with a vector (without object creation).
     * Uses the supplied target vector as result vector.
     * Note: Do not supply the same vector in both arguments! This will lead to wrong results!
     * @param vector the vector to multiply.
     * @param target the result vector.
     */
    public final void mult(FXVector vector, FXVector target)
    {
        int precision = this.mPrecision;
        target.xFX = (int) (((mCol1xFX * (long)vector.xFX) >> (precision)) +      //#FX2F target.xFX = mCol1xFX * vector.xFX + mCol2xFX * vector.yFX;
                            ((mCol2xFX * (long)vector.yFX) >> (precision)));      //#FX2F
        target.yFX = (int) (((mCol1yFX * (long)vector.xFX) >> (precision)) +      //#FX2F target.yFX = mCol1yFX * vector.xFX + mCol2yFX * vector.yFX;
                            ((mCol2yFX * (long)vector.yFX) >> (precision)));      //#FX2F

    }

    /**
     * Inverts the matrix.
     */
    public final void invert()
    {
        int precision = this.mPrecision;
        long determinanteMatFX = ((mCol1xFX * mCol2yFX - mCol1yFX * mCol2xFX) >> (precision));  //#FX2F float determinanteMatFX = (mCol1xFX * mCol2yFX - mCol1yFX * mCol2xFX);

        if (determinanteMatFX == 0)
        {
            mCol1xFX = 0;
            mCol1yFX = 0;
            mCol2xFX = 0;
            mCol2yFX = 0;
            return;
        }

        mCol1xFX ^= mCol2yFX;       //#FX2F float tmp = mCol2yFX;
        mCol2yFX ^= mCol1xFX;       //#FX2F mCol2yFX = mCol1xFX;
        mCol1xFX ^= mCol2yFX;       //#FX2F mCol1xFX = tmp;

        mCol1yFX = -mCol1yFX;
        mCol2xFX = -mCol2xFX;

        mCol1xFX = (((mCol1xFX) << (precision))/ determinanteMatFX);    //#FX2F mCol1xFX = mCol1xFX / determinanteMatFX;
        mCol1yFX = (((mCol1yFX) << (precision))/ determinanteMatFX);    //#FX2F mCol1yFX = mCol1yFX / determinanteMatFX;
        mCol2xFX = (((mCol2xFX) << (precision))/ determinanteMatFX);    //#FX2F mCol2xFX = mCol2xFX / determinanteMatFX;
        mCol2yFX = (((mCol2yFX) << (precision))/ determinanteMatFX);    //#FX2F mCol2yFX = mCol2yFX / determinanteMatFX;
    }

    /**
     * Calculates the 1 norm.
     * @fx
     * @return the 1 norm of the matrix.
     */
    public long normMatFX()
    {
        return Math.max(Math.abs(mCol1xFX) + Math.abs(mCol1yFX), Math.abs(mCol2xFX) + Math.abs(mCol2yFX));
    }


    /**
     * Prints the matrix to the console.
     * @param arg string to append
     */
    public void dumpToConsole(String arg)                                              //#NoJ2ME
    {                                                                                  //#NoJ2ME
        System.out.print("(" + ((float) (mCol1xFX) / (1 << (mPrecision))) + ", "      //#NoJ2ME
                             + ((float) (mCol1yFX) / (1 << (mPrecision))) + ")( "     //#NoJ2ME
                             + ((float) (mCol2xFX) / (1 << (mPrecision))) + ", "      //#NoJ2ME
                             + ((float) (mCol2yFX) / (1 << (mPrecision)))             //#NoJ2ME
                             + ")" + arg);                                             //#NoJ2ME
    }                                                                                  //#NoJ2ME

}
