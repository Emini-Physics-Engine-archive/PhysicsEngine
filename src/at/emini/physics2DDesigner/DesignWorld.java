package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import at.emini.physics2D.Body;
import at.emini.physics2D.Constraint;
import at.emini.physics2D.Contact;
import at.emini.physics2D.Event;
import at.emini.physics2D.Joint;
import at.emini.physics2D.Landscape;
import at.emini.physics2D.Motor;
import at.emini.physics2D.MultiShape;
import at.emini.physics2D.ParticleEmitter;
import at.emini.physics2D.Script;
import at.emini.physics2D.Shape;
import at.emini.physics2D.Spring;
import at.emini.physics2D.UserData;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;

/*
 * Wrapper around World for testing purposes
 * 
 * @author Alexander Adensamer
 *
 */
public class DesignWorld extends World
{
    /**
     * **Duplicated from world**
     * This is removed when saving is moved to World class
     */
    static final int MASK_VERSION = 0xff00;   //version mask
    static final int VERSION_1 =    0x0100;   //version Index
    static final int VERSION_2 =    0x0200;   //version Index
    static final int VERSION_3 =    0x0300;   //version Index
    static final int VERSION_4 =    0x0400;   //version Index 4: new event structure, trigger once
    static final int VERSION_5 =    0x0500;   //version Index 5: shape uses mass
    static final int VERSION_6 =    0x0600;   //version Index 6: add simulation parameter in world file
    static final int VERSION_7 =    0x0700;   //version Index 7: userData for shape and body
    static final int VERSION_8 =    0x0800;   //version Index 8: userData for world, events, particles and constraints
    static final int VERSION_9 =    0x0900;   //version Index 9: multishapes
    static final int VERSION_10 =   0x0A00;   //version Index 10: rotational damping

    //section indices
    static final int SHAPES_IDX       = 1;   
    static final int BODY_IDX         = 2;   
    static final int CONSTRAINTS_IDX  = 3;   
    static final int SCRIPTS_IDX      = 4;   
    static final int EVENTS_IDX       = 5;   
    static final int LANDSCAPE_IDX    = 6;   
    static final int WORLD_IDX        = 7;   
    static final int PARTICLES_IDX    = 8;   
       
   private boolean drawContacts = true; 
   private boolean drawVertexTrajectories = false;
   private boolean drawBodyTrajectory = true;
   private boolean drawParticleTrajectory = true;
   private boolean drawAABB = false;
   private boolean drawDesignInfo = true;
   
   private boolean drawInteractives = true;

   private int currLayer = 0;   //collision layer 
   
   private int contactSize = 5;
   
   private Random random = new Random();
   
   private Vector listener = new Vector();
   
   private DefaultTreeModel objectTreeModel = new DefaultTreeModel(new ObjectTreeNode("World", worldIcon));           
   
   private DefaultMutableTreeNode bodiesNode;
   private DefaultMutableTreeNode constraintsNode;
   private DefaultMutableTreeNode eventsNode;
   private DefaultMutableTreeNode particlesNode;
   private DefaultMutableTreeNode landscapeNode;
   private DefaultMutableTreeNode parameterNode;
   
   private static ImageIcon worldIcon;
   
   private static ImageIcon bodyIcon;
   private static ImageIcon particleIcon;
   private static ImageIcon eventIcon;
   private static ImageIcon jointIcon;
   private static ImageIcon fixjointIcon;
   private static ImageIcon springIcon;
   private static ImageIcon motorIcon;
   
   private static ImageIcon bodiesIcon;
   private static ImageIcon constraintsIcon;
   private static ImageIcon eventsIcon;
   private static ImageIcon landscapeIcon;
   private static ImageIcon particlesIcon;
   private static ImageIcon parameterIcon;
   
   public static void initImages(Object anchor)
   {
       try
       {      
           Image image;
           
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/base_world.png") );
           worldIcon = new ImageIcon(image, "World");
           
         //create imageicons for elements
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/base_body.png") );
           bodyIcon = new ImageIcon(image, "Body");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/base_event.png") );
           eventIcon = new ImageIcon(image, "Event");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/base_particles.png") );
           particleIcon = new ImageIcon(image, "Particle Emitter");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/base_joint.png") );
           jointIcon = new ImageIcon(image, "Joint");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/base_fixjoint.png") );
           fixjointIcon = new ImageIcon(image, "Fix joint");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/base_spring.png") );
           springIcon = new ImageIcon(image, "Spring");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/base_motor.png") );
           motorIcon = new ImageIcon(image, "Motor");
           
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/multi_bodies.png") );
           bodiesIcon = new ImageIcon(image, "Bodies");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/multi_constraints.png") );
           constraintsIcon = new ImageIcon(image, "Constraints");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/multi_events.png") );
           eventsIcon = new ImageIcon(image, "Events");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/multi_particles.png") );
           particlesIcon = new ImageIcon(image, "Particles");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/button_new_landscape.png") );
           landscapeIcon = new ImageIcon(image, "Landscape");
           image = ImageIO.read( anchor.getClass().getResourceAsStream("/res/parameters.png") );
           parameterIcon = new ImageIcon(image, "Parameter");
           
       }
       catch (IOException e)
       {
           e.printStackTrace();
       }
   }
   
