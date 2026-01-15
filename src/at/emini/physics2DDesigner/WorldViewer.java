package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;

import at.emini.physics2D.Body;
import at.emini.physics2D.util.FXUtil;

public class WorldViewer extends MovePanel implements WorldChangeListener
{
    private static final long serialVersionUID = -6061848796822674139L;

    private DesignWorld world;

    private GraphicsWrapper currentGraphics;

    public WorldViewer(DesignWorld world)
    {
        super(false, true);
        setWorld(world);

        initComponents();
    }

    private void initComponents()
    {
        validate();
    }

    public void setWorld(DesignWorld world)
    {
        this.world = world;
        world.registerListener(this);
        refresh();
    }

    public DesignWorld getWorld()
    {
        return world;
    }

    public void scaleTo(int width, int height)
    {
        int bodyCount = world.getBodyCount();
        Body[] bodies = world.getBodies();

        int minxFX = Integer.MAX_VALUE;
        int minyFX = Integer.MAX_VALUE;
        int maxxFX = Integer.MIN_VALUE;
        int maxyFX = Integer.MIN_VALUE;

        for( int i = 0; i < bodyCount; i++)
        {
            minxFX = bodies[i].positionFX().xFX < minxFX ? bodies[i].positionFX().xFX : minxFX;
            minyFX = bodies[i].positionFX().yFX < minyFX ? bodies[i].positionFX().yFX : minyFX;
            maxxFX = bodies[i].positionFX().xFX > maxxFX ? bodies[i].positionFX().xFX : maxxFX;
            maxyFX = bodies[i].positionFX().yFX > maxyFX ? bodies[i].positionFX().yFX : maxyFX;
        }

        double diffX = (double) (maxxFX - minxFX) / (double) (FXUtil.ONE_FX);
        double diffY = (double) (maxyFX - minyFX) / (double) (FXUtil.ONE_FX);
        double centerX = ((double) (maxxFX + minxFX) / (double) (FXUtil.ONE_FX)) / 2.0;
        double centerY = ((double) (maxyFX + minyFX) / (double) (FXUtil.ONE_FX)) / 2.0;

        double scale = Math.min(width/diffX, height/diffY) * 0.75;

        translate( - centerX,
                   - centerY);
        scale(scale);

    }



    public void clearBackground(GraphicsWrapper g)
    {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void paintCanvas(GraphicsWrapper g)
    {
        world.draw(g, false);

        //highlight script objects
        for( int i = 0; i < world.getDesignScriptBodyCount(); i++)
        {
            DesignScript s = (DesignScript) world.getScriptBodyScript(i);
            if( s.isVisible())
            {
                DesignBody b = (DesignBody) world.getScriptBody(i);
                b.drawObject(g, s.getColor(), null, true);
            }
        }

        currentGraphics = g;
    }


    public void componentShown(ComponentEvent e)
    {
        world.recalcBodyVertices();
        super.componentShown(e);

        repaint();
    }


    public void updateRequired()
    {
        if (getGraphics() != null)
        {
            refresh();
        }
    }

    public void worldChanged(DesignWorld w)
    {
        invalidate();
    }

}
