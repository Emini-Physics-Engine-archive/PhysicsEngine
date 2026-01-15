package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import at.emini.physics2D.MultiShape;

public class ShapeSelectViewer extends ShapeViewer implements MouseListener
{

    private static final long serialVersionUID = 1L;

    private boolean selected = false;
    private MultiShape multiShape;

    public ShapeSelectViewer(MultiShape multiShape, DesignShape shape, DesignWorld world)
    {
        super(shape, world, true, false);

        this.multiShape = multiShape;

        canvas.addMouseListener(this);

    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public void clearBackground(GraphicsWrapper g)
    {
        g.setColor( selected ? new Color(180, 255, 180) : Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
    };

    @Override
    public void mouseClicked(MouseEvent e)
    {
        selected = !selected;

        if (selected)
        {
            ((DesignMultiShape) multiShape).addShape(shape);
        }
        else
        {
            if (! ((DesignMultiShape) multiShape).removeShape(shape) )
            {
                selected = true;
            }
        }

        world.updateAndRepaint();

        refresh();
    }
}
