package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;

public class ShapeViewer extends MovePanel implements WorldChangeListener
{

    private static final long serialVersionUID = 8514341254366144824L;

    DesignShape shape;
    DesignWorld world;

    boolean small = false;

    public ShapeViewer(DesignShape shape, DesignWorld world, boolean small, boolean interactive)
    {
        super(false, interactive);

        this.shape = shape;
        this.world = world;

        this.small = small;

        world.registerListener(this);
    }


    public void setShape(DesignShape shape)
    {
        this.shape = shape;
        refresh();
    }

    private void calcScale()
    {
        double scale = 1.0;
        double offset = 4.0;

        scale = (getHeight()) / (Math.sqrt( shape.getBoundingRadiusSquare())) * 0.45;
        setScale(scale);
        repaint();
    }

    public void clearBackground(GraphicsWrapper g)
    {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void paintCanvas(GraphicsWrapper g)
    {
        if (g != null)
        {
            if (shape != null)
            {
                shape.draw(g, false);
            }
        }
    }

    public Dimension getPreferredSize()
    {
        return small ? new Dimension(24, 24) : new Dimension(100, 150);
    }


    public void worldChanged(DesignWorld w)
    {
        //calcScale();
    }

    public void updateRequired()
    {
        calcScale();
    }

    public void componentResized(ComponentEvent e)
    {
        super.componentResized(e);
        calcScale();
    }

}
