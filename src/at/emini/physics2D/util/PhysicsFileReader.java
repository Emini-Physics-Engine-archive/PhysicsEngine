package at.emini.physics2D.util;

import java.io.File;                    //#NoJ2ME
import java.io.FileInputStream;         //#NoJ2ME
import java.io.FileNotFoundException;   //#NoJ2ME
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for an InputStream.
 * Features convenience methods to convert read bits.
 *
 * @author Alexander Adensamer
 */
public class PhysicsFileReader
{
    /**
     * File version.
     */
    private int mVersion = 0;

    /**
     * The stream.
     */
    private InputStream mStream;

    /**
     * Constructor.
     * Uses the J2ME resource loading mechanism.
     * @param name the name of the file resource.
     */
    public PhysicsFileReader( String name )
    {
        mStream = getClass().getResourceAsStream( name );
        readHeader();
    }

    /**
     * Constructor using a File.
     * @param file the file containing the data stream.
     */
    public PhysicsFileReader( File file )               //#NoJ2ME
    {                                                   //#NoJ2ME
        try {                                           //#NoJ2ME
            mStream = new FileInputStream(file);         //#NoJ2ME
            readHeader();                               //#NoJ2ME
        } catch (FileNotFoundException e) {             //#NoJ2ME
            e.printStackTrace();                        //#NoJ2ME
        }                                               //#NoJ2ME
    }                                                   //#NoJ2ME

    /**
     * COnstructor using an input stream.
     * @param stream the data stream.
     */
    public PhysicsFileReader( InputStream stream )
    {
        this.mStream = stream;
        readHeader();
    }

    /**
     * Reads the header of the passed file/stream.
     */
    private void readHeader()
    {
        mVersion = nextInt();
    }

    /**
     * Gets the version of the data stream.
     * @return the version of the data stream.
     */
    public int getVersion()
    {
        return mVersion;
    }

    /**
     * Reads the next byte.
     * @return the next byte in the stream.
     */
    public int next()
    {
        if (mStream != null)
        {
            try
            {
                return mStream.read();
            }
            catch (IOException e)
            {
            }
        }
        return -1;
    }


    /**
     * Reads the next string.
     * The first 4 bytes indicate the length.
     * The following bytes indicate the string characters encoded in UTF-8.
     * @return the next bytes read as string.
     */
    public String nextString()
    {
        if (mStream != null)
        {
            try
            {
                int length = nextInt();

                byte[] bytes = new byte[length];
                mStream.read(bytes, 0, length);
                return new String(bytes, "UTF-8");
            }
            catch (IOException e)
            {
            }
        }
        return null;
    }

    /**
     * Reads the next int (4 byte).
     * @return the next int of the stream (4 bytes).
     */
    public int nextInt()
    {
        if (mStream != null)
        {
            try
            {
                int bits1 = mStream.read();
                int bits2 = mStream.read();
                int bits3 = mStream.read();
                int bits4 = mStream.read();
                return (bits1 << 24) + (bits2 << 16) + (bits3 << 8) + bits4;
            }
            catch (IOException e)
            {
            }
        }
        return -1;
    }

    //#FX2F private static final int decimal = FXUtil.DECIMAL;
    //#FX2F private static final int koeff = 1 << decimal;      //trick to avoid preprocessing

    /**
     * Read the next int (4 byte, FX).
     * The method is required for the float conversion.
     * @fx
     * @return the next int of the stream (4 bytes)
     */
    public int nextIntFX()
    {
        if (mStream != null)
        {
            try
            {
                int bits1 = mStream.read();
                int bits2 = mStream.read();
                int bits3 = mStream.read();
                int bits4 = mStream.read();
                return (bits1 << 24) + (bits2 << 16) + (bits3 << 8) + bits4; //#FX2F return ((float) ((bits1 << 24) + (bits2 << 16) + (bits3 << 8) + bits4)) / (float) koeff;
            }
            catch (IOException e)
            {
            }
        }
        return -1;
    }

    //#FX2F private static final int decimal2 = FXUtil.DECIMAL2;
    //#FX2F private static final int koeff2 = 1 << decimal2;      //trick to avoid preprocessing

    /**
     * Read the next int (4 byte, 2FX).
     * The method is required for the float conversion to distinguish values stored in fx and 2fx
     * @fx
     * @return the next int of the stream (4 bytes)
     */
    public int nextInt2FX()
    {
        if (mStream != null)
        {
            try
            {
                int bits1 = mStream.read();
                int bits2 = mStream.read();
                int bits3 = mStream.read();
                int bits4 = mStream.read();
                return (bits1 << 24) + (bits2 << 16) + (bits3 << 8) + bits4; //#FX2F return ((float) ((bits1 << 24) + (bits2 << 16) + (bits3 << 8) + bits4)) / (float) koeff2;
            }
            catch (IOException e)
            {
            }
        }
        return -1;
    }

    /**
     * Reads the next vector : 4 + 4 bytes.
     * @return the next 2d vector (4 + 4 bytes) in the stream.
     */
    public FXVector nextVector()
    {
        if (mStream != null)
        {
            return new FXVector( nextIntFX(), nextIntFX());
        }
        return new FXVector();
    }

    /**
     * Closes the stream.
     */
    public void close()
    {
        try {
            mStream.close();
        } catch (IOException e) {
        }
    }

}
