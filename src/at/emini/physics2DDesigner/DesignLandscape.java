package at.emini.physics2DDesigner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import at.emini.physics2D.Landscape;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;

public class DesignLandscape extends Landscape implements DesignSelectionObject 
{
    private final int interactiveDistance = 4;

    private Vector selectedPoints = new Vector();
    private FXVector lastPosition = new FXVector();
    private boolean actionperformed = false;
    
    private static int wallDepth = 5;
    
    private static BufferedImage switchIcon = null;
    
    private static int width = 10;
    private static int height = 10;
    private static int widthFX = width * FXUtil.ONE_FX;
    private static int heightFX = height * FXUtil.ONE_FX;
    
    
    public DesignLandscape()
    {
        super();
        initIcon();
    }

    public DesignLandscape(Landscape landscape)
    {
        super(landscape);
    }

    public Landscape copy()
    {
        DesignLandscape copy = new DesignLandscape(this);        

        return copy;
    }
    
    public Color getColor()
    {
        return Color.gray;
    }
    
    public void setColor(Color c)
    {
    }

    private void initIcon()
    {
        if ( switchIcon == null)
        {
            try 
            {            
                switchIcon = ImageIO.read( getClass().getResourceAsStream("/res/icon_rotate_sm.png") );
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    protected void consolidateVectors()
    {
        for( int i = 0; i < mSegmentCount; i++)
        {
            if (mStartpoints[i].xFX > mEndpoints[i].xFX)
            {
                FXVector tmp = mStartpoints[i];
                mStartpoints[i] = mEndpoints[i];
                mEndpoints[i] = tmp;

                if ( mFaces[i] == FACE_RIGHT)
                {
                    mFaces[i] = FACE_LEFT;
                }
                else if ( mFaces[i] == FACE_LEFT)
                {
                    mFaces[i] = FACE_RIGHT;
                }
            }
        }
        sortArrays();
    }

    public void drawObject(GraphicsWrapper g, Color c, Color c2, boolean drawFull) 
    {
        g.setColor(c);
        
        float zoomScale = (float) g.getZoomScale();
        
        BasicStroke stroke = new BasicStroke((int) (1.0f / zoomScale) );
        g.setStroke(stroke);
        
        for( int i = 0; i < mSegmentCount; i++)
        {
            //create wall effect
            if (mFaces[i] != Landscape.FACE_NONE)
            {			  
                g.setPaintMode();
                //g.setXORMode(Color.white);

                FXVector dir = new FXVector(mEndpoints[i]);
                dir.subtract(mStartpoints[i]);
                dir.normalize();
                dir.turnRight();
                dir.multFX((int) (wallDepth / zoomScale));

                if (mFaces[i] != Landscape.FACE_RIGHT)
                {
                    dir.mult(-1);
                }
                
                g.setPaint( new GradientPaint(mStartpoints[i].xFX - dir.xFX, mStartpoints[i].yFX - dir.yFX, Color.black,
                        mStartpoints[i].xFX + dir.xFX, mStartpoints[i].yFX + dir.yFX, Color.white) );
                FXVector[] positions = new FXVector[4]; 
                
                positions[0] = mStartpoints[i];
                positions[1] = mEndpoints[i];
                positions[2] = new FXVector(mEndpoints[i]);
                positions[2].add(dir);
                positions[3] = new FXVector(mStartpoints[i]);
                positions[3].add(dir);
                Polygon p = GraphicsWrapper.createPolygon(positions, 0, 4);                 

                g.fillPolygon(p);
                g.setPaint(c);
            } 
                
            g.setXORMode(Color.white);
            
            g.drawLine(mStartpoints[i].xFX, mStartpoints[i].yFX, 
                    mEndpoints[i].xFX, mEndpoints[i].yFX);
        }
        g.setStroke(new BasicStroke());
        g.setPaintMode();
        
    }

    public void drawInteractives(GraphicsWrapper g, Color color, Color c2)
    {
        double zoomScale = g.getZoomScale();
        
        g.setColor(Color.black);
        for( int i = 0; i < mSegmentCount; i++)
        {
            g.drawArc((int) (mStartpoints[i].xFX - interactiveDistance / zoomScale), (int) (mStartpoints[i].yFX - interactiveDistance / zoomScale), 
                    (int) (interactiveDistance * 2 / zoomScale), 
                    (int) (interactiveDistance * 2 / zoomScale), 0, 360);
            g.drawArc((int) (mEndpoints[i].xFX - interactiveDistance / zoomScale), (int) (mEndpoints[i].yFX - interactiveDistance / zoomScale), 
                    (int) (interactiveDistance * 2 / zoomScale), 
                    (int) (interactiveDistance * 2 / zoomScale), 0, 360);

            //draw icon
            g.drawImage(switchIcon, (mStartpoints[i].xFX + mEndpoints[i].xFX) / 2, (mStartpoints[i].yFX + mEndpoints[i].yFX) / 2,
                    (int) (width / zoomScale), (int) (height / zoomScale), 0, null);
        }

    }

    public int getAction(GraphicsWrapper g, FXVector mousepos) 
    {
        FXVector ul = new FXVector();
        FXVector br = new FXVector();
        
        double zoomScale = g.getZoomScale();
        
        //check all positions
        for( int i = 0; i < mSegmentCount; i++)
        {   
            ul.assignFX((mStartpoints[i].xFX + mEndpoints[i].xFX) / 2, (mStartpoints[i].yFX + mEndpoints[i].yFX) / 2);
            br.assignFX(ul.xFX + widthFX, ul.yFX + heightFX);
            if (mousepos.isInRect(ul, br))
            {
                return Designer.ACTION_FACE_SWITCH;
            }
            
            if (mStartpoints[i].distFX(mousepos) < interactiveDistance / zoomScale)
            {
                return 	Designer.ACTION_MOVE;
            }
            if (mEndpoints[i].distFX(mousepos) < interactiveDistance / zoomScale)
            {
                return 	Designer.ACTION_MOVE;
            }
        }
        return Designer.ACTION_NONE;
    }
    
    public int getAction(GraphicsWrapper g, FXVector p1, FXVector p2) 
    {
        //check all positions
        for( int i = 0; i < mSegmentCount; i++)
        {
            if (mStartpoints[i].isInRect(p1, p2))
            {
                return  Designer.ACTION_MOVE;
            }
            if (mEndpoints[i].isInRect(p1, p2))
            {
                return  Designer.ACTION_MOVE;
            }
            
        }
        return -1;
    }

    public void switchFace(FXVector mousepos)
    {
        FXVector ul = new FXVector();
        FXVector br = new FXVector();
        
        //check all positions
        for( int i = 0; i < mSegmentCount; i++)
        {   
            ul.assignFX((mStartpoints[i].xFX + mEndpoints[i].xFX) / 2, (mStartpoints[i].yFX + mEndpoints[i].yFX) / 2);
            br.assignFX(ul.xFX + widthFX, ul.yFX + heightFX);
            if (mousepos.isInRect(ul, br))
            {
                mFaces[i] = (short)((mFaces[i] + 1) % 3);
                break;
            }
        }
    }
    
    
    public FXVector makeMove(FXVector newPos) 
    {
        FXVector diff = new FXVector(newPos);
        diff.subtract(lastPosition);            
        for( int i = 0; i < selectedPoints.size(); i++)
        {
            ((FXVector) selectedPoints.elementAt(i)).add(diff);
        }
        consolidateVectors();

        lastPosition = newPos;
        actionperformed = true;
        return diff;
    }

    public int setAction(GraphicsWrapper g, FXVector mousepos, FXVector gridPos) 
    {
        unsetAction();
        double zoomScale = g.getZoomScale();
        
        //check all positions
        for( int i = 0; i < mSegmentCount; i++)
        {
            /*
            //check if face switch was pressed
            ul.assignFX((startpoints[i].xFX + endpoints[i].xFX) / 2, (startpoints[i].yFX + endpoints[i].yFX) / 2 + offsetFX);
            br.assignFX(ul.xFX + widthFX, ul.yFX + heightFX);
            if (mousepos.isInRect(ul, br))
            {
                faces[i] = (short) ((faces[i] + 1) % 3); 
                System.out.println(faces[i]);
                return Designer.ACTION_NONE;
            }*/
            
            if (mStartpoints[i].distFX(mousepos) < interactiveDistance / zoomScale)
            {
                selectedPoints.addElement(mStartpoints[i]);
                continue;
            }
            if (mEndpoints[i].distFX(mousepos) < interactiveDistance / zoomScale)
            {
                selectedPoints.addElement(mEndpoints[i]);
            }
        }
        for( int i = 0; i < selectedPoints.size(); i++)
        {
            ((FXVector) selectedPoints.elementAt(i)).assign(gridPos);
        }
        lastPosition = gridPos;
        return selectedPoints.size() > 0 ? Designer.ACTION_MOVE : Designer.ACTION_NONE;
    }

    
    public void setCoAction(int action, GraphicsWrapper g, FXVector startPos, FXVector gridPos)
    {
        if (action != Designer.ACTION_MOVE || hasAction())
        {
            return;
        }
    }
    
    public void selectLastElement(FXVector mousepos, FXVector gridPos, FXVector endPoint)
    {
        unsetAction();
        //check all positions
        selectedPoints.addElement(endPoint);
        lastPosition = gridPos;
    }

    public boolean unsetAction() 
    {
        boolean returnval = hasAction() && actionperformed;
        selectedPoints.clear();
        actionperformed = false;
        return returnval;
    }

    public boolean hasAction() 
    {
        return selectedPoints.size() > 0;
    }

    public void removeAt(GraphicsWrapper g, FXVector mousepos) 
    {
        double zoomScale = g.getZoomScale();        
        for( int i = 0; i < mSegmentCount; i++)
        {
            if (   mStartpoints[i].distFX(mousepos) < interactiveDistance / zoomScale
                    || mEndpoints[i].distFX(mousepos) < interactiveDistance / zoomScale )
            {
                mSegmentCount--;
                if (i < mSegmentCount)
                {
                    mStartpoints[i] = mStartpoints[mSegmentCount];
                    mEndpoints[i] = mEndpoints[mSegmentCount];
                    mFaces[i] = mFaces[mSegmentCount];
                    i--;
                }
                mStartpoints[mSegmentCount] = null;
                mEndpoints[mSegmentCount] = null;
                mFaces[mSegmentCount] = 0;
            }
        }

        consolidateVectors();		
    }


    public void saveToFile(MyFileWriter fileWriter, Vector shapes)
    {
        try 
        {   
            fileWriter.writeInt( mSegmentCount );

            for( int i = 0; i < mSegmentCount; i++)
            {
                fileWriter.writeFX( mStartpoints[i] );
                fileWriter.writeFX( mEndpoints[i] );
                fileWriter.write( mFaces[i] );
            }
            new DesignShapeStd(getShape()).saveToFile(fileWriter, shapes);

        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }

    }

    public static DesignLandscape loadFromFile(File file, Vector shapes) 
    {
        PhysicsFileReader reader = new PhysicsFileReader(file);

        Landscape landscape = Landscape.loadLandscape(reader);

        return new DesignLandscape(landscape);
    }

    public static DesignLandscape loadDesignLandscape(PhysicsFileReader reader)
    {
        return new DesignLandscape(Landscape.loadLandscape(reader));
    }
    
    private Vector listeners = new Vector();
    public void addListener(DesignObjectChangeListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeListener(DesignObjectChangeListener listener)
    {
        listeners.remove(listener);
    }

    public void notifyListeners()
    {
        for(int i = 0; i < listeners.size(); i++)
        {
            ((DesignObjectChangeListener) listeners.elementAt(i)).designObjectChanged(this);
        }
    }
    
    public String toString()
    {
        return "Landscape";
    }
}
