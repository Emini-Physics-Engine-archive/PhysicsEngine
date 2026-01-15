package at.emini.physics2DDesigner;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import at.emini.physics2D.MultiShape;
import at.emini.physics2D.Shape;


public class ShapeSelectionPanel extends SelectionPanel
{
    private static final long serialVersionUID = -8536252621737921090L;

    private JButton addShape;
    private JButton addMultiShape;
    private JButton removeShape;
    private JButton saveShape;
    private JButton loadShape;

    private Vector shapeSelectionListener = new Vector();

    private JPopupMenu newShapeMenu;

    public ShapeSelectionPanel(DesignWorld world, WorldDesigner designer)
    {
        super(world, designer);

        initActions();
        initComponents();
    }

    public void setWorld(DesignWorld world)
    {
        super.setWorld(world);

        selectedInfoPanel = null;

        setShapes(world);
    }

    private void initActions()
    {
        try
        {
            Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_save_shape.png") );
            saveShape = new JButton( new ImageIcon(image));
            saveShape.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    saveShape();
                }
            });
            saveShape.setToolTipText("Save Shapes");

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_new_shape.png") );
            addShape = new JButton(new ImageIcon(image));
            addShape.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    newShapeMenu.show(addShape, getX(), getY() + addShape.getHeight());
                    //addShape( DesignShape.createDesignShape(30, 30) );
                }
            });
            addShape.setToolTipText("New Shape");

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_new_multi_shape.png") );
            addMultiShape = new JButton(new ImageIcon(image));
            addMultiShape.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    newMultiShape();
                }
            });
            addMultiShape.setToolTipText("New Combined Shape");

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_load_shape.png") );
            loadShape = new JButton( new ImageIcon(image));
            loadShape.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    loadShape();
                }
            });
            loadShape.setToolTipText("Load Shapes");

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_delete_shape.png") );
            removeShape = new JButton(new ImageIcon(image));
            removeShape.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    removeSelected();
                }
            });
            removeShape.setToolTipText("Delete Shape");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void initComponents()
    {

        toolBar.add(saveShape);
        toolBar.add(loadShape);
        toolBar.addSeparator();

        toolBar.add(addShape);
        toolBar.add(addMultiShape);
        toolBar.add(removeShape);


        newShapeMenu = new JPopupMenu("Select Shape Prototype");

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int[] polygons = {3, 4, 5, 6, 8, 1};
        try
        {
            for( int i = 0; i < polygons.length; i++)
            {
                final int vertices = polygons[i];
                Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_poly_" + vertices + ".png") );
                JMenuItem poly = new JMenuItem();
                poly.setIcon(new ImageIcon(image));
                poly.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       addShape(DesignShapeStd.createDesignPolygon(15,vertices));
                    }
                });
                newShapeMenu.add(poly);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public Dimension getPreferredSize()
    {
        return new Dimension(250,100);
    }

    public void addShapeSelectionListener(ShapeSelectionListener listener)
    {
        shapeSelectionListener.add(listener);
    }

    private void newMultiShape()
    {
        Vector shapes = new Vector();
        Vector availableShapes = world.getShapeSet().getShapes();
        for( int i = 0; i < availableShapes.size(); i++)
        {
            if (! (availableShapes.get(i) instanceof MultiShape) )
            {
                shapes.add(availableShapes.get(i));
                break;
            }
        }

        addShape( new DesignMultiShape(shapes) );
    }

    private void addShape( DesignShape shape )
    {
        //register shape at world
        if (world != null)
        {
            world.getShapeSet().registerShape( (Shape) shape );
        }

        ShapeInfoPanel shapeInfo = new ShapeInfoPanel( shape, this );
        addPanel(shapeInfo);
        resetLabels();

        checkMultiShapePanels();

        validate();
        repaint();
    }


    public void infoPanelEdited(ShapeDesigner d) {}

    private void removeSelected()
    {
        if (selectedInfoPanel != null)
        {
            //check is shape is used
            //if (! world.isShapeUsed(selectedInfoPanel.getShape()) )
            world.removeShape((Shape) ((ShapeInfoPanel) selectedInfoPanel).getShape());
            removePanel(selectedInfoPanel);
            resetLabels();
        }

        checkMultiShapePanels();

        validate();
        repaint();
    }

    private void checkMultiShapePanels()
    {
        for( int i = 0; i < selectionPanel.getComponentCount(); i++)
        {
            DesignShape shape = ((ShapeInfoPanel) selectionPanel.getComponent(i)).getShape();
            if ( shape instanceof MultiShape)
            {
                ((ShapeInfoPanel) selectionPanel.getComponent(i)).checkShapes(world);
            }
        }
    }

    private void saveShape()
    {
        if (selectedInfoPanel != null)
        {
            JFileChooser chooser = new JFileChooser("Save Shapes");
            chooser.setCurrentDirectory( Designer.stdDir );
            chooser.setFileFilter(new PhyFileFilter());
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = chooser.showSaveDialog( this );
            if( returnVal == JFileChooser.APPROVE_OPTION )
            {
                File file = chooser.getSelectedFile();
                String filename = file.getName();
                if (! filename.contains("."))
                {
                    filename += ".phy";
                    file = new File(file.getParent(), filename);
                }
                DesignWorld saveWorld = new DesignWorld();
                saveWorld.setDesignShapeSet(world.getDesignShapeSet());
                saveWorld.saveToFile(file);
            }

        }
        repaint();
    }

    private void loadShape()
    {
        JFileChooser chooser = new JFileChooser("Load Shapes");
        chooser.setCurrentDirectory( Designer.stdDir );
        chooser.setFileFilter(new PhyFileFilter());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File file = chooser.getSelectedFile();
            DesignWorld loadWorld = DesignWorld.loadFromFile(file);
            Vector shapes = loadWorld.getDesignShapeSet().getShapes();
            for( int i = 0; i < shapes.size(); i++)
            {
                DesignShapeStd shape = (DesignShapeStd)shapes.elementAt(i);
                addShape(new DesignShapeStd(shape));
            }

        }
    }

    public void infoPanelSelected(SelectionInfoPanel infoPanel)
    {
        super.infoPanelSelected(infoPanel);

        for(int i = 0; i < shapeSelectionListener.size(); i++)
        {
            ShapeSelectionListener listener = (ShapeSelectionListener) shapeSelectionListener.get(i);
            if (listener != null)
            {
                listener.shapeInfoPanelSelected((ShapeInfoPanel)infoPanel);
            }
        }
    }

    private void setShapes(DesignWorld world)
    {
        selectionPanel.removeAll();
        Vector shapes = world.getShapes();

        for( int i = 0; i < shapes.size(); i++)
        {
            if (shapes.get(i) instanceof DesignShape)
            {
                DesignShape s = (DesignShape) shapes.get(i);
                addShape( s );
            }
        }

        if (shapes.size() > 0)
        {
            infoPanelSelected((ShapeInfoPanel) selectionPanel.getComponent(0));
        }
    }

    public DesignShape getSelectedShape()
    {
        return ((ShapeInfoPanel) selectedInfoPanel).getShape();
    }

    private void resetLabels()
    {
        for( int i = 0; i < selectionPanel.getComponentCount(); i++)
        {
            ((ShapeInfoPanel) selectionPanel.getComponent(i)).resetLabel();
        }
    }
}
