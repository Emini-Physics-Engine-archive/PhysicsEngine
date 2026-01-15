package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import at.emini.physics2D.Body;
import at.emini.physics2D.PhysicsEventListener;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class SimulatorThread extends Thread implements WorldChangeListener
{
    private DesignWorld origWorld;

    private DesignWorld[] runWorld;
    private int worldCount = 1;

    private boolean stop = true;
    private Component displayComponent;

    private int stepCount = 0;
    private int millis = 0;

    private double avgTime = 0.0;

    private PhysicsEventListener listener;

    private GraphicsWrapper currentGraphics;

    private SimulationBodyInfoPanel infoPanel;

    //to apply force to body
    private DesignBody interactionBody = null;
    private FXVector interactionAnchorPos = new FXVector();
    private FXVector interactionPos = new FXVector();
    private FXVector interactionTargetPos = new FXVector();

    public SimulatorThread(DesignWorld world, Component c, PhysicsEventListener listener, int worldCount)
    {
        this.worldCount = worldCount;
        this.listener = listener;
        setWorld(world);
        this.displayComponent = c;

        millis = (world.getTimestepFX() * 1000) >> FXUtil.DECIMAL;  //#FX2F millis = (int) (world.getTimestepFX() * 1000);
    }

    public void setWorld(DesignWorld world)
    {
        this.origWorld = world;
        this.runWorld = new DesignWorld[worldCount];
        for( int i = 0; i < worldCount; i++)
        {
            runWorld[i] = new DesignWorld((origWorld));
            runWorld[i].setPhysicsEventListener(listener);
            runWorld[i].registerListener(this);
            if (i > 0)
            {
                runWorld[i].jitter();
            }
        }
    }

    public void registerInfoPanel(SimulationBodyInfoPanel infoPanel)
    {
        this.infoPanel = infoPanel;
    }

    public DesignWorld getWorld(int index)
    {
        return runWorld[index];
    }

    public void setDrawParameter(boolean drawContacts,
            boolean drawBodyTrajectory,
            boolean drawVertexTrajectory,
            boolean drawDesignInfo,
            boolean drawParticleTrajectory,
            boolean drawAABB)
    {
        for( int i = 0; i < worldCount; i++)
        {
            runWorld[i].setDrawContacts(drawContacts);
            runWorld[i].setDrawBodyTrajectory(drawBodyTrajectory);
            runWorld[i].setDrawVertexTrajectories(drawVertexTrajectory);
            runWorld[i].setDrawDesignInfo(drawDesignInfo);
            runWorld[i].setDrawParticleTrajectory(drawParticleTrajectory);
            runWorld[i].setDrawAABB(drawAABB);
        }
    }

    public void run()
    {
        long start = 0, startnano = 0, diff = 0;
        while(true)
        {
            while(stop)
            {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            start = System.currentTimeMillis();

            diff = tick();

            //paint time
            if (displayComponent!= null && displayComponent.getGraphics() != null)
            {
                Graphics g = displayComponent.getGraphics();
                g.setColor(Color.blue);
                avgTime = avgTime * 0.8 + (double) diff * 0.2;
                long displaytime = Math.round(avgTime / 1000);
                g.drawString("ms/f: " + String.valueOf(displaytime), 20, 50);
                g.drawString("step: " + String.valueOf(stepCount), 20, 70);
                //System.out.println("--> " + String.valueOf(runWorld[0].getContactCount()) );
                //System.out.println("--- " + String.valueOf(stepCount) + " ---");
            }

            while( System.currentTimeMillis() < start + millis )
            {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public long tick()
    {
        long startnano = System.nanoTime();
        stepCount++;

        for( int i = 0; i < worldCount; i++)
        {
            if (runWorld[i] != null)
            {
                runWorld[i].tick();
            }
        }
        //world.collisionUpdate();
        long diff = System.nanoTime() - startnano;
        if (displayComponent!= null && displayComponent.getGraphics() != null)
        {
            displayComponent.paint(displayComponent.getGraphics());
        }

        //apply interaction
        if (interactionBody != null)
        {
            FXMatrix rot = new FXMatrix( interactionBody.getRotationMatrix() );
            interactionPos = rot.mult(interactionAnchorPos);

            int forceFX = interactionBody.shape().getMassFX() * 2 / 16;

            FXVector impulse = new FXVector(interactionPos);
            impulse.add(interactionBody.positionFX());
            impulse.subtract(interactionTargetPos);

            impulse.multFX(-forceFX);

            interactionBody.applyMomentumAt(impulse, interactionPos);
        }

        if ( infoPanel != null)
        {
            infoPanel.update();
        }

        return diff;
    }

    public void draw(GraphicsWrapper g)
    {
        for( int i = 0; i< worldCount; i++)
        {
            if (runWorld[i] != null)
            {
                runWorld[i].draw(g, true);
            }
        }

        //draw interactive
        if (interactionBody != null)
        {
            g.setColor(Color.red);

            FXMatrix rot = new FXMatrix( interactionBody.getRotationMatrix() );
            interactionPos = rot.mult(interactionAnchorPos);

            g.drawLine(interactionPos.xFX + interactionBody.positionFX().xFX,
                       interactionPos.yFX + interactionBody.positionFX().yFX,
                       interactionTargetPos.xFX,
                       interactionTargetPos.yFX);

        }

        currentGraphics = g;
    }

    public void setInteractionBodyAt(FXVector pos)
    {
        interactionBody =  getWorld(0).checkBody( currentGraphics, pos, pos, null, null );
        if (interactionBody != null)
        {
            FXMatrix rot = new FXMatrix( interactionBody.getRotationMatrix() );
            rot.invert();
            interactionAnchorPos.assignDiff(pos, interactionBody.positionFX());
            interactionAnchorPos = rot.mult(interactionAnchorPos);
        }
        infoPanel.selectBody(interactionBody, getWorld(0));

        interactionTargetPos.assign(pos);
    }

    public void setInteractionPos(FXVector pos)
    {
        interactionTargetPos.assign(pos);
    }

    public void clearInteractionBody()
    {
        interactionBody = null;
    }

    public boolean hasInteraction()
    {
        return interactionBody != null;
    }

    public int getStepCount()
    {
        return stepCount;
    }

    public void end()
    {
        stop = true;
    }

    public void restartSimulation()
    {
        stepCount = 0;
        setWorld(origWorld);
    }

    public void restart()
    {
        stop = false;
    }

    public boolean isStopped()
    {
        return stop;
    }

    public void updateRequired()
    {
        if (displayComponent.getGraphics() != null)
        {
            displayComponent.repaint();
        }
    }

    public void worldChanged(DesignWorld world)
    {
        if (displayComponent.getGraphics() != null)
        {
            displayComponent.repaint();
        }
    }

}
