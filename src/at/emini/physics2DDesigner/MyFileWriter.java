package at.emini.physics2DDesigner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;


public class MyFileWriter extends FileOutputStream 
{

    public MyFileWriter(File file) throws IOException 
    {
        super(file);
    }

    private static final int decimal = FXUtil.DECIMAL;  //trick to avoid automated conversion 
    //method for world saving compatibility in float mode
    public void writeInt (float value) throws IOException
    {
        int val = 0; //#FX2F int val = (int) (value * (1 << decimal)); 
        int b1 =  (val >> 24);
        int b2 =  (val >> 16);
        int b3 =  (val >> 8);
        int b4 =  (val >> 0);
        super.write( b1 );
        super.write( b2 );
        super.write( b3 );
        super.write( b4 );
    } 
    
    public void writeInt (int value) throws IOException
    {
        //int v1 = value >> 16;
        //int v2 = value & (( 1 << 16 ) - 1);
        int b1 =  (value >> 24);
        int b2 =  (value >> 16);
        int b3 =  (value >> 8);
        int b4 =  (value >> 0);
        super.write( b1 );
        super.write( b2 );
        super.write( b3 );
        super.write( b4 );
    } 
    
    public void writeFX (FXVector vector) throws IOException
    {
        writeInt(vector.xFX);
        writeInt(vector.yFX);
    } 
}
