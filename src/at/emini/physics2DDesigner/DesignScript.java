package at.emini.physics2DDesigner;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import at.emini.physics2D.Script;
import at.emini.physics2D.util.PhysicsFileReader;

public class DesignScript extends Script
{
    protected boolean visible = true;

    protected static int scriptIndex = 1;

    protected int index;

    protected Color c;

    private static int currColor = 0;
    private static final Color defaultColors[] = {
    new Color(180,   0,   0, 50),
    new Color(180, 180,   0, 50),
    new Color(  0, 180,   0, 50),
    new Color(  0, 180, 180, 50),
    new Color(  0,   0, 180, 50),
    new Color(180,   0, 180, 50) };

    public DesignScript(boolean restart) {
        super(restart);
        index = scriptIndex++;
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    public DesignScript(Script script) {
        super(script);

        index = scriptIndex++;
        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    public static void resetIndex()
    {
        scriptIndex = 1;
    }

    public Color getColor()
    {
        return c;
    }

    public Color getOpaqueColor()
    {
        return DesignerUtilities.getGrayBlendColor(c);
    }

    public void saveToFile(File file)
    {
        try
        {
            MyFileWriter fileWriter = new MyFileWriter( file );
            saveToFile(fileWriter);
            fileWriter.close();
        }
        catch( IOException e)
        {
            System.out.print("Error while writing file!\n");
        }
    }

    public void saveToFile(MyFileWriter fileWriter)
    {
        try
        {
            fileWriter.write( mRestart ? 1 : 0 );
            fileWriter.write( mElements.size() );

            for( int i = 0; i < mElements.size(); i++)
            {
                ScriptElement element = ((ScriptElement) mElements.elementAt(i));
                fileWriter.write( element.mType );
                fileWriter.writeInt( element.mTargetAFX );
                fileWriter.writeInt( element.mTargetBFX );
                fileWriter.write( element.mTimeSteps );
            }

        }
        catch( IOException e)
        {
            System.out.print("Error while writing file!\n");
        }
    }

    public static DesignScript loadDesignScript(PhysicsFileReader reader, Vector bodies)
    {
        return new DesignScript(Script.loadScript(reader));
    }

    public int getElementCount()
    {
        return mElements.size();
    }

    public ScriptElement getElement(int i)
    {
        return (ScriptElement) mElements.get(i);
    }

    public String getName()
    {
        return "Script " + index;
    }

    public boolean isRestart() {
        return mRestart;
    }

    public void setRestart(boolean selected) {
        mRestart = selected;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public boolean isVisible()
    {
       return visible;
    }

    public String toString()
    {
        return "Script ";
    }
}
