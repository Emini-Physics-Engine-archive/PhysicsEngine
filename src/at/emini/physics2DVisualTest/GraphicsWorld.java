package at.emini.physics2DVisualTest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Contact;
import at.emini.physics2D.Landscape;
import at.emini.physics2D.Spring;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

/**
 * 
 * @author Alexander Adensamer
 * 
 * Wrapper around World for testing purposes
 *
 */
public class GraphicsWorld extends World
{
   
   private boolean drawContacts = true; 
   private boolean drawVertexTrajectories = false;
   private boolean drawBodyTrajectory = true;
 
   private double zoom = 1.0;

   private Graphics graphics;
    
   public GraphicsWorld()
   {
   }
   
   public GraphicsWorld(World world)
   {
       super(world);
   }
   
   public void collisionUpdate()
   {
       //check for collisions
       checkCollisions();       
   }
   
   public synchronized void executeManualMoves()
   {
       for( int i = 0; i < getBodyCount(); i++)
       {
           if (getBodies()[i] instanceof InteractiveBody)
           {
               InteractiveBody b = (InteractiveBody) getBodies()[i];
               b.executeManual();
           }
       }
   }
   
   
   public double getZoom()
   {
       return zoom;
   }
   
   public void setZoom( double zoom)
   {
       this.zoom = zoom;
   }
   
   private static int currColor = 0;
   private static final Color defaultColors[] = {
   new Color(255,   0,   0,  55), 
   new Color(255, 255,   0,  55),
   new Color(  0, 255,   0,  55),
   new Color(  0, 255, 255,  55),
   new Color(  0,   0, 255,  55),
   new Color(255,   0, 255,  55),
   new Color(255, 120, 120,  55),
   new Color(120, 255, 120,  55),
   //new Color(120, 120, 255,  55),
   };
   
   //draws everything
   public void draw(Graphics g)
   {
       
       //bodies
       g.setColor( Color.black );
       for( int i = 0; i < getBodyCount(); i++)
       {           
           drawBody( g, getBodies()[i] );
       }
     
       //landscape
       drawLandscape(g, getLandscape());
       
       g.setColor(Color.black);
       //joints
       for( int i = 0; i < getConstraintCount(); i++)
       {
           Constraint constraint = getConstraints()[i];
           if (constraint instanceof Spring)
           {
               Spring joint = (Spring) constraint;
               FXVector p1 = joint.getPoint1();
               FXVector p2 = joint.getPoint2();
               g.drawLine((int) (p1.xAsFloat() * zoom), 
                          (int) (p1.yAsFloat() * zoom), 
                          (int) (p2.xAsFloat() * zoom), 
                          (int) (p2.yAsFloat() * zoom));
               
               //System.out.print(((float) p1.distFX(p2) / (float) FXUtil.ONE) + "\n");
           }
       }
       
       if (drawContacts)
       {
           for( int i = 0; i < getContactCount(); i++)
           {               
               Contact contact = getContacts()[i];           
               g.setColor( Color.red );
               FXVector pos = new FXVector(contact.getContactPosition1());
               g.fillArc( (int) (pos.xAsFloat()* zoom) - 2, (int) (pos.yAsFloat() * zoom )- 2, 5, 5, 0, 360); 
               
               g.setColor( Color.orange );
               FXVector normEndPos = new FXVector(pos);
               normEndPos.add(contact.getNormal().timesFX( - contact.getDepth1FX()));
               g.drawLine( (int) (pos.xAsFloat() * zoom), 
                           (int) (pos.yAsFloat() * zoom), 
                           (int) (normEndPos.xAsFloat() * zoom), 
                           (int) (normEndPos.yAsFloat() * zoom));
               
               g.fillArc( (int) (normEndPos.xAsFloat()* zoom) - 2, (int) (normEndPos.yAsFloat() * zoom )- 2, 5, 5, 0, 360); 
               
               
               if (! contact.isSingle())
               {
                   g.setColor( Color.red );
                   pos = new FXVector(contact.getContactPosition2());
                   g.fillArc( (int) (pos.xAsFloat()* zoom) - 2, (int) (pos.yAsFloat() * zoom )- 2, 5, 5, 0, 360); 

                   g.setColor( Color.orange );
                   normEndPos = new FXVector(pos);
                   normEndPos.add(contact.getNormal().timesFX( - contact.getDepth2FX()));
                   g.drawLine( (int) (pos.xAsFloat() * zoom), 
                               (int) (pos.yAsFloat() * zoom), 
                               (int) (normEndPos.xAsFloat() * zoom), 
                               (int) (normEndPos.yAsFloat() * zoom));
                   
                   g.fillArc( (int) (normEndPos.xAsFloat()* zoom) - 2, (int) (normEndPos.yAsFloat() * zoom )- 2, 5, 5, 0, 360); 
                   
               }
           }           
       }
              
   }
   