   public DesignWorld()
   {
       setLandscape(new DesignLandscape());
       mShapeSet = new DesignShapeSet();
       mUserData = new StringUserData();
       createTreeModel();
   }
   
   public DesignWorld(DesignWorld world)
   {
       super(world);
       createTreeModel();
       
       addAllElementNodes();
   }
   
   public DesignWorld copy()
   {
       DesignWorld copy = new DesignWorld(this);
       
       return copy;
   }

      
   public TreeModel getObjectTreeModel()
   {
       return objectTreeModel;
   }
   
   private void createTreeModel()
   {
       bodiesNode = new ObjectTreeNode("Bodies", bodiesIcon);
       constraintsNode = new ObjectTreeNode("Constraints", constraintsIcon);
       eventsNode = new ObjectTreeNode("Events", eventsIcon);
       particlesNode = new ObjectTreeNode("Particles", particlesIcon);
       landscapeNode = new ObjectTreeNode("Landscape", landscapeIcon, (DesignLandscape) getLandscape());
       parameterNode = new ObjectTreeNode("Parameter", parameterIcon, new DesignParameter(this));
       
       ObjectTreeNode rootNode = (ObjectTreeNode) objectTreeModel.getRoot();
       rootNode.add(bodiesNode);
       rootNode.add(constraintsNode);
       rootNode.add(eventsNode);
       rootNode.add(particlesNode);
       rootNode.add(landscapeNode);
       rootNode.add(parameterNode);
   }
   
   public void registerListener(WorldChangeListener listener)
   {
       this.listener.add(listener);
   }
   
   public void updateAndRepaint()
   {
       recalcBodyVertices();
       for( int i = 0; i < listener.size(); i++)
       {
           ((WorldChangeListener) listener.get(i)).updateRequired();
       }
   }
   
   public void worldChanged()
   {
       for( int i = 0; i < listener.size(); i++)
       {
           ((WorldChangeListener) listener.get(i)).worldChanged(this);
       }
   }
   
   public void collisionUpdate()
   {
       //check for collisions
       checkCollisions();       
   }
   
   public DesignShapeSet getDesignShapeSet()
   {
       return (DesignShapeSet) getShapeSet();
   }
   
