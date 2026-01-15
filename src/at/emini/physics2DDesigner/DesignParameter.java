package at.emini.physics2DDesigner;

import java.awt.Color;

import at.emini.physics2D.util.FXVector;

public class DesignParameter implements DesignSelectionObject
{
    private DesignWorld world;

    public DesignParameter(DesignWorld world)
    {
        this.world = world;
    }

    public void addListener(DesignObjectChangeListener listener)
    {
    }

    public void drawInteractives(GraphicsWrapper g, Color color, Color c2)
    {
    }

    public void drawObject(GraphicsWrapper g, Color c, Color c2, boolean drawFull)
    {
    }

    public int getAction(GraphicsWrapper g, FXVector mousePos)
    {
        return -1;
    }

    public Color getColor()
    {
        return Color.lightGray;
    }

    public void setColor(Color c)
    {
    }

    public boolean hasAction()
    {
        return false;
    }

    public FXVector makeMove(FXVector newPos)
    {
        return null;
    }

    public void notifyListeners()
    {
    }

    public void removeListener(DesignObjectChangeListener listener)
    {
    }

    public int setAction(GraphicsWrapper g, FXVector mousePos, FXVector gridPos)
    {
        return 0;
    }

    public void setCoAction(int action, GraphicsWrapper g, FXVector startPos,
            FXVector gridPos)
    {
    }

    public boolean unsetAction()
    {
        return false;
    }

    public String toString()
    {
        return "Parameters";
    }

    public DesignWorld getWorld()
    {
        return world;
    }
}
