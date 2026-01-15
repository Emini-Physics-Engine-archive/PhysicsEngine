package at.emini.physics2DVisualTest;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import at.emini.physics2D.Body;
import at.emini.physics2D.Event;
import at.emini.physics2D.Joint;
import at.emini.physics2D.Landscape;
import at.emini.physics2D.Motor;
import at.emini.physics2D.MultiShape;
import at.emini.physics2D.PhysicsEventListener;
import at.emini.physics2D.Script;
import at.emini.physics2D.Shape;
import at.emini.physics2D.Spring;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXMatrix;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;


public class TestVisualInteractive extends Frame 
implements MouseListener, MouseMotionListener, MouseWheelListener, PhysicsEventListener 
{

    /**
     * 
     */
    private static final long serialVersionUID = 7758973799295711046L;
    private static final boolean cDynamic = true;
    private static final boolean cStatic = false;

    private GraphicsWorld world;
    private TestSimThread simulation;
    
    private Vector bodies = new Vector();
    private Vector joints = new Vector();
    private Vector connections = new Vector();
    private Landscape landscape = new Landscape();
    private Vector restartCopy = new Vector();
    private Vector restartJointCopy = new Vector();
    private Vector restartConnectionsCopy = new Vector();
    private Landscape restartLandscape = new Landscape();
    
    private InteractiveBody movedBody = null;
    
    boolean screenMove = false;
    int xOffset = 0;
    int yOffset = 0;
    int grabX = 0;
    int grabY = 0;
    double zoom = 1.0;
    
    //Components
    private Checkbox drawVertexTrajectories;
    private Checkbox drawBodyTrajectories;
    private Checkbox drawContacts; 
    private Checkbox drawControls; 
    
    private int opacity = 255;
    private BufferedImage buffer = new BufferedImage( 1000, 1000, BufferedImage.TYPE_INT_RGB );
    
    private static final  Color myColor1 = new Color(80,  0,  0);
    private static final  Color myColor2 = new Color(80, 80,  0);
    private static final  Color myColor3 = new Color( 0, 80,  0);
    private static final  Color myColor4 = new Color( 0, 80, 80);
    private static final  Color myColor5 = new Color( 0,  0, 80);
    private static final  Color myColor6 = new Color(80,  0, 80);
        
    public TestVisualInteractive()
    {
        super("Visual Test for the Physics engine");
        
        setLocation( 100, 100);
        addWindowListener (new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) {
                dispose();
                System.exit(0);
            }
        });
             
        clearBufferImage();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        Panel control = new Panel();
        control.setLayout( new GridLayout(25, 1) );

        Button restartSzenario0 = new Button("General Test");
        restartSzenario0.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        generalTest();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario0);
        
        Button restartSzenario1 = new Button("Szenario1");
        restartSzenario1.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario1();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario1);
        
        Button restartSzenario2 = new Button("Rope");
        restartSzenario2.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario2();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario2);
        
        Button restartSzenario3 = new Button("Stack");
        restartSzenario3.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario3();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario3);
        
        Button restartSzenario4 = new Button("Bridge");
        restartSzenario4.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario4();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario4);
        
        Button restartSzenario5 = new Button("Balance");
        restartSzenario5.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario5();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario5);
        
        Button restartSzenario6 = new Button("Bucket");
        restartSzenario6.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario6();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario6);
        
        Button restartSzenario7 = new Button("Scripting");
        restartSzenario7.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario7();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario7);
        
        Button restartSzenario8 = new Button("Flow");
        restartSzenario8.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario8();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario8);
        
        Button restartSzenario9 = new Button("compound Pendulum");
        restartSzenario9.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario9();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario9);
        
        Button restartSzenario10 = new Button("Event trigger");
        restartSzenario10.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario10();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario10);
        
        Button restartSzenario11 = new Button("External Force");
        restartSzenario11.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario11();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario11);
        
        Button restartSzenario12 = new Button("Energy Bug");
        restartSzenario12.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        createSzenario12();
                        repaint();
                    } 
                }); 
        control.add(restartSzenario12);
        
        Button restart = new Button("Restart");
        restart.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                        restart();
                        repaint();
                    } 
                }); 
        control.add(restart);

        Button start = new Button("Start");
        start.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        start();
                    } 
                }); 
        control.add(start);
        
        Button tick = new Button("Tick");
        tick.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        tick();
                    } 
                }); 
        control.add(tick);
                
        Button stop = new Button("Stop");
        stop.addActionListener ( new java.awt.event.ActionListener () 
                {
                    public void actionPerformed (java.awt.event.ActionEvent evt) 
                    {
                        stop();
                    } 
                }); 
        control.add(stop);
        
        drawControls = new Checkbox("Controls", true);
        drawControls.addItemListener(new ItemListener() {
        
            public void itemStateChanged(ItemEvent e) {                
                repaint();
            }        
        });
        control.add(drawControls);
        
        drawContacts = new Checkbox("Contacts", true);
        drawContacts.addItemListener(new ItemListener() {
        
            public void itemStateChanged(ItemEvent e) {
                world.setDrawContacts(drawContacts.getState()); 
                repaint();
            }        
        });
        control.add(drawContacts);
        
        drawBodyTrajectories = new Checkbox("Body Trajectories", true);
        drawBodyTrajectories.addItemListener(new ItemListener() {
        
            public void itemStateChanged(ItemEvent e) {
                world.setDrawBodyTrajectory(drawBodyTrajectories.getState());
                repaint();
            }        
        });
        control.add(drawBodyTrajectories);
              
        drawVertexTrajectories = new Checkbox("Vertex Trajectories", false);
        drawVertexTrajectories.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                world.setDrawVertexTrajectories(drawVertexTrajectories.getState());
                repaint();
            }        
        });
        control.add(drawVertexTrajectories);
        
        add("East", control);
        
        createSzenario1();        
    }
    
    private void clearBufferImage()
    {
        Graphics g = buffer.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 1000, 1000);        
    }
    
    private void initWorld()
    {        
        world = new GraphicsWorld();
        world.setDrawContacts(drawContacts.getState());
        world.setDrawBodyTrajectory(drawBodyTrajectories.getState());
        world.setDrawVertexTrajectories(drawVertexTrajectories.getState());
        
        world.setPhysicsEventListener(this);
        
        bodies.clear();
        joints.clear();
        connections.clear();
        
        xOffset = 0;
        yOffset = 0;
        
        zoom = 1.0;
        world.setZoom(zoom);
        clearBufferImage();
    }
    
    private void addBody(InteractiveBody b)
    {
        bodies.add(b);
        world.addBody(b);
    }
    
    private void setLandscape( Landscape landscape)
    { 
        this.landscape = landscape;
        world.setLandscape(landscape);
    }
    
    private void addConnection(Spring j)
    {
        connections.add(j);
        world.addConstraint(j);
    }
    
    private void addJoint(Joint j)
    {
        joints.add(j);
        world.addConstraint(j);
    }
    
    private void generalTest()
    {
        initWorld();
        /*
        World w = new World();
        Shape box4 = Shape.createRectangle(40, 40);
        Body b1 = new Body(12, 23, box4, true);
        Body b2 = new Body(12, 46, box4, true);
        Spring j = new Spring(b1, b2, new FXVector(), new FXVector(), -1);
        
        w.addBody(b1);
        w.addBody(b2);
        w.addConstraint(j);
        
        world.addWorld(w);
        
        Shape box1 = Shape.createRectangle(40, 40);
        Shape box2 = Shape.createRectangle(100, 40);

        
        FXVector[] corners = new FXVector[1];
        corners[0] = new FXVector(0, 27);        
        Shape circleSmall = new Shape(corners);
        
        
        int width = 200;
        int height = 300;
        
        Landscape landscape = new Landscape();
        landscape.addSegment( FXVector.newVector(0, 0), FXVector.newVector(width, 0), Landscape.FACE_RIGHT);
        landscape.addSegment( FXVector.newVector(width, 0), FXVector.newVector(width, height), Landscape.FACE_RIGHT);
        landscape.addSegment( FXVector.newVector(width, height), FXVector.newVector(0, height), Landscape.FACE_RIGHT);
        landscape.addSegment( FXVector.newVector(0, height), FXVector.newVector(0, 0), Landscape.FACE_RIGHT);
        //setLandscape(landscape);
        
        //addBody(new InteractiveBody(40, 40, box1, true ) );
        addBody(new InteractiveBody(160, 160, box2, false ) );
        addBody(new InteractiveBody(150, 120, box2, true ) );
        addBody(new InteractiveBody(165, 75, box2, true ) );
        addBody(new InteractiveBody(155, 35, box2, true ) );
        addBody(new InteractiveBody(160, -5, box2, true ) );
        addBody(new InteractiveBody(165, -50, box2, true ) );
        
        addBody(new InteractiveBody(165, -80, circleSmall, true ) );
        addBody(new InteractiveBody(105, -80, circleSmall, true ) );
        
        float gravityAngle = 47.0f;
        int gravityAngleFX = (int) (gravityAngle * FXUtil.PI_2FX / 180f);
        FXMatrix rotationMatrix = FXMatrix.createRotationMatrix(gravityAngleFX);
        //world.setGravity( rotationMatrix.mult(new FXVector(0, FXUtil.ONE_FX * 100)) );       
        */
        
        Shape circle = Shape.createCircle(16);
        circle.setElasticity(80);
        circle.setFriction(90);
        InteractiveBody ball = new InteractiveBody(100, 100, circle, true);
        InteractiveBody ball2 = new InteractiveBody(200, 200, circle, true);
        
        int screenWidth = 480;
        int screenHeight = 800;
        // create boundary (to avoid ball thrown out of the screen!)
        Shape rectangleH = Shape.createRectangle(screenWidth, 5);
        rectangleH.setElasticity(80);
        rectangleH.setFriction(90);
        Shape rectangleV = Shape.createRectangle(5, screenHeight);
        rectangleV.setElasticity(80);
        rectangleV.setFriction(90);

        InteractiveBody barTop = new InteractiveBody(screenWidth/2, 0, rectangleH, false);
        InteractiveBody barBottom = new InteractiveBody(screenWidth/2, screenHeight, rectangleH, false);
        InteractiveBody barLeft = new InteractiveBody(0, screenHeight/2, rectangleV, false);
        InteractiveBody barRight = new InteractiveBody(screenWidth, screenHeight/2, rectangleV, false);

        
        //add all stuff to world
        world.addBody(ball);
        world.addBody(ball2);

        world.addBody(barTop);
        world.addBody(barBottom);
        world.addBody(barLeft);
        world.addBody(barRight);
        
        
        FXVector f = new FXVector();

//      f.xFX = (int) (-gX * 100 * Math.pow(2, FXUtil.DECIMAL));
//      f.yFX = (int) (gY * 100 * Math.pow(2, FXUtil.DECIMAL));
        f.xFX = (int) (0 * 100 * Math.pow(2, FXUtil.DECIMAL));
        f.yFX = (int) (10 * 100 * Math.pow(2, FXUtil.DECIMAL));
//      Log.e("FX", "FX(X): " + String.valueOf(gX) + "FX(Y): "
//              + String.valueOf(gY));
        world.setGravity(f);
        
        simulation = new TestSimThread(world, this);
        simulation.start();       
    }
    
    private void createSzenario1()
    {        
        //current test
        initWorld();
        //world.setGravity(0);

        /*Shape rectangle = Shape.createRectangle(40,120);
        Shape line = Shape.createRectangle(440,20);

        //clockwise - real world (screen counterclockwise)
        FXVector[] octagonCorners = new FXVector[8];
        octagonCorners[0] = new FXVector(-40 << FXUtil.DECIMAL, 20 << FXUtil.DECIMAL);
        octagonCorners[1] = new FXVector(-20 << FXUtil.DECIMAL, 40 << FXUtil.DECIMAL);
        octagonCorners[2] = new FXVector( 20 << FXUtil.DECIMAL, 40 << FXUtil.DECIMAL);
        octagonCorners[3] = new FXVector( 40 << FXUtil.DECIMAL, 20 << FXUtil.DECIMAL);
        octagonCorners[4] = new FXVector( 40 << FXUtil.DECIMAL,-20 << FXUtil.DECIMAL);
        octagonCorners[5] = new FXVector( 20 << FXUtil.DECIMAL,-40 << FXUtil.DECIMAL);
        octagonCorners[6] = new FXVector(-20 << FXUtil.DECIMAL,-40 << FXUtil.DECIMAL);
        octagonCorners[7] = new FXVector(-40 << FXUtil.DECIMAL,-20 << FXUtil.DECIMAL);
        
        Shape octagon = new Shape(octagonCorners);
        
        
        //addBody( new InteractiveBody( 250, 500, line, 100, cStatic) );
        
        InteractiveBody body1 = new InteractiveBody( 120, 300, rectangle, 100, cStatic);
        body1.setRotationDeg(90);
        addBody(body1);
        
        InteractiveBody body2 = new InteractiveBody( 180, 180, rectangle, 2, cDynamic);
        addBody(body2);
        
        InteractiveBody body3 = new InteractiveBody( 400, 100, octagon, 2, cDynamic);
        addBody(body3);
        
        InteractiveBody body4 = new InteractiveBody( 50, 100, octagon, 1, cDynamic);
        addBody(body4);
        
        //addJoint( new Joint(body2, body3, new FXVector(), new FXVector(), -1) );
        
        addJoint( new Joint(body2, body1, new FXVector(0, -60<< FXUtil.DECIMAL), new FXVector(), -1));
        */
      //clockwise - real world (screen counterclockwise)
        
        FXVector[] octagonCorners = new FXVector[8];
        octagonCorners[0] = new FXVector(-40 << FXUtil.DECIMAL, 20 << FXUtil.DECIMAL);
        octagonCorners[1] = new FXVector(-20 << FXUtil.DECIMAL, 40 << FXUtil.DECIMAL);
        octagonCorners[2] = new FXVector( 20 << FXUtil.DECIMAL, 40 << FXUtil.DECIMAL);
        octagonCorners[3] = new FXVector( 40 << FXUtil.DECIMAL, 20 << FXUtil.DECIMAL);
        octagonCorners[4] = new FXVector( 40 << FXUtil.DECIMAL,-20 << FXUtil.DECIMAL);
        octagonCorners[5] = new FXVector( 20 << FXUtil.DECIMAL,-40 << FXUtil.DECIMAL);
        octagonCorners[6] = new FXVector(-20 << FXUtil.DECIMAL,-40 << FXUtil.DECIMAL);
        octagonCorners[7] = new FXVector(-40 << FXUtil.DECIMAL,-20 << FXUtil.DECIMAL);        
        Shape octagon = new Shape(octagonCorners);
        
      //triangle
        FXVector[] triangleCorners = new FXVector[3];
        triangleCorners[0] = new FXVector( 00 << FXUtil.DECIMAL,  70 << FXUtil.DECIMAL);
        triangleCorners[1] = new FXVector( 20 << FXUtil.DECIMAL,  70 << FXUtil.DECIMAL);
        triangleCorners[2] = new FXVector(  0 << FXUtil.DECIMAL,-150 << FXUtil.DECIMAL);
        
        FXVector[] triangleCorners2 = new FXVector[3];
        triangleCorners2[0] = new FXVector(  0 << FXUtil.DECIMAL, 150 << FXUtil.DECIMAL);
        triangleCorners2[1] = new FXVector( 20 << FXUtil.DECIMAL, -70 << FXUtil.DECIMAL);
        triangleCorners2[2] = new FXVector( 00 << FXUtil.DECIMAL, -70 << FXUtil.DECIMAL);
        
        Shape triangle = new Shape(triangleCorners);
        triangle.correctCentroid();
                
        //some shape
        FXVector[] someShapeCorners = new FXVector[5];
        someShapeCorners[0] = new FXVector(-40 << FXUtil.DECIMAL, 40 << FXUtil.DECIMAL);
        someShapeCorners[1] = new FXVector(-30 << FXUtil.DECIMAL,-40 << FXUtil.DECIMAL);
        someShapeCorners[2] = new FXVector(  0 << FXUtil.DECIMAL,-60 << FXUtil.DECIMAL);
        someShapeCorners[3] = new FXVector( 30 << FXUtil.DECIMAL,-40 << FXUtil.DECIMAL);
        someShapeCorners[4] = new FXVector( 40 << FXUtil.DECIMAL, 40 << FXUtil.DECIMAL);
        Shape someShape = new Shape(someShapeCorners);
        
        Shape circle = Shape.createCircle(20);
        
        Shape rectangle = Shape.createRectangle(1000,50);
        Shape smallrectangle = Shape.createRectangle(100,50);
        Shape box = Shape.createRectangle(50,50);

        InteractiveBody body0 = new InteractiveBody( 200, 500, rectangle, cStatic);
        body0.setRotationDeg(25);
        addBody(body0);

        InteractiveBody bodyb = new InteractiveBody( 100, 500, rectangle, cStatic);
        bodyb.setRotationDeg(90);
        //addBody(bodyb);
        
        InteractiveBody body1 = new InteractiveBody( 200, 405, box, cDynamic);
        body1.setVelocityFX(new FXVector(0, 2000 << FXUtil.DECIMAL));
        //addBody(body1);
        
        InteractiveBody body2 = new InteractiveBody(  0, 800, smallrectangle, cDynamic);
        //addBody(body2);
        //addConnection( new DistanceJoint(body2, body0, new FXVector( -40 << FXUtil.DECIMAL, 0 ), new FXVector(- 40 << FXUtil.DECIMAL, 0 ), -1) );
        //addJoint( new Joint(body2, body0, new FXVector(  40 << FXUtil.DECIMAL, 0 ), new FXVector(  40 << FXUtil.DECIMAL, 0 ), -1) );
                
        InteractiveBody body3 = new InteractiveBody(  300, 300, octagon, cDynamic);
        //addBody(body3);
        
        InteractiveBody body4 = new InteractiveBody(  300, 400, triangle, cDynamic);
        //addBody(body4);
        
        InteractiveBody body5 = new InteractiveBody( 100, 455, box, cDynamic);
        //addBody(body5);
        
        InteractiveBody body6 = new InteractiveBody( 400, 400, someShape, cDynamic);
        body6.setRotationDeg(90);
        //addBody(body6);
        Script body6Script = new Script(true);
        body6Script.addElement(Script.VELOCITY, 10, 2, 20);
        //world.addConstraint(body6Script);
        
        InteractiveBody body8 = new InteractiveBody( 200, 150, circle, cDynamic);
        //addBody(body8);
        
        int one2FX = 1 << FXUtil.DECIMAL2;
        //Motor motor1 = new Motor(body8, one2FX * 5, FXUtil.ONE_FX * 5);
        Motor motor1 = new Motor(body8, -FXUtil.ONE_FX * 20, 0,  FXUtil.ONE_FX * 10);
        //world.addConstraint(motor1);
        
        //Landscape        
        Landscape landscape = new Landscape();
        landscape.addSegment(new FXVector(0,0), new FXVector(FXUtil.ONE_FX * 100,0), Landscape.FACE_NONE);
        landscape.addSegment(new FXVector(0,0), new FXVector(-FXUtil.ONE_FX * 100,0), Landscape.FACE_RIGHT);
        landscape.addSegment(new FXVector(FXUtil.ONE_FX *100,0), new FXVector(FXUtil.ONE_FX * 100,- FXUtil.ONE_FX * 100), Landscape.FACE_NONE);
        landscape.getShape().setFriction(100);
        setLandscape(landscape);
        
        Shape ms1 = Shape.createRectangle(60, 20);
        Shape ms2 = Shape.createRectangle(20, 60);
        
        Vector shapes = new Vector();
        shapes.add(ms1);
        shapes.add(ms2);
        
        Shape multiShape1 = new MultiShape(shapes);
        
        InteractiveBody body10 = new InteractiveBody( 200, 200, multiShape1, cDynamic);
        addBody(body10);
        InteractiveBody body11 = new InteractiveBody( 270, 200, multiShape1, cDynamic);
        addBody(body11);
        InteractiveBody body12 = new InteractiveBody( 250, 140, multiShape1, cDynamic);
        addBody(body12);
        InteractiveBody body13 = new InteractiveBody( 140, 140, multiShape1, cDynamic);
        addBody(body13);
        
        
        
        simulation = new TestSimThread(world, this);
        simulation.start();
    }
    
    private void createSzenario2()
    {
        //Rope
        initWorld();

        Shape rectangle = Shape.createRectangle(60,20);
        Shape box = Shape.createRectangle(10,10);
        Shape rod = Shape.createRectangle(50,10);
        rod.setMass(10);

        ///*
        InteractiveBody body0 = new InteractiveBody( 200, 50, rectangle, cStatic);
        addBody(body0);
        
        InteractiveBody body1 = new InteractiveBody( 180, 70, box, cDynamic, myColor1);
        addBody(body1);
        
        InteractiveBody body2 = new InteractiveBody( 160, 90, box, cDynamic, myColor2);
        addBody(body2);
        
        InteractiveBody body3 = new InteractiveBody( 140, 100, box, cDynamic, myColor3);
        addBody(body3);
        
        InteractiveBody body4 = new InteractiveBody( 120, 110, box, cDynamic, myColor4);
        addBody(body4);
        
        InteractiveBody body5 = new InteractiveBody( 100, 100, box, cDynamic, myColor5);
        addBody(body5);
                       
        addConnection( new Spring(body0, body1, new FXVector(), new FXVector(), -1) );
        addConnection( new Spring(body1, body2, new FXVector(), new FXVector(), -1) );
        addConnection( new Spring(body2, body3, new FXVector(), new FXVector(), -1) );
        addConnection( new Spring(body3, body4, new FXVector(), new FXVector(), -1) );
        addConnection( new Spring(body4, body5, new FXVector(), new FXVector(), -1) );
        
        InteractiveBody body0b = new InteractiveBody( 500, 50, rectangle, cStatic);
        addBody(body0b);
        
        InteractiveBody body1b = new InteractiveBody( 480, 50, rod, cDynamic, myColor1);
        addBody(body1b);
        
        InteractiveBody body2b = new InteractiveBody( 440, 50, rod, cDynamic, myColor2);
        addBody(body2b);
        
        InteractiveBody body3b = new InteractiveBody( 400, 50, rod, cDynamic, myColor3);
        addBody(body3b);
        
        InteractiveBody body4b = new InteractiveBody( 360, 50, rod, cDynamic, myColor4);
        addBody(body4b);
        
        InteractiveBody body5b = new InteractiveBody( 320, 50, rod, cDynamic, myColor5);
        addBody(body5b);
                       
        Joint j1 = new Joint(body0b, body1b, new FXVector(0, 0), new FXVector(20 << FXUtil.DECIMAL, 0), false);
        Joint j2 = new Joint(body1b, body2b, new FXVector(-20 << FXUtil.DECIMAL, 0), new FXVector(20 << FXUtil.DECIMAL, 0), false);
        Joint j3 = new Joint(body2b, body3b, new FXVector(-20 << FXUtil.DECIMAL, 0), new FXVector(20 << FXUtil.DECIMAL, 0), false);
        Joint j4 = new Joint(body3b, body4b, new FXVector(-20 << FXUtil.DECIMAL, 0), new FXVector(20 << FXUtil.DECIMAL, 0), false);
        Joint j5 = new Joint(body4b, body5b, new FXVector(-20 << FXUtil.DECIMAL, 0), new FXVector(20 << FXUtil.DECIMAL, 0), false);
        
        j1.setCollisionLayer(1);
        j2.setCollisionLayer(2);
        j3.setCollisionLayer(3);
        j4.setCollisionLayer(4);
        j5.setCollisionLayer(5);
        
        addJoint(j1);
        addJoint(j2);
        addJoint(j3);
        addJoint(j4);
        addJoint(j5);
                
        simulation = new TestSimThread(world, this);
        simulation.start();        
    }
    
    private void createSzenario3()
    {        
        //Stack
        initWorld();
        //world.setGravity(0);
        
        int boxSize = 40;
        Shape rectangle = Shape.createRectangle(4000,10);
        Shape box = Shape.createRectangle(boxSize,boxSize);

        InteractiveBody body0 = new InteractiveBody( 0, 475, rectangle, false);
        addBody(body0);
        
        InteractiveBody body1 = new InteractiveBody( -2000, 0, rectangle, false);
        body1.setRotationDeg(90);
        addBody(body1);
        
        InteractiveBody body2 = new InteractiveBody( 2000, 0, rectangle, false);
        body2.setRotationDeg(90);
        addBody(body2);
        
        for( int i = 0; i < 100; i++)
        {
            InteractiveBody body = new InteractiveBody( 200, 475 - boxSize / 2 - i * (boxSize), box, true);
            addBody(body);                        
        }   
                
        simulation = new TestSimThread(world, this);
        simulation.start();        
    }
    
        
    private void createSzenario4()
    {
        //Bridge
        initWorld();

        Shape anchor = Shape.createRectangle(100,20);
        Shape rect = Shape.createRectangle(40,10);

        InteractiveBody body0 = new InteractiveBody( 50, 250, anchor, cStatic);
        addBody(body0);
                
        InteractiveBody body1 = new InteractiveBody( 150, 240, rect, cDynamic);
        addBody(body1);
        
        InteractiveBody body2 = new InteractiveBody( 220, 210, rect, cDynamic);
        addBody(body2);
        
        InteractiveBody body3 = new InteractiveBody( 290, 160, rect, cDynamic);
        addBody(body3);
        
        InteractiveBody body4 = new InteractiveBody( 360, 130, rect, cDynamic);
        addBody(body4);
        
        InteractiveBody body5 = new InteractiveBody( 430, 90, rect, cDynamic);
        addBody(body5);
                       
        InteractiveBody body6 = new InteractiveBody( 500, 150, anchor, cStatic);
        addBody(body6);
        
        
        addConnection( new Spring(body0, body1, new FXVector(50 << FXUtil.DECIMAL, 0), new FXVector(-20 << FXUtil.DECIMAL, 0), -1) );
        addConnection( new Spring(body1, body2, new FXVector(20 << FXUtil.DECIMAL, 0), new FXVector(-20 << FXUtil.DECIMAL, 0), -1) );
        addConnection( new Spring(body2, body3, new FXVector(20 << FXUtil.DECIMAL, 0), new FXVector(-20 << FXUtil.DECIMAL, 0), -1) );
        addConnection( new Spring(body3, body4, new FXVector(20 << FXUtil.DECIMAL, 0), new FXVector(-20 << FXUtil.DECIMAL, 0), -1) );
        addConnection( new Spring(body4, body5, new FXVector(20 << FXUtil.DECIMAL, 0), new FXVector(-20 << FXUtil.DECIMAL, 0), -1) );
        addConnection( new Spring(body5, body6, new FXVector(20 << FXUtil.DECIMAL, 0), new FXVector(-50 << FXUtil.DECIMAL, 0), -1) );
                
        simulation = new TestSimThread(world, this);
        simulation.start();        
    }
    
    private void createSzenario5()
    {
        //Balance
        initWorld();

        //clockwise - real world (screen counterclockwise)
        FXVector[] triangleCorners = new FXVector[3];
        triangleCorners[0] = new FXVector(-40 << FXUtil.DECIMAL, 20 << FXUtil.DECIMAL);
        triangleCorners[1] = new FXVector(  0 << FXUtil.DECIMAL,-40 << FXUtil.DECIMAL);
        triangleCorners[2] = new FXVector( 40 << FXUtil.DECIMAL, 20 << FXUtil.DECIMAL);
        
        Shape triangle = new Shape(triangleCorners);
        
        Shape line1 = Shape.createRectangle(400,20);
        line1.setElasticity(0);
        Shape line2 = Shape.createRectangle(400,10);
        line2.setMass(10);
        line2.setFriction(10);
        
        Shape box1 = Shape.createRectangle(40,40);
        box1.setMass(2);
        box1.setFriction(10);
        
        Shape box2 = Shape.createRectangle(40,40);
        box2.setMass(5);
        box2.setFriction(10);
        
        Shape pedestal = Shape.createRectangle(100,100);
        
        InteractiveBody body0 = new InteractiveBody( 250, 400, line1, cStatic);
        addBody(body0);
                
        //InteractiveBody body1 = new InteractiveBody( 200, 370, triangle, 1, cStatic);
        //addBody(body1);
        //InteractiveBody body1 = new InteractiveBody( 200, 400, pedestal, 1, cStatic);
        //body1.setRotationDeg(45);
        //addBody(body1);
        
        InteractiveBody body2 = new InteractiveBody( 250, 360, line2, cDynamic);
        addBody(body2);
        Joint fix = new Joint(body0, body2, new FXVector(0, -40 << FXUtil.DECIMAL), new FXVector(), false);
        addJoint(fix);
        
        InteractiveBody body3 = new InteractiveBody( 350, 270, box1, cDynamic);
        addBody(body3);
        
        InteractiveBody body4 = new InteractiveBody( 150, 50, box2, cDynamic);
        addBody(body4);
        
        //addJoint(new Joint(body1, body2, new FXVector(), new FXVector(), -1));
                  
        simulation = new TestSimThread(world, this);
        simulation.start();        
    }

    private void createSzenario6()
    {        
        //Bucket
        initWorld();
        
        //world.setSimulationArea(0, 400);
        
        int size = 45;
        
        Shape rectangle = Shape.createRectangle(150,50);
        Shape box = Shape.createRectangle(size, size);
        FXVector[] triangleCorners = new FXVector[4];
        
        triangleCorners[0] = new FXVector(-20 << FXUtil.DECIMAL, 10 << FXUtil.DECIMAL);
        triangleCorners[1] = new FXVector(  0 << FXUtil.DECIMAL,-30 << FXUtil.DECIMAL);
        triangleCorners[2] = new FXVector( 20 << FXUtil.DECIMAL, 10 << FXUtil.DECIMAL);
        triangleCorners[3] = new FXVector( 15 << FXUtil.DECIMAL, 20 << FXUtil.DECIMAL);
        //triangleCorners[4] = new FXVector( -5 << FXUtil.DECIMAL, 15 << FXUtil.DECIMAL);
        
        Shape triangle = new Shape(triangleCorners);
        
        
        InteractiveBody body0a = new InteractiveBody( 125, 500, rectangle, cStatic);
        addBody(body0a);
        InteractiveBody body0d = new InteractiveBody( 275, 500, rectangle, cStatic);
        addBody(body0d);

        InteractiveBody body0b = new InteractiveBody( -35, 355, rectangle, cStatic);
        body0b.setRotationDeg(45);
        addBody(body0b);
        
        InteractiveBody body0e = new InteractiveBody( 75, 455, rectangle, cStatic);
        body0e.setRotationDeg(45);
        addBody(body0e);
        
        InteractiveBody body0c = new InteractiveBody( 325, 455, rectangle, cStatic);
        body0c.setRotationDeg(315);
        addBody(body0c);
        
        InteractiveBody body0f = new InteractiveBody( 435, 355, rectangle, cStatic);
        body0f.setRotationDeg(315);
        addBody(body0f);
        
        //InteractiveBody body0d = new InteractiveBody( 200, 200, rectangle, 1000, cStatic);
        //addBody(body0d);
        
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 20; j++)
            {
                InteractiveBody body = new InteractiveBody( 50 + (size + 5) * i, 350 - (size + 5) * j, triangle, cDynamic);
                addBody(body);
            }            
        }
       
        
        simulation = new TestSimThread(world, this);
        simulation.start();        
    }
    
    private void createSzenario7()
    {        
        //Scriptiong
        initWorld();

        Shape rectangle = Shape.createRectangle(200,50);
        Shape box = Shape.createRectangle(30,30);

        InteractiveBody body0a = new InteractiveBody( 200, 500, rectangle, cStatic);
        addBody(body0a);

        InteractiveBody body0b = new InteractiveBody( 50, 400, rectangle, cStatic);
        body0b.setRotationDeg(45);
        addBody(body0b);
        
        InteractiveBody body0c = new InteractiveBody( 350, 400, rectangle, cStatic);
        body0c.setRotationDeg(315);
        addBody(body0c);
        
        InteractiveBody body0d = new InteractiveBody( 200, 100, rectangle, cStatic);
        addBody(body0d);
        
        InteractiveBody body2 = new InteractiveBody( 100, 50, box, cDynamic);
        addBody(body2);
        
        InteractiveBody body3 = new InteractiveBody( 150, 50, box, cDynamic);
        addBody(body3);
        
        InteractiveBody body4 = new InteractiveBody( 200, 50, box, cDynamic);
        addBody(body4);
        
        InteractiveBody body5 = new InteractiveBody( 250, 50, box, cDynamic);
        addBody(body5);
        
        Script s1 = new Script(true);
        s1.addElement(Script.NONE, 0, 100);
        s1.addElement(Script.ROTATIONAL_VELOCITY, -2, 100);
        s1.addElement(Script.NONE, 0, 100);
        s1.addElement(Script.VELOCITY, 1, 0, 500);
        s1.addElement(Script.VELOCITY, 0, 1, 500);
        world.addScript(s1);
        s1.applyToBody(body5, world);
        
        Event event1 = Event.createBodyEvent( null, null, Event.TYPE_BODY_VELOCITY, 10, 20, 0, 0);
        Event event2 = Event.createBodyEvent(body4, null, Event.TYPE_BODY_COLLISION, 1, 0, 0, 0);
        world.addEvent(event1);
        world.addEvent(event2);
                
        simulation = new TestSimThread(world, this);
        simulation.start();
    }
    
    private void createSzenario8()
    {        
        //flow
        initWorld();

        Shape rectangle = Shape.createRectangle(400,50);
        Shape box = Shape.createRectangle(30,30);
        FXVector[] triangleCorners = new FXVector[3];
        
        triangleCorners[0] = new FXVector(-20 << FXUtil.DECIMAL, 10 << FXUtil.DECIMAL);
        triangleCorners[1] = new FXVector(  0 << FXUtil.DECIMAL,-20 << FXUtil.DECIMAL);
        triangleCorners[2] = new FXVector( 20 << FXUtil.DECIMAL, 10 << FXUtil.DECIMAL);
        
        Shape triangle = new Shape(triangleCorners);
        
        
        InteractiveBody body0b = new InteractiveBody( 100, 300, rectangle, cStatic);
        body0b.setRotationDeg(45);
        addBody(body0b);
        
        InteractiveBody body0c = new InteractiveBody( 400, 600, rectangle, cStatic);
        body0c.setRotationDeg(315);
        addBody(body0c);
        
        for(int i = 0; i < 600; i++)
        {
            InteractiveBody body = new InteractiveBody( 50, 180 - 35 * i, box, cDynamic);
            addBody(body);
                        
        }
        
                
        simulation = new TestSimThread(world, this);
        simulation.start();        
    }
    
    
    private void createSzenario9()
    {
        //compound Pendulum
        initWorld();

        Shape anchor = Shape.createRectangle(200,20);
        Shape box = Shape.createRectangle(20,20);

        InteractiveBody body0 = new InteractiveBody( 200, 250, anchor, cStatic);
        addBody(body0);
                
        InteractiveBody body1 = new InteractiveBody( 150, 400, box, cDynamic);
        addBody(body1);
        
        InteractiveBody body2 = new InteractiveBody( 250, 400, box, cDynamic);
        addBody(body2);
       
       
        addConnection( new Spring(body0, body1, new FXVector(-50 << FXUtil.DECIMAL, 0), new FXVector( 0 << FXUtil.DECIMAL, 0), -1) );
        addConnection( new Spring(body0, body2, new FXVector( 50 << FXUtil.DECIMAL, 0), new FXVector( 0 << FXUtil.DECIMAL, 0), -1) );
        
        Spring spring = new Spring(body1, body2, new FXVector( 0 << FXUtil.DECIMAL, 0), new FXVector( 0 << FXUtil.DECIMAL, 0), -1);
        spring.setCoefficient(5);
        addConnection( spring );
                
        simulation = new TestSimThread(world, this);
        simulation.start();        
    }
    
    private void createSzenario10()
    {
        //compound Pendulum
        initWorld();

        Shape box = Shape.createRectangle(20,20);

        InteractiveBody body1 = new InteractiveBody( 150, 400, box, cDynamic);
        addBody(body1);

        InteractiveBody body2 = new InteractiveBody( 250, 400, box, cDynamic);
        addBody(body2);

        InteractiveBody body3 = new InteractiveBody( 250, 600, box, cStatic);
        addBody(body3);
        
        Body collisionBody = new Body(160, 450, box, cStatic);
        Event event = Event.createBodySensorEvent(null, null, collisionBody);
        world.addEvent(event);
        
        Event event2 = Event.createCollisionRelativeEvent(body2, null, 40, 140);
        world.addEvent(event2);
                
        simulation = new TestSimThread(world, this);
        simulation.start();
           
    }
    
    private void createSzenario11()
    {
        //external force
        initWorld();

        Shape box = Shape.createRectangle(20,20);

        InteractiveBody body1 = new InteractiveBody( 150, 200, box, cDynamic);
        addBody(body1);
        
        InteractiveBody body2 = new InteractiveBody( 155, 400, box, cDynamic);
        addBody(body2);
        
        InteractiveBody body3 = new InteractiveBody( 300, 200, box, cDynamic);
        body3.applyForce(FXVector.newVector(1000, 0), world.getTimestepFX());
        addBody(body3);
        
        
        BalloonForce force = new BalloonForce(body2, FXVector.newVector(0, -250));
        world.addExternalForce(force);
        
        simulation = new TestSimThread(world, this);
        simulation.start();
           
    }
    
    private void createSzenario12() {
        
        initWorld();
        
        world.setGravity(0);
        world.setDampingLateralFX(FXUtil.divideFX(5, 100));


        //calculate positions for the border
        int w = 256;
        int h = 256;
        
        int x[] = {w / 3, 2 * w / 3,     w,         w, 2 * w / 3, w / 3,         0,     0 };
        int y[] = {    0,         0, h / 3, 2 * h / 3,         h,     h, 2 * h / 3, h / 3 };
        
        Landscape landscape = new Landscape();
        for( int i = 0 , j = 7; i < 8; j=i, i++)
        {
            //create a static line = landscape object
            //the FXVector uses fixpoint math integers, they have to be shifted to the left by FXUtil.DECIMAL 
            landscape.addSegment(new FXVector(x[i] << FXUtil.DECIMAL, y[i] << FXUtil.DECIMAL),
                                 new FXVector(x[j] << FXUtil.DECIMAL, y[j] << FXUtil.DECIMAL),
                                 Landscape.FACE_NONE);
            //and add it to the world
        }
     //   world.setLandscape(landscape);
  
        //helper variables for better readability
        final boolean isStatic = false;
        final boolean isDynamic = true;
        
        //create the static diamond in the center
        /*Shape square = Shape.createRegularPolygon(20, 4);
        Body diamond = new Body(w / 2, h / 2, square, isStatic);
        diamond.setRotationDeg(45);         //we turn it by 45 degrees
        world.addBody(diamond);             //and add it to the world
        */
        //create some moving objects
        Shape circle = Shape.createCircle(10);


        circle.setElasticity(100);
        circle.setMass(10);
        addBody(new InteractiveBody(1 * w /10, 1 * h / 10, circle, isDynamic) );
        addBody(new InteractiveBody(2 * w /10, 2 * h / 10, circle, isDynamic) );
        addBody(new InteractiveBody(3 * w /10, 3 * h / 10, circle, isDynamic) );
        addBody(new InteractiveBody(4 * w /10, 4 * h / 10, circle, isDynamic) );
        addBody(new InteractiveBody(5 * w /10, 5 * h / 10, circle, isDynamic) );
        addBody(new InteractiveBody(6 * w /10, 6 * h / 10, circle, isDynamic) );
        addBody(new InteractiveBody(7 * w /10, 7 * h / 10, circle, isDynamic) );
        addBody(new InteractiveBody(8 * w /10, 8 * h / 10, circle, isDynamic) );
        addBody(new InteractiveBody(9 * w /10, 9 * h / 10, circle, isDynamic) );

        addBody(new InteractiveBody(6 * w /10, 9 * h / 10, circle, isDynamic) );


        FXVector myAccel = new FXVector();
        myAccel.xFX = -1200 << FXUtil.DECIMAL;
        myAccel.yFX = -5300 << FXUtil.DECIMAL;

        world.getBodies()[0].applyAcceleration(myAccel, world.getTimestepFX());
      
        simulation = new TestSimThread(world, this);
        simulation.start();           
    }
    
    public void paint(Graphics graphics)
    {
        Graphics g = buffer.getGraphics();
        if (opacity >= 255)
        {
            //g.clearRect(0, 0, 1000, 1000);
            g.setColor(Color.white);
            g.fillRect(0, 0, 1000, 1000);
        }
        else
        {
            Graphics2D g2 = (Graphics2D) g;
            Color c = new Color(255,255,255,opacity);
            
            g2.setColor(c);
            g2.fillRect(0, 0, 1000, 1000);
        }       
        g.translate(-xOffset, -yOffset);
                
        world.draw(g);
        
        //Graphics g = simPane.getGraphics();
        if (drawControls.getState())
        {
            for(int i = 0; i < bodies.size(); i++)
            {
                InteractiveBody body = (InteractiveBody) bodies.elementAt(i); 
                body.drawInteractives(g, zoom);
            }
        }
        
        graphics.drawImage(buffer, 0, 0, null);
    }
    
    private void start()
    {
        if (simulation != null)
        {
            restartCopy.clear();
            restartJointCopy.clear();
            restartConnectionsCopy.clear();
            restartLandscape = new Landscape();
            for(int i = 0; i < bodies.size(); i++)
            {
                InteractiveBody body = (InteractiveBody) bodies.elementAt(i); 
                InteractiveBody copy = (InteractiveBody) body.copy(); 
                restartCopy.add( copy );                
            }
            for(int i = 0; i < connections.size(); i++)
            {
                Spring joint = (Spring) connections.elementAt(i);
                int body1Index = bodies.indexOf(joint.getBody1());
                int body2Index = bodies.indexOf(joint.getBody2());
                Spring copy = new Spring((Body) restartCopy.get(body1Index), (Body) restartCopy.get(body2Index),
                                       new FXVector(joint.getRawPoint1()), new FXVector(joint.getRawPoint2()), joint.getDistance()); 
                restartConnectionsCopy.add( copy );                
            }
            for(int i = 0; i < joints.size(); i++)
            {
                Joint joint = (Joint) joints.elementAt(i);
                int body1Index = bodies.indexOf(joint.getBody1());
                int body2Index = bodies.indexOf(joint.getBody2());
                Joint copy = new Joint((Body) restartCopy.get(body1Index), (Body) restartCopy.get(body2Index),
                                       new FXVector(joint.getRawPoint1()), new FXVector(joint.getRawPoint2()), false); 
                restartJointCopy.add( copy );                
            }
            
            restartLandscape = landscape.copy();
                        
            simulation.restart();
        }
    }
    
    private void restart()
    {
        if (restartCopy.size() == 0)
        {
            return;            
        }
        
        if (simulation != null)
        {
            simulation.end();
        }
        
        initWorld();
        
        for(int i = 0; i < restartCopy.size(); i++)
        {
            InteractiveBody body = (InteractiveBody) restartCopy.elementAt(i); 
            InteractiveBody copy = (InteractiveBody) body.copy(); 
            bodies.add( copy );
            world.addBody( copy );
        }
        for(int i = 0; i < restartJointCopy.size(); i++)
        {
            Joint joint = (Joint) restartJointCopy.elementAt(i);        
            int body1Index = restartCopy.indexOf(joint.getBody1());
            int body2Index = restartCopy.indexOf(joint.getBody2());
            Joint copy = new Joint((Body)bodies.get(body1Index), (Body)bodies.get(body2Index),
                                   new FXVector(joint.getRawPoint1()), new FXVector(joint.getRawPoint2()), false); 
            
            joints.add( copy );
            world.addConstraint( copy );
        }
        for(int i = 0; i < restartConnectionsCopy.size(); i++)
        {
            Spring joint = (Spring) restartConnectionsCopy.elementAt(i);        
            int body1Index = restartCopy.indexOf(joint.getBody1());
            int body2Index = restartCopy.indexOf(joint.getBody2());
            Spring copy = new Spring((Body)bodies.get(body1Index), (Body)bodies.get(body2Index),
                                   new FXVector(joint.getRawPoint1()), new FXVector(joint.getRawPoint2()), joint.getDistance()); 
            
            connections.add( copy );
            world.addConstraint( copy );
        }
        
        landscape = restartLandscape.copy();
        world.setLandscape(restartLandscape);
        
        simulation = new TestSimThread(world, this);
        simulation.start();
    }
    
    private void stop()
    {
        if (simulation != null)
        {
            simulation.end();
            paint(getGraphics());
        }
    }
    
    private void tick()
    {
        if (simulation != null && simulation.isStopped())
        {
            simulation.tick();
            paint(getGraphics());
        }
    }
    

    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) 
    {
        FXVector mousePos = new FXVector((int)((e.getX() + xOffset) / zoom) << FXUtil.DECIMAL, (int) ((e.getY() +yOffset)/zoom) << FXUtil.DECIMAL);
        movedBody = null;
        for(int i = 0; i < bodies.size(); i++)
        {
            InteractiveBody body = (InteractiveBody) bodies.elementAt(i); 
            if (body.canMove(mousePos) || body.canRotate(mousePos))
            {
                movedBody = body;
                movedBody.setAction( mousePos);
         
                paint(getGraphics());
                
                break;
            }
        }
        if (movedBody == null)
        {
            screenMove = true;
            grabX = e.getX();
            grabY = e.getY();
        }
    }
    
    public void mouseReleased(MouseEvent e) 
    {
        if (movedBody != null)
        {
            movedBody.unsetAction();
            movedBody = null;
            
            paint(getGraphics());
        }
        screenMove = false;
        grabX = 0;
        grabY = 0;
    }
    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) 
    {
        if (movedBody != null) 
        {
            FXVector newPos = new FXVector((int) ((e.getX() + xOffset) / zoom) << FXUtil.DECIMAL, (int) ((e.getY() + yOffset)/zoom) << FXUtil.DECIMAL);
            movedBody.makeMove(newPos);
            world.collisionUpdate();
            paint(getGraphics());
        }
        
        if (screenMove)
        {
            xOffset += (grabX - e.getX());
            yOffset += (grabY - e.getY());
            grabX = e.getX();
            grabY = e.getY();
            
            clearBufferImage();
            paint(getGraphics());
        }
        if (simulation.isStopped())
        {
            world.executeManualMoves();
            paint(getGraphics());
        }
    }

    public void mouseMoved(MouseEvent e) {}

    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        TestVisualInteractive test = new TestVisualInteractive();
        test.setSize( 800, 600 );
        test.setVisible( true );
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        double center1x = (xOffset + e.getX()) / zoom;
        double center1y = (yOffset + e.getY()) / zoom;
        
        zoom = zoom * Math.pow(0.9, e.getWheelRotation());
        
        xOffset = (int) (center1x * zoom - e.getX());
        yOffset = (int) (center1y * zoom - e.getY());
        world.setZoom(zoom);
        
        clearBufferImage();
        paint(getGraphics());
        
    }

    public void eventTriggered(Event e, Object param) 
    {
        System.out.println("Event triggered: " + e.getIdentifier());        
    }
}