   public void setDesignShapeSet(DesignShapeSet shapeset)
   {
       this.mShapeSet = shapeset;
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
   
   public void jitter()
   {
       for( int i = 0; i < getBodyCount(); i++)
       {
           jitter(getBodies()[i]);
       }
   }
   
   private void jitter(Body b)
   {
       int jitterFX = 1 << (FXUtil.DECIMAL - 2); 
       b.positionFX().add( new FXVector( random.nextInt() % jitterFX, random.nextInt() % jitterFX)); 
       
   }
   
   //draws everything
   public void draw(GraphicsWrapper g, boolean simulate)
   {       
       //bodies
       g.setColor( Color.black );
       for( int i = 0; i < getBodyCount(); i++)
       {
           drawBody( g, getBodies()[i] );
	   
           /*if ( ! simulate && drawInteractives && (bodies[i] instanceof DesignBody) )
           {
               ((DesignBody) bodies[i]).drawInteractives(g);
           }*/
       }

       ((DesignLandscape) getLandscape()).drawObject(g, Color.black, null, (!simulate) || drawDesignInfo);
       
       g.setColor(Color.black);
       //joints
       for( int i = 0; i < getConstraintCount(); i++)
       {
           Constraint constraint = getConstraints()[i];
           
           if (constraint instanceof DesignConstraint)
           {
               ((DesignConstraint) constraint).drawObject(g, Color.black, null, (!simulate) || drawDesignInfo);
           }
       }
       
       //draw events
       for( int i = 0; i < eventCount(); i++)
       {
           DesignPhysicsEvent e = (DesignPhysicsEvent) getEvent(i); 
           e.drawObject(g, Color.black, null, (!simulate) || drawDesignInfo);           
       }    
       
       //draw particles
       for( int i = 0; i < mParticles.size(); i++)
       {
           DesignParticleEmitter emitter = (DesignParticleEmitter) mParticles.get(i);
           emitter.drawParticles(g, drawParticleTrajectory);
           
           emitter.drawObject(g, Color.black, null, (!simulate) || drawDesignInfo);           
       }
       
       
       if (simulate && drawContacts)
       {
           double zoomScale = g.getZoomScale();
           for( int i = 0; i < getContactCount(); i++)
           {               
               Contact contact = getContacts()[i];           
               g.setColor( Color.red );
               FXVector pos = new FXVector(contact.getContactPosition1());
               g.fillArc( (int) (pos.xFX - contactSize / zoomScale), (int) (pos.yFX - contactSize / zoomScale), (int) (contactSize * 2  / zoomScale)+ 1, (int) (contactSize * 2 / zoomScale), 0, 360); 
               
               g.setColor( Color.orange );
               FXVector normEndPos = new FXVector(pos);
               normEndPos.add(contact.getNormal().timesFX( - contact.getDepth1FX()));
               g.drawLine( (int) (pos.xFX ), 
                           (int) (pos.yFX ), 
                           (int) (normEndPos.xFX ), 
                           (int) (normEndPos.yFX ));
               
               g.fillArc( (int) (normEndPos.xFX - contactSize / zoomScale), (int) (normEndPos.yFX - contactSize / zoomScale), (int) (contactSize * 2 / zoomScale), (int)(contactSize * 2 / zoomScale), 0, 360); 
               
               
               if (! contact.isSingle())
               {
                   g.setColor( Color.red );
                   pos = new FXVector(contact.getContactPosition2());
                   g.fillArc( (int) (pos.xFX - contactSize / zoomScale), (int) (pos.yFX - contactSize / zoomScale), (int)(contactSize * 2 / zoomScale), (int)(contactSize * 2 / zoomScale), 0, 360); 

                   g.setColor( Color.orange );
                   normEndPos = new FXVector(pos);
                   normEndPos.add(contact.getNormal().timesFX( - contact.getDepth2FX()));
                   g.drawLine( (int) (pos.xFX), 
                               (int) (pos.yFX), 
                               (int) (normEndPos.xFX), 
                               (int) (normEndPos.yFX));
                   
                   g.fillArc( (int) (normEndPos.xFX - contactSize / zoomScale), (int) (normEndPos.yFX - contactSize / zoomScale), (int)(contactSize * 2 / zoomScale), (int)(contactSize * 2 / zoomScale), 0, 360); 
                   
               }
           }           
       }
              
   }
  
   BufferedImage imageBuffer;
   //draws a single body
   public void drawBody(GraphicsWrapper g, Body b)
   {   
       BufferedImage image = getDesignShapeSet().getImage(b);
       if (image != null)
       {            
           int widthFX = image.getWidth() * FXUtil.ONE_FX; 
           int heightFX = image.getHeight() * FXUtil.ONE_FX;
           g.drawImage(image, b.positionFX().xFX - widthFX / 2,  b.positionFX().yFX - heightFX / 2, widthFX, heightFX, b.rotation2FX(), null);
       }
       
       if (b instanceof DesignBody)
       {
           DesignBody designBody = (DesignBody) b;
           if (! designBody.isDynamic())
           {
               designBody.fillBody(g, Color.gray);
           }
           designBody.fillBody(g, designBody.getColor());
       }
       
       if (drawAABB)
       {
           //draw aabb
           g.drawRect(b.getAABBMinXFX(),
                   b.getAABBMinYFX(),
                   b.getAABBMaxXFX() - b.getAABBMinXFX(),
                   b.getAABBMaxYFX() - b.getAABBMinYFX());
       }
       
       FXVector[] positions = b.getVertices();
              
       if (drawBodyTrajectory)
       {
           //draw current velocity
           g.setColor(Color.blue);
           FXVector pos2 = new FXVector(b.positionFX());
           pos2.add(b.velocityFX());
           g.drawLine( (int) (b.positionFX().xFX), 
                       (int) (b.positionFX().yFX), 
                       (int) (pos2.xFX), 
                       (int) (pos2.yFX));
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
                              
               g.drawLine( (int) (positions[i].xFX), 
                           (int) (positions[i].yFX), 
                           (int) (velocity.xFX), 
                           (int) (velocity.yFX));
           }           
       }
   }

   
   public void drawColorBody(Graphics graphics, Body b, Color c)
   {  
       
       Graphics2D g = (Graphics2D) graphics;
       FXVector[] positions = b.getVertices();
       
       //Polygon polygon = GraphicsWrapper.createPolygon(positions);

       //g.setColor(c);       
       //g.fillPolygon(polygon);
   }

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

    public boolean isDrawAABB() {
        return drawAABB;
    }


    public void setDrawAABB(boolean drawAABB) {
        this.drawAABB = drawAABB;
    }

