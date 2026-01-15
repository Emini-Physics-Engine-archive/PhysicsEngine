package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.emini.physics2D.util.PhysicsFileReader;
import at.emini.physics2DDesigner.guiutil.SaveChangeListener;

public class Designer extends JPanel implements ShapeSelectionListener, UndoListener
{

    /**
     * Serial id
     */
    private static final long serialVersionUID = 9165118103326342149L;

    /*
    public static File stdDir = new File("C:/eigenes/Projekte/physengine/aaphys/misc/designerfiles");
    public static File testStdDir = new File("C:/eigenes/Programmieren/Eclipse/PhysicsEngine");
    //*/
    //*
    public static File stdDir = new File(".");
    public static File testStdDir = new File(".");
    //*/
    
    public static final int ACTION_NONE = -1;
    public static final int ACTION_POINT = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_ROTATE = 3;
    public static final int ACTION_NEW_SHAPE = 4;
    public static final int ACTION_SCROLL = 5;
    public static final int ACTION_RESIZE = 6;
    public static final int ACTION_JOINT = 7;
    public static final int ACTION_SPRING = 8;
    public static final int ACTION_NEW_VERTEX = 9;
    public static final int ACTION_DELETE = 10;
    public static final int ACTION_NEW_LANDSCAPE = 11;
    public static final int ACTION_FIX_JOINT = 12;
    public static final int ACTION_MOTOR = 13;
    public static final int ACTION_FACE_SWITCH = 14;
    public static final int ACTION_MOVE_POINT = 15;
    public static final int ACTION_RESIZE_VERTICAL = 16;
    public static final int ACTION_RESIZE_HORIZONTAL = 17;
    public static final int ACTION_PARTICLE = 18;
    public static final int ACTION_EVENT = 19;
    
    public static Cursor moveCursor;
    public static Cursor movePointCursor;
    public static Cursor rotateCursor;
    public static Cursor newCursor;
    public static Cursor scrollCursor;
    public static Cursor resizeVerticalCursor;
    public static Cursor resizeHorizontalCursor;
    public static Cursor jointCursor;
    public static Cursor fixJointCursor;
    public static Cursor vertexCursor;
    public static Cursor deleteCursor;
    
    private WorldDesigner worldDesigner;
    private WorldSimulator worldSimulator;
    
    //sidepanel
    private JTabbedPane sidePanel;
    private JPanel simulationSidePanel;
    
    private ShapeSelectionPanel shapeSelection;
    private ScriptSelectionPanel scriptSelection;
    private ObjectTreePanel objectTreePanel;
    
    private SimulationBodyInfoPanel simulationBodyInfoPanel;
   
    //Toolbar
    private JToolBar toolbar;
    
    //Actions
    private JButton startSimulation;
    private JButton pauseSimulation;
    private JButton resetSimulation;
    private JButton tickSimulation;
    
    private JButton saveWorld;
    private JButton saveWorldAs;
    private JButton loadWorld;
    private JButton newWorld;
    private JButton saveTest;
    
    private JButton loadBackground;
    
    private JButton undo;
    private JButton redo;
    
    private JToggleButton enableGrid; 
    private JSpinner gridSize;
    private JToggleButton useRuler; 
    private MultiActionButton newButton;
    
    private static int[] newActions = 
    {
        ACTION_NEW_SHAPE, 
        ACTION_JOINT, 
        ACTION_FIX_JOINT, 
        ACTION_SPRING, 
        ACTION_MOTOR, 
        ACTION_PARTICLE, 
        ACTION_EVENT, 
    };
    
    private static String[] newActionImages = 
    {
        "/res/button_new_shape.png", 
        "/res/button_new_joint.png", 
        "/res/button_new_fix_joint.png", 
        "/res/button_new_spring.png", 
        "/res/button_new_motor.png", 
        "/res/button_new_particles.png",          
        "/res/button_new_event.png",          
    };
    
    private static String[] newActionToolTips = 
    {
        "New body", 
        "New joint", 
        "New fixed joint", 
        "New spring", 
        "New motor", 
        "New particle emitter",          
        "New event",
    };
    
    private DesignWorld world;

    private UndoManager undoManager = new UndoManager(10);
    
    private File saveFile = null;
    private boolean isSaved = true;
    private SaveChangeListener saveListener = null;
    
