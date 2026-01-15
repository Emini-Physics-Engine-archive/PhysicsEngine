package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

public class ShapeDrawCanvas extends JPanel {

    private static final long serialVersionUID = -8622191484973373803L;

    protected DesignShapeStd shape;
    private boolean isSelected = false;

    public ShapeDrawCanvas(DesignShapeStd shape)
    {
        this.shape = shape;
    }

    public void paint(GraphicsWrapper g)
    {
        g.setColor(isSelected ? Color.orange : Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (shape != null)
        {
            double scale = Math.min(getWidth(), getHeight()) / ((double)shape.getBoundingRadiusFX() * 2);
            AffineTransform scaleTransform = AffineTransform.getScaleInstance(scale, scale);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.translate(getWidth()/2, getHeight()/2);
            g.transform(scaleTransform);

            shape.draw(g, false);
        }
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public DesignShape getShape() {
        return shape;
    }

    public void setShape(DesignShapeStd shape) {
        this.shape = shape;
    }
}