    public void recalcBodyVertices() {
        for( int i = 0; i < getBodyCount(); i++)
        {
            ((DesignBody) getBodies()[i]).recalcShapeInternals();
            getBodies()[i].setRotation2FX(getBodies()[i].rotation2FX());            
        }        
    }
    
    
    private ObjectTreeNode findModelNode(DesignSelectionObject object)
    {
        return ((ObjectTreeNode) objectTreeModel.getRoot()).findNode(object);
    }
    
    public void addBody(Body body)
    {
        super.addBody(body);        
        addBodyNode(body);
    }
    
    private void addBodyNode(Body body)
    {
        if (body instanceof DesignBody && objectTreeModel != null)
        {
            ObjectTreeNode newNode = new ObjectTreeNode("Body", bodyIcon, (DesignBody) body);
            objectTreeModel.insertNodeInto(newNode, bodiesNode, bodiesNode.getChildCount());
        }        
    }
    
    public void setLandscape(Landscape landscape)
    {
        super.setLandscape(landscape);
        if (landscapeNode != null)
        {
            ((ObjectTreeNode) landscapeNode).setObject((DesignLandscape) landscape);
        }
    }
    
    public void addConstraint(Constraint constraint)
    {
        super.addConstraint(constraint);
        
        addConstraintNode(constraint);
    }
    
    private void addConstraintNode(Constraint constraint)
    {
        if (constraint instanceof DesignConstraint && objectTreeModel != null)
        {
            ObjectTreeNode newNode = null;
            if (constraint instanceof Spring)
            {
                newNode = new ObjectTreeNode("Spring", springIcon, (DesignConstraint) constraint);
            }
            else if (constraint instanceof Joint)
            {
                if ( ! ((Joint) constraint).isFixed() )
                {
                    newNode = new ObjectTreeNode("Joint", jointIcon, (DesignConstraint) constraint);                
                }
                else
                {
                    newNode = new ObjectTreeNode("Fixed Joint", fixjointIcon, (DesignConstraint) constraint);
                }
            } 
            else if (constraint instanceof Motor)
            {
                newNode = new ObjectTreeNode("Motor", motorIcon, (DesignConstraint) constraint);
            }
            
            if (newNode != null)
            {
                objectTreeModel.insertNodeInto(newNode, constraintsNode, constraintsNode.getChildCount());
            }
        }
    }

    public void addEvent(Event event)
    {
        super.addEvent(event);
        
        addEventNode(event);
    }
    
    private void addEventNode(Event event)
    {
        if (event instanceof DesignPhysicsEvent && objectTreeModel != null)
        {
            ObjectTreeNode newNode = new ObjectTreeNode("Event", eventIcon, (DesignPhysicsEvent) event);
            objectTreeModel.insertNodeInto(newNode, eventsNode, eventsNode.getChildCount());
        }
    }

    public void addParticleEmitter(ParticleEmitter emitter)
    {
        super.addParticleEmitter(emitter);
        
        addParticleEmitterNode(emitter);
    }
    
    private void addParticleEmitterNode(ParticleEmitter emitter)
    {
        if (emitter instanceof DesignParticleEmitter && objectTreeModel != null)
        {
            ObjectTreeNode newNode = new ObjectTreeNode("Particle Emitter", particleIcon, (DesignParticleEmitter) emitter);
            objectTreeModel.insertNodeInto(newNode, particlesNode, particlesNode.getChildCount());
        }
    }
    
    protected void addAllElementNodes()
    {
        Body[] bodies = getBodies();
        for( int i = 0; i < getBodyCount(); i++)
        {
            addBodyNode(bodies[i]);
        }
        
        Constraint[] constraints = getConstraints();
        for( int i = 0; i < getConstraintCount(); i++)
        {
            addConstraintNode(constraints[i]);
        }
        
        Vector events = getEvents();
        for( int i = 0; i < events.size(); i++)
        {
            addEventNode((Event) events.get(i)); 
        }
        
        Vector emitters = getParticleEmitters();
        for( int i = 0; i < emitters.size(); i++)
        {
            addParticleEmitterNode((ParticleEmitter)emitters.get(i));
        }
    }

    protected void validateTree()
    {
        //check bodies
        for( int i = bodiesNode.getChildCount() - 1; i >= 0; i--)
        {
            DesignBody body = (DesignBody) ((ObjectTreeNode) bodiesNode.getChildAt(i)).getObject();
            if (findBody(body) == null)
            {
                objectTreeModel.removeNodeFromParent((ObjectTreeNode)bodiesNode.getChildAt(i));
            }
        }
        
        //check constraints
        for( int i = constraintsNode.getChildCount() - 1; i >= 0; i--)
        {
            DesignConstraint constraint = (DesignConstraint) ((ObjectTreeNode) constraintsNode.getChildAt(i)).getObject();
            if (findConstraint(constraint) == null)
            {
                objectTreeModel.removeNodeFromParent((ObjectTreeNode)constraintsNode.getChildAt(i));
            }
        }
        
        //check particles
        for( int i = particlesNode.getChildCount() - 1; i >= 0; i--)
        {
            DesignParticleEmitter particles = (DesignParticleEmitter) ((ObjectTreeNode) particlesNode.getChildAt(i)).getObject();
            if (getParticleEmitters().indexOf(particles) < 0)
            {
                objectTreeModel.removeNodeFromParent((ObjectTreeNode)particlesNode.getChildAt(i));
            }
        }
        
    }
    
