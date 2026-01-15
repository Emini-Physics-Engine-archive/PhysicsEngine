package at.emini.physics2DDesigner;

import java.awt.Color;

import at.emini.physics2D.util.FXVector;

public interface DesignSelectionObject
{
    public Color getColor();
    public void setColor(Color c);

    public void drawObject(GraphicsWrapper g, Color c, Color c2, boolean drawFull);
    public void drawInteractives(GraphicsWrapper g, Color color, Color c2);

    public int getAction(GraphicsWrapper g, FXVector mousePos);
    public int setAction(GraphicsWrapper g, FXVector mousePos, FXVector gridPos);
    public void setCoAction(int action, GraphicsWrapper g, FXVector startPos, FXVector gridPos);
    public boolean hasAction();
    public boolean unsetAction();

    public FXVector makeMove(FXVector newPos);  //return the diff to the last move


    public void addListener(DesignObjectChangeListener listener);
    public void removeListener(DesignObjectChangeListener listener);
    public void notifyListeners();
}
