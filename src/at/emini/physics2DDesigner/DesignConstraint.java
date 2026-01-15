package at.emini.physics2DDesigner;

import java.util.Vector;

import at.emini.physics2D.Constraint;
import at.emini.physics2D.util.FXVector;

public interface DesignConstraint extends DesignSelectionObject, Constraint
{

    public void saveToFile(MyFileWriter fileWriter, Vector bodies);

    public boolean isPointInConstraint(GraphicsWrapper g, FXVector mousePos);
    public boolean isConstraintInSelection(GraphicsWrapper g, FXVector p1, FXVector p2);

    public void scale(float scale);
}