    public DesignBody checkBody(GraphicsWrapper g, FXVector p1, FXVector p2, DesignSelectionObject ignoreObject, DesignElementSelector selector) 
    {
        for( int i = 0; i< getBodyCount(); i++)
        {
            if (getBodies()[i] instanceof DesignBody && getBodies()[i] != ignoreObject)
            {
                DesignBody body = ((DesignBody) getBodies()[i]);
                if ( body.getAction(g, p1) >= 0 || body.isPointInShape(p1))
                {
                    if (selector == null)
                    {
                        return body;
                    }
                    selector.addSelection(body);
                    continue;
                }
                if (body.isShapeInSelection(p1, p2) && selector != null && selector.canSelectMultiple())
                {
                    if (selector == null)
                    {
                        return body;
                    }
                    selector.addSelection(body);
                }
            }            
        }
        return null;
    }
    
    public void checkConstraint(GraphicsWrapper g, FXVector p1, FXVector p2, DesignSelectionObject ignoreObject, DesignElementSelector selector) 
    {
        for( int i = 0; i< getConstraintCount(); i++)
        {
            if (getConstraints()[i] instanceof DesignConstraint && getConstraints()[i] != ignoreObject)
            {
                DesignConstraint constraint = ((DesignConstraint) getConstraints()[i]);
                if ( constraint.getAction(g, p1) >= 0 )
                {
                    selector.addSelection(constraint);
                    continue;
                }
                if (constraint.isConstraintInSelection(g, p1, p2) && selector.canSelectMultiple())
                {
                    selector.addSelection(constraint);
                }
            }            
        }
    }
    
    public void checkParticle(GraphicsWrapper g, FXVector p1, FXVector p2, DesignSelectionObject ignoreObject, DesignElementSelector selector) 
    {
        for( int i = 0; i< getParticleEmitters().size(); i++)
        {
            
            if (getParticleEmitters().get(i) instanceof DesignParticleEmitter && getParticleEmitters().get(i) != ignoreObject)
            {
                DesignParticleEmitter emitter = ((DesignParticleEmitter) getParticleEmitters().get(i));
                if ( emitter.getAction(g, p1) >= 0 )
                {
                    selector.addSelection(emitter);
                    continue;
                }
                /*if (emitter.isInSelection(g, p1, p2))
                {
                    selection.add(emitter);
                }*/
            }            
        }
    }
    
    
    public void checkEvent(GraphicsWrapper g, FXVector p1, FXVector p2, DesignSelectionObject ignoreObject, DesignElementSelector selector) 
    {
        Vector events = getEvents();
        for( int i = 0; i < events.size(); i++)
        {   
            if (events.elementAt(i) instanceof DesignPhysicsEvent && events.elementAt(i) != ignoreObject)
            {
                DesignPhysicsEvent event = (DesignPhysicsEvent) events.elementAt(i);
                if ( event.getAction(g, p1) >= 0 )
                {
                    selector.addSelection(event);
                    continue;
                }
            }            
        }
    }

    public void checkLandscape(GraphicsWrapper g, FXVector p1, FXVector p2, DesignSelectionObject ignoreObject, DesignElementSelector selector) 
    {
        if (getLandscape() instanceof DesignLandscape && getLandscape() != ignoreObject)
        {
            DesignLandscape object = ((DesignLandscape) getLandscape());
            if ( object.getAction(g, p1) >= 0 )
            {   
                selector.addSelection(object);
            }
            if ( object.getAction(g, p1, p2) >= 0 )
            {   
                selector.addSelection(object);
            }
        }
        
    }

