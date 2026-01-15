package at.emini.physics2DDesigner;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class PhyFileFilter extends FileFilter
{

    public boolean accept(File f)
    {        
        String name = f.getName().toLowerCase();        
        return name.endsWith(".phy");
    }

    public String getDescription()
    {
        return "Emini Physics files (.phy)";
    }

}