    public Designer()
    {
        setLayout( new BorderLayout( 10, 10) );
                
        DesignWorld.initImages(this);
        
        world = new DesignWorld();
                
        initCursors();
        initActions();
        initComponents();
        
        initWorld();
        initUndoManager();        
    }
    
    public Designer(PhysicsFileReader reader)
    {
        this();
        world = DesignWorld.loadFromFile(reader);
        initWorld();
        initUndoManager();
    }
    
    public Designer(File file)
    {
        this(new PhysicsFileReader(file));
        
        saveFile = file;
    }
    
    private void initCursors()
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try 
        {
            
            Image image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_move.gif") );
            moveCursor = toolkit.createCustomCursor(image , new Point(12,12), "move");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_move_point.gif") );
            movePointCursor = toolkit.createCustomCursor(image , new Point(12,12), "move point");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_rotate.gif") );
            rotateCursor = toolkit.createCustomCursor(image , new Point(8,8), "rotate");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_new.gif") );
            newCursor = toolkit.createCustomCursor(image , new Point(3,3), "new");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_scroll.gif") );
            scrollCursor = toolkit.createCustomCursor(image , new Point(8,8), "scroll");        
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_resize_vert.gif") );
            resizeVerticalCursor = toolkit.createCustomCursor(image , new Point(4,8), "resize vertical");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_resize_hor.gif") );
            resizeHorizontalCursor = toolkit.createCustomCursor(image , new Point(8,4), "resize horizontal");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_joint.gif") );
            jointCursor = toolkit.createCustomCursor(image , new Point(4,4), "joint");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_fix_joint.gif") );
            fixJointCursor = toolkit.createCustomCursor(image , new Point(4,4), "fixjoint");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_vertex.gif") );
            vertexCursor = toolkit.createCustomCursor(image , new Point(4,4), "vertex");
    
            image = ImageIO.read( getClass().getResourceAsStream("/res/mouse_remove.gif") );
            deleteCursor = toolkit.createCustomCursor(image , new Point(5,5), "delete");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
     }
    
