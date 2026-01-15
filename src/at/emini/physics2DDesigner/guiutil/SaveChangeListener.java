package at.emini.physics2DDesigner.guiutil;

import java.io.File;

public interface SaveChangeListener
{
    public void saveStateChanged(boolean saved, File file);
}