    public void checkObject(GraphicsWrapper g, FXVector mousePos, FXVector startPoint,  DesignElementSelector selector, DesignSelectionObject ignoreObject) 
    {
        if (startPoint == null)
        {
            startPoint = mousePos;
        }
        selector.clearSelection();
        
        FXVector p1 = new FXVector( Math.min(mousePos.xFX, startPoint.xFX),
                Math.min(mousePos.yFX, startPoint.yFX));
        FXVector p2 = new FXVector( Math.max(mousePos.xFX, startPoint.xFX),
                Math.max(mousePos.yFX, startPoint.yFX));

        if (selector.canSelectConstraint())
        {
            checkConstraint(g, p1, p2, ignoreObject, selector);            
        }
        
        if (selector.canSelectParticle())
        {
            checkParticle(g, p1, p2, ignoreObject, selector);            
        }
        
        if (selector.canSelectBody())
        {            
            checkBody(g, p1, p2, ignoreObject, selector);
        }        
        
        if (selector.canSelectLandscape())
        {
	        checkLandscape(g, p1, p2, ignoreObject, selector); 
        }       
        
        if (selector.canSelectEvent())
        {
            checkEvent(g, p1, p2, ignoreObject, selector);            
        }
        
        //selector.setSelection(selection);
    }