    private void initActions()
    {
        try
        {
            Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_play.png") );        
            startSimulation = new JButton(new ImageIcon(image));
            startSimulation.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    startSimulation();
                }
            });
            startSimulation.setToolTipText("Start Simulation");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_pause.png") );
            pauseSimulation = new JButton(new ImageIcon(image));
            pauseSimulation.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    pauseSimulation();
                }
            });
            pauseSimulation.setToolTipText("Pause Simulation");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_tick.png") );
            tickSimulation = new JButton(new ImageIcon(image));
            tickSimulation.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    tickSimulation();
                }
            });
            tickSimulation.setToolTipText("Single Step");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_stop.png") );
            resetSimulation = new JButton(new ImageIcon(image));
            resetSimulation.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    restartSimulation();
                }
            });
            resetSimulation.setToolTipText("Reset Simulation");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_save_world.png") );
            saveWorld = new JButton(new ImageIcon(image));
            saveWorld.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    saveWorld();
                }
            });
            saveWorld.setToolTipText("Save World");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_save_world_as.png") );
            saveWorldAs = new JButton(new ImageIcon(image));
            saveWorldAs.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    saveWorldAs();
                }
            });
            saveWorldAs.setToolTipText("Save World As...");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_load_world.png") );
            loadWorld = new JButton(new ImageIcon(image));
            loadWorld.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    loadWorld();
                }
            });
            loadWorld.setToolTipText("Load World");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_new_world.png") );
            newWorld = new JButton(new ImageIcon(image));
            newWorld.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    newWorld();
                }
            });
            newWorld.setToolTipText("Clear World");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_load_image.png") );
            loadBackground= new JButton(new ImageIcon(image));
            loadBackground.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    loadBackGroundImage();
                }
            });
            loadBackground.setToolTipText("Load Background Image");
            
            saveTest = new JButton("Create Test");
            saveTest.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    createTest();
                }
            });
            saveTest.setToolTipText("Create Test");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_undo.png") );
            undo = new JButton(new ImageIcon(image));
            undo.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    undo();
                }
            });
            undo.setToolTipText("Undo");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_redo.png") );
            redo = new JButton(new ImageIcon(image));
            redo.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    redo();
                }
            });
            redo.setToolTipText("Redo");
            updateUndoButtons();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void initComponents()
    {
        JPanel eastHold = new JPanel(new BorderLayout());
        sidePanel = new JTabbedPane();
        simulationSidePanel = new JPanel(new BorderLayout());
        simulationSidePanel.setVisible(false);
        
        //worldInfoPanel = new WorldInfoPanel();
        //worldInfoPanel.setWorld(world);
        worldDesigner = new WorldDesigner(world); //, bodyInfoPanel, springInfoPanel, motorInfoPanel, landscapeInfoPanel, particleInfoPanel);
        objectTreePanel = new ObjectTreePanel(world, worldDesigner);
        worldDesigner.setUndoListener(this);
        worldDesigner.setTree(objectTreePanel);
        worldSimulator = new WorldSimulator(world);
        
        shapeSelection = new ShapeSelectionPanel(world, worldDesigner);
        shapeSelection.setWorld(world);
        shapeSelection.addShapeSelectionListener(this);
        
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.add("Center", shapeSelection);
        
        scriptSelection = new ScriptSelectionPanel(world, worldDesigner);
        
        //JPanel simulationParameter = new SimulationParameterPanel();
                
        sidePanel = new JTabbedPane();
        sidePanel.addTab("Shapes", bodyPanel);
        sidePanel.addTab("Script", scriptSelection);
        sidePanel.addTab("World", objectTreePanel);               
        //sidePanel.addTab("Parameter", simulationParameter);               
                
        simulationBodyInfoPanel = new SimulationBodyInfoPanel(worldSimulator);
        simulationSidePanel.add(simulationBodyInfoPanel, BorderLayout.CENTER);
        
        eastHold.add("Center", sidePanel);
        eastHold.add("East", simulationSidePanel);
        
        add("East", eastHold);
        add("Center", worldDesigner );
        

        //toolbar
        toolbar = new JToolBar("Actions");
        
        toolbar.add(newWorld);
        toolbar.add(saveWorld);
        toolbar.add(saveWorldAs);
        toolbar.add(loadWorld);
        
        //toolbar.add(saveTest);
        
        toolbar.addSeparator(new Dimension(10,0));
        
        toolbar.add(loadBackground);
        
        toolbar.addSeparator(new Dimension(10,0));
        
        toolbar.add(undo);
        toolbar.add(redo);
        
        toolbar.addSeparator(new Dimension(10,0));
        
        try
        {
            Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_point.png") );        
            ButtonGroup actionGroup = new ButtonGroup();
            JToggleButton point = new JToggleButton( new ImageIcon(image, "Point"));
            point.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pointSelected();
                }
            });
            point.setToolTipText("Select");
            actionGroup.add(point);
            toolbar.add(point);
                        
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_new_shape.png") );
            JToggleButton mainButton = new JToggleButton(new ImageIcon(image, "New Shape"));
            mainButton.setRolloverEnabled(true); 
            toolbar.add(mainButton);    //to size it correctly
            
            mainButton.setSelected(true);
            worldDesigner.setAction(ACTION_NEW_SHAPE);
            
            newButton = new MultiActionButton(mainButton, SwingConstants.SOUTH);
            newButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newSelected();
                }
            });
            actionGroup.add(mainButton);
            toolbar.add(newButton);
            
            
            JPopupMenu actionMenu = new JPopupMenu("Actions");
            
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            try
            {
                for( int i = 0; i < newActions.length; i++)
                {
                    final int actionId = newActions[i];
                    image = ImageIO.read( getClass().getResourceAsStream(newActionImages[i]) );
                    JMenuItem action = new JMenuItem();
                    final ImageIcon imageIcon = new ImageIcon(image);
                    final String toolTip = newActionToolTips[i];
                    action.setIcon(imageIcon);
                    action.addActionListener(new ActionListener() {            
                        public void actionPerformed(ActionEvent e) {
                           setNewAction(actionId, imageIcon, toolTip);
                        }
                    });
                    action.setToolTipText(toolTip);
                    actionMenu.add(action);
                    if ( i == 0 )
                    {
                        newButton.setAction(actionId, imageIcon, toolTip);
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            
            newButton.setMenu(actionMenu);
           
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_delete.png") );
            JToggleButton deleteBody = new JToggleButton( new ImageIcon(image, "Delete"));
            deleteBody.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteSelected();
                }
            });
            
            deleteBody.setToolTipText("Delete");
            actionGroup.add(deleteBody);
            toolbar.add(deleteBody);
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_new_landscape.png") );
            JToggleButton newLandscape = new JToggleButton( new ImageIcon(image, "New Landscape"));
            newLandscape.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newLandscapeSelected();
                }
            });
            newLandscape.setToolTipText("New Landscape");
            actionGroup.add(newLandscape);
            toolbar.add(newLandscape);
            
            toolbar.addSeparator(new Dimension(10,0));
            
            toolbar.add(startSimulation);
            toolbar.add(tickSimulation);
            toolbar.add(pauseSimulation);
            toolbar.add(resetSimulation);
            
            toolbar.addSeparator(new Dimension(10,0));
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_ruler.png") );
            useRuler = new JToggleButton(new ImageIcon(image, "Use Ruler"));
            useRuler.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setUseRuler();
                }
            });
            useRuler.setToolTipText("Use Ruler");
            useRuler.setSelected(true);
            toolbar.add(useRuler);
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_raster.png") );
            enableGrid = new JToggleButton( new ImageIcon(image, "Enable Grid"));
            enableGrid.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setGridParam();
                }
            });
            enableGrid.setToolTipText("Enable Grid");
            toolbar.add(enableGrid);
            
            
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
        gridSize = new JSpinner(new SpinnerNumberModel(10.0, 0.5, 100.0, 1.0));
        gridSize.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                setGridParam();
            }
        });
        toolbar.add(new JLabel("Grid Size:"));
        toolbar.add(gridSize);
        
        toolbar.addSeparator(new Dimension(10,0));
        
        add(toolbar, BorderLayout.PAGE_START);
        
        //pack();
    }
        
    private void newSelected()
    {
        worldDesigner.setAction(newButton.getActionId());
        
        if (newButton.getActionId() == ACTION_NEW_SHAPE)
        {
            DesignShape shape = null;
            if ( shapeSelection.getSelection() != null)
            {
                shape = shapeSelection.getSelectedShape();
            }
            worldDesigner.setShape( shape );
        }
    }
    
    private void setNewAction(int actionId, ImageIcon imageIcon, String toolTip)
    {
        newButton.setAction(actionId, imageIcon, toolTip);
        newSelected();
    }
    
    private void pointSelected()
    {
        worldDesigner.setAction(ACTION_POINT);        
    }    
    
    private void deleteSelected()
    {
        worldDesigner.setAction(ACTION_DELETE);
    }
    
    private void newLandscapeSelected()
    {
        worldDesigner.setAction(ACTION_NEW_LANDSCAPE);
    }
    
    private void setGridParam()
    {
        MovePanel.setGridParam( ((Double)((SpinnerNumberModel) gridSize.getModel()).getValue()).floatValue() , enableGrid.isSelected());
        repaint();
    }
    
    private void setUseRuler()
    {
        worldDesigner.setRulerVisibility(useRuler.isSelected());
        worldSimulator.setRulerVisibility(useRuler.isSelected());
        repaint();
    }
    
    
    public void startSimulation()
    {
        ensureSimulator();
        worldSimulator.startSimulation();
    }
    
    public void pauseSimulation()
    {
        //ensureSimulator();        
        worldSimulator.stopSimulation();
    }
    
    public void tickSimulation()
    {
        ensureSimulator();
        worldSimulator.tickSimulation();
    }
    
    public void restartSimulation()
    {
        worldSimulator.stopSimulation();
        ensureDesigner();        
    }
    
    private void ensureSimulator()
    {
        if (worldSimulator.getParent() == null)
        {
            sidePanel.setVisible(false);
            simulationSidePanel.setVisible(true);
                                    
            worldSimulator.restartSimulation();
            worldSimulator.adjustTransform(worldDesigner);
            remove(worldDesigner);
            add("Center", worldSimulator);
            validate();
            repaint();
        }
    } 
    
    public void ensureDesigner()
    {
        if (worldDesigner.getParent() == null)
        {
            sidePanel.setVisible(true);
            simulationSidePanel.setVisible(false);
            
            worldDesigner.adjustTransform(worldSimulator);
            remove(worldSimulator);
            add("Center", worldDesigner);
            validate();
            repaint();
        }
    } 
    
    public void loadWorld()
    {
        if (! checkSaved() )
        {
            return;
        }
        
        JFileChooser chooser = new JFileChooser("Load World");
        chooser.setCurrentDirectory( Designer.stdDir );
        chooser.setFileFilter(new PhyFileFilter());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION ) 
        {          
            File file = chooser.getSelectedFile();
            
            //PhysicsFileReader reader = new PhysicsFileReader(file);
            //World w = new World( World.loadWorld(reader));
            
            world = DesignWorld.loadFromFile(file);
            if (world != null)
            {
                saveFile = file;
                setSaved(true);
            }
            initWorld();
            initUndoManager();
        }    
    }
    
    private void newWorld()
    {
        world = new DesignWorld();
        initWorld();
        initUndoManager();
    }
    
    private void loadBackGroundImage()
    {
        worldDesigner.loadImage();
    }
    
    private void initWorld()
    {
        world.registerListener(worldDesigner);
        
        worldDesigner.setWorld(world);
        worldSimulator.setWorld(world);
        
        shapeSelection.setWorld(world);
        scriptSelection.setWorld(world);
        objectTreePanel.setWorld(world);                        
    }
    
    private void setSaved(boolean saved)
    {        
        isSaved = saved;
        if( saveListener != null)
        {
            saveListener.saveStateChanged(saved, saveFile);
        }        
    }
    
    private boolean saveWorld()
    {
        if (saveFile == null)
        {
            setSaveFile();
            if (saveFile == null)
            {
                return false;
            }
        }        
        world.saveToFile(saveFile);
        setSaved(true);
                
        return true;
    }
    
    private boolean saveWorldAs()
    {
        
        setSaveFile();
        if (saveFile == null)
        {
            return false;
        }
                
        world.saveToFile(saveFile);
        setSaved(true);
                
        return true;
    }
    
    private void setSaveFile()
    {
        JFileChooser chooser = new JFileChooser("Save World");
        chooser.setCurrentDirectory( Designer.stdDir );
        chooser.setFileFilter(new PhyFileFilter());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showSaveDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION ) 
        {          
            saveFile = chooser.getSelectedFile();
            String filename = saveFile.getName();
            if (! filename.contains("."))
            {
                filename += ".phy";
                saveFile = new File(saveFile.getParent(), filename);
            }
        }
        else
        {
            return;
        }
    } 
    
    public void setSaveChangeListener(SaveChangeListener listener)
    {
        saveListener = listener;
    }
    
    public boolean checkSaved()
    {
        if (isSaved)
        {
            return true;
        }
        
        String name = saveFile != null ? saveFile.getName() : "untitled";
        Object[] options = {"Save",
                            "Discard",
                            "Cancel"};
        int n = JOptionPane.showOptionDialog(this,
                "Do you want to save the world '" + name + "' ?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        
        switch (n)
        {
        case 2: return false;
        case 1: return true;
        case 0:
        default:
            return saveWorld();
        }
    }
    
    private void createTest()
    {
        TestCreationDialog testCreator = new TestCreationDialog(world);        
        testCreator.setVisible(true);
    }
    
    public void shapeInfoPanelSelected(ShapeInfoPanel infoPanel) 
    {
        if (infoPanel != null)
        {
            worldDesigner.setShape( infoPanel.getShape() );
        }
        else
        {
            worldDesigner.setShape( null );
        }
    }
    
    public void addUndoPoint()
    {
        undoManager.addUndoElement(world.copy());
        setSaved( false );
        updateUndoButtons();
    }
    
    private void undo()
    {
        if (undoManager.hasUndoElement())
        {
            world = undoManager.getUndoElement().copy();
            initWorld();
        }
        updateUndoButtons();
    }
    
    private  void redo()
    {
        if (undoManager.hasRedoElement())
        {
            world = undoManager.getRedoElement().copy();
            initWorld();
        }
        updateUndoButtons();
    }
    
    private void initUndoManager()
    {
        undoManager.reset(world.copy());        
        updateUndoButtons();
    }
    
    private void updateUndoButtons()
    {
        undo.setEnabled(undoManager.hasUndoElement());
        redo.setEnabled(undoManager.hasRedoElement());
    }


    public static void main(String[] args)
    {
        final JFrame frame = new JFrame("Emini Physics 2D Designer");
        frame.setSize( 800, 600 );
        
        final Designer designer = new Designer();
        
        frame.addWindowListener(new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) {
                if ( ! designer.checkSaved() )
                {
                    return;
                } 
                frame.dispose();
                System.exit(0);
            }
        });
        
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        frame.setContentPane(designer);
        frame.setLocation( 100, 100);
        frame.setVisible( true );
    }

}