   private void drawLandscape(Graphics g, Landscape landscape) 
   {
       g.setColor(Color.black);
       for( int i = 0; i < landscape.segmentCount(); i++)
       {
           g.drawLine( (int) (landscape.startPoint(i).xAsFloat() * zoom), 
                   (int) (landscape.startPoint(i).yAsFloat() * zoom),
                   (int) (landscape.endPoint(i).xAsFloat() * zoom), 
                   (int) (landscape.endPoint(i).yAsFloat() * zoom));
           
       }    
    
   }

   //draws a single body
   public void drawBody(Graphics graphics, Body b)
   {   
       this.graphics = graphics;
       Graphics2D g = (Graphics2D) graphics;
       FXVector[] positions = b.getVertices();
       
       Color color1 = Color.darkGray;
       if (b instanceof InteractiveBody)
       {
           color1 = ((InteractiveBody) b).c;
       }
       g.setColor(color1);       

       if (positions.length == 1)
       {
           double factor = 1 / ((double) FXUtil.ONE_FX) * zoom;
           int radiusFX = b.shape().getBoundingRadiusFX();
           if (! b.isDynamic())
           {
               //g.setPaint(new GradientPaint(0, 0, color1, 0, 1, Color.black));
               g.setColor(Color.lightGray);
               g.fillArc((int)( (b.positionFX().xFX - radiusFX) * factor), 
                       (int)((b.positionFX().yFX - radiusFX) * factor), 
                       (int)(radiusFX * 2 * factor), 
                       (int)(radiusFX * 2 * factor), 0, 360);
               g.setColor(color1);
           }
           else if (b.isResting())
           {
               //g.setPaint(new GradientPaint(0, 0, color1, 0, 1, Color.black));
               g.setColor(new Color(200,200,255));
               g.fillArc((int)((b.positionFX().xFX - radiusFX) * factor), 
                       (int)((b.positionFX().yFX - radiusFX) * factor), 
                       (int)(radiusFX * 2 * factor), 
                       (int)(radiusFX * 2 * factor), 0, 360);
               g.setColor(color1);
           }
           
           g.drawArc((int)((b.positionFX().xFX - radiusFX) * factor), 
                   (int)((b.positionFX().yFX - radiusFX) * factor), 
                   (int)(radiusFX * 2 * factor), 
                   (int)(radiusFX * 2 * factor), 0, 360);        
           g.drawLine((int)((b.positionFX().xFX) * factor), 
                   (int)((b.positionFX().yFX) * factor) , 
                   (int)(positions[0].xFX * factor), 
                   (int)(positions[0].yFX * factor));
           
       }
       else
       {
           Polygon polygon = new Polygon();
           for( int i = 0; i < positions.length; i++)
           {
               polygon.addPoint( (int) (positions[i].xAsFloat() * zoom), (int) (positions[i].yAsFloat() * zoom));
           }
           
           if (! b.isDynamic())
           {
               //g.setPaint(new GradientPaint(0, 0, color1, 0, 1, Color.black));
               g.setColor(Color.lightGray);
               g.fillPolygon(polygon);
               g.setColor(color1);
               g.drawPolygon(polygon);
           }
           else if (b.isResting())
           {
               //g.setPaint(new GradientPaint(0, 0, color1, 0, 1, Color.black));
               g.setColor(new Color(200,200,255));
               g.fillPolygon(polygon);
               g.setColor(color1);
               g.drawPolygon(polygon);
           }
           else
           {
               g.drawPolygon(polygon);
           }
       }
       
       if (drawBodyTrajectory)
       {
           //draw current velocity
           g.setColor(Color.blue);
           FXVector pos2 = new FXVector(b.positionFX());
           pos2.add(b.velocityFX().times(5));
           g.drawLine( (int) (b.positionFX().xAsFloat() * zoom), 
                       (int) (b.positionFX().yAsFloat() * zoom), 
                       (int) (pos2.xAsFloat() * zoom), 
                       (int) (pos2.yAsFloat() * zoom));
       }
       
       if (drawVertexTrajectories)
       {
           g.setColor(Color.blue);
           
           for( int i = 0; i < positions.length; i++)
           {
               //VectorFX relativePosition = new VectorFX(b.positionFX());
               //relativePosition.subtract(positions[i]);
               FXVector relativePosition = new FXVector(positions[i]);
               relativePosition.subtract(b.positionFX());
               FXVector velocity = b.getVelocity(relativePosition).times(5);
               velocity.add(positions[i]);
                              
               g.drawLine( (int) (positions[i].xAsFloat() * zoom), 
                           (int) (positions[i].yAsFloat() * zoom), 
                           (int) (velocity.xAsFloat() * zoom), 
                           (int) (velocity.yAsFloat() * zoom));
           }           
       }
   }

   
   public void drawColorBody(Graphics graphics, Body b, Color c)
   {  
       
       Graphics2D g = (Graphics2D) graphics;
       FXVector[] positions = b.getVertices();
       
       Polygon polygon = new Polygon();
       for( int i = 0; i < positions.length; i++)
       {
           polygon.addPoint( (int) (positions[i].xAsFloat() * zoom), (int) (positions[i].yAsFloat() * zoom));
       }

       g.setColor(c);       
       //g.fillPolygon(polygon);
   }

   
   /*protected void __checkBodyPair(Body body1, Body body2)
   {
       if ( ! body1.isDynamic() && ! body2.isDynamic())
       {
           return;
       }
              
       Contact newContact = Collision.detectCollision( body1, body2);
       
       if (newContact != null)
       {   
           contacts[contactCount++] = newContact;
       }
       
       if (   ((InteractiveBody) body1).move || ((InteractiveBody) body1).rotate 
           || ((InteractiveBody) body2).move || ((InteractiveBody) body2).rotate)
       { 
           
           graphics.setColor(Color.black);
           graphics.drawLine( (int) (body1.positionFX().xAsFloat() * zoom), 
                           (int) (body1.positionFX().yAsFloat() * zoom), 
                           (int) (body2.positionFX().xAsFloat() * zoom), 
                           (int) (body2.positionFX().yAsFloat() * zoom));
       }
   }*/
   
    public boolean isDrawContacts() {
        return drawContacts;
    }
    
    
    public void setDrawContacts(boolean drawContacts) {
        this.drawContacts = drawContacts;
    }
        
    public boolean isDrawVertexTrajectories() {
        return drawVertexTrajectories;
    }
    
    
    public void setDrawVertexTrajectories(boolean drawVertexTrajectory) {
        this.drawVertexTrajectories = drawVertexTrajectory;
    }


    public boolean isDrawBodyTrajectory() {
        return drawBodyTrajectory;
    }


    public void setDrawBodyTrajectory(boolean drawBodyTrajectory) {
        this.drawBodyTrajectory = drawBodyTrajectory;
    }

    public void translate(FXVector translation)
    {
        int bodyCount = getBodyCount();
        Body[] bodies = getBodies();
        for( int i = 0; i < bodyCount; i++)
        {
            bodies[i].translate(translation, getTimestepFX());            
        }
        
    }
}