    public boolean isShapeUsed(Shape s)
    {
        for( int i = 0; i< getBodyCount(); i++)
        {
            if (getBodies()[i].shape() == s)
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isDrawInteractives() {
        return drawInteractives;
    }

    public void setDrawInteractives(boolean drawInteractives) {
        this.drawInteractives = drawInteractives;
    }

    public void saveToFile(File file)
    {
        try 
        {
            MyFileWriter fileWriter = new MyFileWriter( file );
            saveToFile(fileWriter);
            fileWriter.close();
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }
    }
    
    public void saveToFile(MyFileWriter fileWriter)
    {
        try 
        {
            reorderIds();
            
            int versionIndex = VERSION_10;
            fileWriter.writeInt(versionIndex);
            
            Vector rawShapes = getShapes();
            Vector shapes = new Vector();
            //correct shape order
                        
            //first shapes
            fileWriter.write(SHAPES_IDX);
            int stdShapeCount = 0;
            for( int i = 0; i < rawShapes.size(); i++)
            {
                if (! (rawShapes.get(i) instanceof MultiShape))
                {
                    shapes.add(rawShapes.get(i));
                    stdShapeCount++;
                }
            }
            for( int i = 0; i < rawShapes.size(); i++)
            {
                if ((rawShapes.get(i) instanceof MultiShape))
                {   shapes.add(rawShapes.get(i));
                }
            }
            
            //std shapes
            fileWriter.write(stdShapeCount);            
            for( int i = 0; i < stdShapeCount; i++)
            {
                ((DesignShape) shapes.get(i)).saveToFile(fileWriter, shapes);                
            }
            
            //multi shapes
            fileWriter.write(shapes.size() - stdShapeCount);            
            for( int i = stdShapeCount; i < shapes.size(); i++)
            {
                ((DesignShape) shapes.get(i)).saveToFile(fileWriter, shapes);                
            }
            
            //then the bodies
            fileWriter.write(BODY_IDX);
            Vector bodyVec = new Vector();
            fileWriter.write(getBodyCount());
            for( int i = 0; i< getBodyCount(); i++)
            {
                bodyVec.addElement(getBodies()[i]);
                ((DesignBody) getBodies()[i]).saveToFile(fileWriter, shapes);
            }
            
            //then the landscape
            if (getLandscape() != null)
            {
	            fileWriter.write(LANDSCAPE_IDX);
	            ((DesignLandscape) getLandscape()).saveToFile(fileWriter, shapes);
            }
            
            if (getConstraintCount() > 0 )
            {
                fileWriter.write(CONSTRAINTS_IDX);
                //then the constraints
                fileWriter.write(getConstraintCount());
                for( int i = 0; i< getConstraintCount(); i++)
                {
                    ((DesignConstraint) getConstraints()[i]).saveToFile(fileWriter, bodyVec);
                } 
            }
            
            if (getScriptCount() > 0)
            {
                fileWriter.write(SCRIPTS_IDX);
                //save the scripts
                fileWriter.write(getScriptCount());
                for( int i = 0; i< getScriptCount(); i++)
                {
                    ((DesignScript) getScripts()[i]).saveToFile(fileWriter);
                }
                
                //save script bodies
                fileWriter.write(getScriptBodyCount());
                for( int i = 0; i< getScriptBodyCount(); i++)
                {
                    fileWriter.write( getScriptIndices()[i] );
                    fileWriter.write( bodyVec.indexOf(getScriptBodies()[i]) );
                }
            }            
            
            if (getEvents().size() > 0)
            {
                fileWriter.write(EVENTS_IDX);
                //then the events
                fileWriter.write(getEvents().size());
                for( int i = 0; i< getEvents().size(); i++)
                {
                    ((DesignPhysicsEvent) getEvents().elementAt(i)).saveToFile(fileWriter, this);
                }
            }    
            
            if (getParticleEmitters().size() > 0)
            {
                fileWriter.write(PARTICLES_IDX);
                //then the particles
                fileWriter.write(getParticleEmitters().size());
                for( int i = 0; i< getParticleEmitters().size(); i++)
                {
                    ((DesignParticleEmitter) getParticleEmitters().elementAt(i)).saveToFile(fileWriter, this);
                }
            }    
            
            //finally world parameter
            fileWriter.write(WORLD_IDX);
            fileWriter.writeFX(getGravity());
            fileWriter.writeInt(getDampingLateralFX());
            fileWriter.writeInt(getDampingRotationalFX());
            
            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);
            
        }
        catch( IOException e) 
        {
            System.out.print("Error while writing file!\n");
        }
                
    }

    public static DesignWorld loadDesignWorld(PhysicsFileReader reader, UserData userData)
    {
       int version = reader.getVersion();
       
       if ( ((version & MASK_VERSION) < VERSION_1) ||
            ((version & MASK_VERSION) > VERSION_10) )
       {
           return null;
       }
       
       DesignWorld world = new DesignWorld();
       Vector shapes = new Vector();
       Vector bodies = new Vector();
       
       int nextItem = 0;
       while( nextItem != -1)
       {           
           switch(nextItem)
           {
           case SHAPES_IDX:
               {
                   int shapeCount = reader.next();
                   for( int i = 0; i < shapeCount; i++)
                   {
                       shapes.addElement( DesignShapeStd.loadDesignShape(reader, userData));
                   }
                   if (version > VERSION_8)
                   {
                       int multiShapeCount = reader.next();                                 
                       for( int i = 0; i < multiShapeCount; i++)                            
                       {                                                               
                           shapes.addElement( DesignMultiShape.loadDesignShape(reader, userData, shapes));                
                       }                                                               
                   }
                   world.mShapeSet.registerShapes(shapes);    //do this at that point to ensure the correct order
               }
               break;
           case BODY_IDX:    
               {
                   int bodyCount = reader.next();
               
                   for( int i = 0; i < bodyCount; i++)
                   {
                       bodies.addElement(DesignBody.loadDesignBody(reader, shapes, userData));
                       world.addBody( (DesignBody) bodies.lastElement());
                   }
               }
               break;
           case LANDSCAPE_IDX:
           {
               world.setLandscape( DesignLandscape.loadDesignLandscape(reader) );               
           }
           break;
           case CONSTRAINTS_IDX:
               {
                   int constraintCount = reader.next();
                   for( int i = 0; i < constraintCount; i++)
                   {
                       world.addConstraint( DesignWorld.loadDesignConstraint(reader, version, bodies, userData));
                   }
               }
               break;
           case SCRIPTS_IDX:    
               {
                   int scriptCount = reader.next();
                   for( int i = 0; i < scriptCount; i++)
                   {
                       world.addScript( DesignScript.loadDesignScript(reader, bodies));
                   }
                   
                   int scriptBodyCount = reader.next();
                   for( int i = 0; i < scriptBodyCount; i++)
                   {
                       int index = reader.next();
                       int bodyIndex = reader.next();
                       if (bodyIndex >= 0 && bodyIndex < bodies.size() && index < scriptCount)
                       {
                           world.addScriptBody(index, (DesignBody) bodies.elementAt(bodyIndex));
                       }
                   }                   
               }
               break;
           case EVENTS_IDX:
               {
                   int eventCount = reader.next();
                   for( int i = 0; i < eventCount; i++)
                   {
                       world.addEvent( DesignPhysicsEvent.loadDesignEvent(reader, world, userData));
                   }            
               }
               break;
           case WORLD_IDX:
               {
                   world.setGravity(reader.nextVector());
                   if (reader.getVersion() > VERSION_9)
                   {
                       world.setDampingLateralFX(reader.nextInt());
                       world.setDampingRotationalFX(reader.nextInt());
                   }
                   else
                   {
                       int dampingFX = FXUtil.ONE_FX - reader.nextInt();
                       world.setDampingLateralFX(dampingFX);
                       world.setDampingRotationalFX(dampingFX);
                   }
                   
                   if (reader.getVersion() > VERSION_7)
                   {
                       String userDataString = reader.nextString();
                       if (userData != null)
                       {
                           world.mUserData = userData.createNewUserData(userDataString, UserData.TYPE_WORLD);
                       }
                   }
               }
               break;
           case PARTICLES_IDX:
               {
                   int particleCount = reader.next();
                   for( int i = 0; i < particleCount; i++)
                   {
                       world.addParticleEmitter( DesignParticleEmitter.loadDesignParticleEmitter(reader, world, userData ));
                   }            
               }
               break;
           default:
               break;
           }
           
           if ( (version & MASK_VERSION) == VERSION_1)
           {
               nextItem++;
               if (nextItem == SCRIPTS_IDX) nextItem++;	//no scripts in old version files
               if (nextItem > EVENTS_IDX)
               {
                   break;
               }
           }
           else
           {
               nextItem = reader.next();
           }
       }
       reader.close();
       
       return world;        
    }
    
    public static DesignWorld loadFromFile(File file) 
    {
        PhysicsFileReader reader = new PhysicsFileReader(file);        
        return DesignWorld.loadDesignWorld(reader, new StringUserData());
    }
    
    public static DesignWorld loadFromFile(PhysicsFileReader reader) 
    {
        DesignWorld world = DesignWorld.loadDesignWorld(reader,new StringUserData());        
        return world;
    }
    
    public static DesignConstraint loadDesignConstraint(PhysicsFileReader reader, int version, Vector bodies, UserData userData)
    {
        Constraint c = World.loadConstraint(reader, bodies, userData);
        if (c instanceof Joint)
        {
            return new DesignJoint((Joint)c, null); 
        }
        if (c instanceof Spring)
        {
            return new DesignSpring((Spring)c, null); 
        }
        if (c instanceof Motor)
        {
            return new DesignMotor((Motor)c, null); 
        }
        return null;
    }

    
    public int getNextLayer() 
    {
        return currLayer++;
    }

    public Vector getShapes() 
    {
        return getDesignShapeSet().getShapes();        
    }

    public void removeObject(DesignSelectionObject element) 
    {
        objectTreeModel.removeNodeFromParent(findModelNode(element));
        
        if (element instanceof DesignBody)
        {
            removeBody((DesignBody) element);
        }
        if (element instanceof DesignPhysicsEvent)
        {
            removeEvent((DesignPhysicsEvent) element);
        }
        if (element instanceof DesignConstraint)
        {
            removeConstraint((DesignConstraint) element);
        }
        if (element instanceof DesignParticleEmitter)
        {
            removeParticleEmitter((DesignParticleEmitter) element);
        }
        
        //check tree for cross removal (e.g: constraints when a body is removed)
        validateTree();
    }
       
    private void reorderIds()
    {
        //correct body ids;
        Body[] bodies = getBodies();
        for( int i = 0; i < getBodyCount(); i++)
        {
            ((DesignBody) bodies[i]).setId(i); 
        }        
    }
    
    /**
     * Rescale the complete world
     * @param scale
     */
    public void scale(float scale)
    {
        int scaleFX = (int) (scale * FXUtil.ONE_FX); 
        
        //1) scale shapes
        Vector shapes = mShapeSet.getShapes(); 
        for( int i = 0; i < shapes.size(); i++)
        {
            ((DesignShapeStd) shapes.get(i)).scale(scale);
        }
        //2) scale bodies
        Body[] bodies = getBodies();
        for( int i = 0; i < getBodyCount(); i++)
        {
            bodies[i].positionFX().multFX(scaleFX);
        } 
        
        //3) scale constraints
        Constraint[] constraints = getConstraints();
        for( int i = 0; i < getConstraintCount(); i++)
        {
            ((DesignConstraint) constraints[i]).scale(scale);
        } 
        
        //4) scale events
        Vector events = getEvents();
        for( int i = 0; i < events.size(); i++)
        {
            ((DesignPhysicsEvent) events.get(i)).scale(scale);
        } 
        
        //5) scale scripts
    }
    
    /**
     * Remove a shape from the world
     * Remove all bodies using the shape as well
     * @param s the constraint to remove
     */
    public void removeShape(Shape s)
    {        
        int bodyCount = getBodyCount();
        Body[] bodies = getBodies();
        for( int i = bodyCount - 1; i >= 0; i--)
        {
            if (bodies[i].shape() == s)
            {
                removeBody(bodies [i]);
                i--;
            }
        }
        getDesignShapeSet().removeShape(s);       
    }
       
    public Script getScriptBodyScript(int index) {       
        return getScripts()[getScriptIndices()[index]];
    }
    
    public Body getScriptBody(int index) {       
        return getScriptBodies()[index];
    }

    public int eventCount() {
        return getEvents().size();
    }

    public Event getEvent(int i) {
        return (Event) getEvents().elementAt(i);
    }

    public int getDesignScriptBodyCount()
    {
        return getScriptBodyCount();
    }

    public boolean isDrawParticleTrajectory()
    {
        return drawParticleTrajectory;
    }

    public void setDrawParticleTrajectory(boolean drawParticleTrajectory)
    {
        this.drawParticleTrajectory = drawParticleTrajectory;
    }

    public boolean isDrawDesignInfo()
    {
        return drawDesignInfo;
    }

    public void setDrawDesignInfo(boolean drawDesignInfo)
    {
        this.drawDesignInfo = drawDesignInfo;
    }
   
}
