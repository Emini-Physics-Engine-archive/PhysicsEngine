package at.emini.physics2DVisualTest;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import at.emini.physics2D.World;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class TestSimThread extends Thread {

    private GraphicsWorld world;
    private boolean stop = true;
    private Component c;
    
    private int stepCount = 0;
    private int millis = 0;
    
    private int maxticks = -1;
    
    private double avgTime = 0.0;
    
    public TestSimThread(GraphicsWorld world, Component c)
    {
        this.world = world;
        this.c = c;
        
        millis = (world.getTimestepFX() * 1000) >> FXUtil.DECIMAL;
    }
       
    public void setMaxTicks(int ticks)
    {
        maxticks = ticks;
    }
    
    public void run(){
        long start = 0, startnano = 0, diff = 0;
        while(maxticks < 0 || maxticks > stepCount)
        {    
            while(stop)
            {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            
            start = System.currentTimeMillis();
            startnano = System.nanoTime();
            stepCount++;
            world.tick();
            
            /*if (stepCount == 100)
            {
                world.translate(FXVector.newVector(0, -100));
            }*/
            
            world.executeManualMoves();
            //world.collisionUpdate();
            diff = System.nanoTime() - startnano;
            c.paint(c.getGraphics());
                        
            //paint time
            Graphics g = c.getGraphics();
            g.setColor(Color.blue);
            avgTime = avgTime * 0.8 + (double) diff * 0.2;
            long displaytime = Math.round(avgTime / 1000);
            g.drawString("ms/f: " + String.valueOf(displaytime), 20,50);
            
            while( System.currentTimeMillis() < start + millis ) 
            {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }            
        }
    }
    
    public void tick()
    {
        stepCount++;
        world.tick();
        //world.collisionUpdate();
        world.executeManualMoves();
        world.draw(c.getGraphics());
        
        if (stepCount == 100)
        {
            world.translate(FXVector.newVector(0, -100));
        }
    }
    
    public int getStepCount()
    {
        return stepCount;
    }
    
    public void end()
    {
        stop = true;
    }
    
    public void restart()
    {
        stop = false;
    }

    public boolean isStopped()
    {
        return stop;
    }
}
