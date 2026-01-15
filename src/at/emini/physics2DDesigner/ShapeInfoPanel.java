package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import at.emini.physics2D.MultiShape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXUtil;

public class ShapeInfoPanel extends SelectionInfoPanel
{

    private static final long serialVersionUID = -8375639835594979045L;

    private DesignShape shape;
    private ShapeDesigner shapeCanvas;
    private ShapeViewer   multiShapeCanvas;
    private ShapeViewer previewCanvas;

    private MultiShapeSelection multiShapeSelection;

    private JPanel shapeDetails;
    private JSpinner elasticity;
    private JSpinner friction;
    private JSpinner mass;
    private JTextArea userData;

    private JToolBar toolBar;

    public ShapeInfoPanel(DesignShape s, SelectionPanel selectionPanel )
    {
        super(selectionPanel, s.getOpaqueColor(), s.getOpaqueColor().brighter());
        this.shape = s;

        initComponents(s);
        if (s instanceof DesignShapeStd)
        {
            initComponentsStdShape( (DesignShapeStd) s);
        }
        else if (s instanceof DesignMultiShape)
        {
            initComponentsMultiShape( (DesignMultiShape) s);
        }

        setShape(s);
    }

    private void initComponents(DesignShape s)
    {
        details.setLayout(new BorderLayout());

        double massVal = s.getMassFX() / (double) FXUtil.ONE_FX;
        double elasticityVal = s.getElasticityFX() / (double) FXUtil.ONE_FX;
        double frictionVal = s.getFrictionFX() / (double) FXUtil.ONE_FX;

        shapeDetails = new JPanel(new GridLayout(3,2));

        mass = new JSpinner(new SpinnerNumberModel(massVal, 1.0, Shape.MAX_MASS_FX >> FXUtil.DECIMAL, 1.0));
        mass.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                shape.setMassFX( (int) ( ((SpinnerNumberModel)mass.getModel()).getNumber().floatValue()  * FXUtil.ONE_FX) );
            }
        });

        elasticity = new JSpinner (new SpinnerNumberModel(elasticityVal, 0.0, 1.0, 0.05));
        elasticity.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                shape.setElasticityFX( (int) ( ((SpinnerNumberModel)elasticity.getModel()).getNumber().floatValue()  * FXUtil.ONE_FX) );
            }
        });
        friction = new JSpinner (new SpinnerNumberModel(frictionVal, 0.0, 1.0, 0.05));
        friction.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                shape.setFrictionFX( (int) ( ((SpinnerNumberModel)friction.getModel()).getNumber().floatValue()  * FXUtil.ONE_FX) );
            }
        });

        userData = new JTextArea(3, 10);
        userData.setText(((StringUserData) s.getUserData()).getData());
        userData.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e){
                saveText();
            }
            public void insertUpdate(DocumentEvent e){
                saveText();
            }
            public void changedUpdate(DocumentEvent e){
                saveText();
            }
        });

        shapeDetails.add( new JLabel("Mass"));
        shapeDetails.add(mass);
        shapeDetails.add( new JLabel("Elastictiy"));
        shapeDetails.add(elasticity);
        shapeDetails.add( new JLabel("Friction"));
        shapeDetails.add(friction);

        JPanel hold = new JPanel();
        hold.setLayout(new BoxLayout(hold, BoxLayout.Y_AXIS));

        hold.add(new JScrollPane(userData));

        details.add("South", hold);


        previewCanvas = new ShapeViewer(s, selectionPanel.getWorld(), true, false);

        header.add(previewCanvas, BorderLayout.EAST);

        resetLabel();
    }

    private void initComponentsStdShape(DesignShapeStd s)
    {
        shapeCanvas = new ShapeDesigner(s, selectionPanel.getWorld(), true);

        details.add(shapeCanvas, BorderLayout.CENTER);

        JPanel hold = new JPanel();
        hold.setLayout(new BoxLayout(hold, BoxLayout.Y_AXIS));

        hold.add(shapeDetails);
        hold.add(new JScrollPane(userData));

        details.add("South", hold);

        toolBar = new JToolBar("Actions");
        toolBar.setOrientation(JToolBar.VERTICAL);

        try
        {
            Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_load_image_sm.png") );
            JButton loadImage = new JButton( new ImageIcon(image, "Load Shape Image"));
            loadImage.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadImage();
                }
            });
            loadImage.setToolTipText("Load Shape Image");
            toolBar.add(loadImage);
            toolBar.addSeparator();

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_move_sm.png") );
            ButtonGroup actionGroup = new ButtonGroup();
            JToggleButton move = new JToggleButton( new ImageIcon(image, "Move"));
            move.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pointSelected();
                }
            });
            move.setToolTipText("Move");
            actionGroup.add(move);
            move.setSelected(true);
            toolBar.add(move);

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_vertex_sm.png") );
            JToggleButton newVertex = new JToggleButton( new ImageIcon(image, "New Vertex"));
            newVertex.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newVertexSelected();
                }
            });
            newVertex.setToolTipText("New Vertex");
            actionGroup.add(newVertex);
            toolBar.add(newVertex);

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_delete_sm.png") );
            JToggleButton deleteVertex = new JToggleButton( new ImageIcon(image, "Delete Vertex"));
            deleteVertex.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteSelected();
                }
            });
            deleteVertex.setToolTipText("Delete Vertex");
            actionGroup.add(deleteVertex);
            toolBar.add(deleteVertex);

            toolBar.addSeparator();

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_center_sm.png") );
            JButton correctCentroid = new JButton( new ImageIcon(image, "Correct Centroid "));
            correctCentroid.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    correctCentroid();
                }
            });
            correctCentroid.setToolTipText("Correct Centroid");
            toolBar.add(correctCentroid);
        }
        catch (IOException e )
        {
            e.printStackTrace();
        }
        details.add(toolBar, BorderLayout.EAST);
    }


    private void initComponentsMultiShape(DesignShape s)
    {
        multiShapeCanvas = new ShapeViewer(s, selectionPanel.getWorld(), false, true);

        details.add(multiShapeCanvas, BorderLayout.CENTER);

        DesignWorld world = selectionPanel.getWorld();
        multiShapeSelection = new MultiShapeSelection((MultiShape) s, world.getShapes(), world);

        details.add(multiShapeSelection, BorderLayout.EAST);

    }

    private void saveText()
    {
        ((StringUserData) shape.getUserData()).setData(userData.getText());
    }

    public DesignShape getShape()
    {
        return shape;
    }

    private void setShape(DesignShape shape)
    {
        this.shape = shape;
        if (shape != null)
        {
            if (shape instanceof DesignShapeStd)
            {
                shapeCanvas.setShape((DesignShapeStd)shape);
            }
            else
            {
                multiShapeCanvas.setShape(shape);
            }
            resetLabel();
        }
    }

    public void resetLabel()
    {
        identifier.setText("Shape: " + shape.getId());
    }

    private void loadImage()
    {
        JFileChooser chooser = new JFileChooser("Load Shape Image");
        chooser.setCurrentDirectory( Designer.stdDir );
        int returnVal = chooser.showOpenDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File imageFile = chooser.getSelectedFile();

            try
            {
                BufferedImage image = ImageIO.read(imageFile);
                shapeCanvas.setBackgroundImage( image, - image.getWidth(this) / 2, - image.getHeight(this) / 2);
                selectionPanel.getWorld().getDesignShapeSet().registerImageForShape(image, (Shape) shape);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void pointSelected()
    {
        shapeCanvas.setSelectedAction(Designer.ACTION_POINT);
    }

    private void newVertexSelected()
    {
        shapeCanvas.setSelectedAction(Designer.ACTION_NEW_VERTEX);
    }

    private void deleteSelected()
    {
        shapeCanvas.setSelectedAction(Designer.ACTION_DELETE);
    }


    public void correctCentroid()
    {
        shape.correctCentroid();
        selectionPanel.getWorld().updateAndRepaint();
        repaint();
    }


    public void checkShapes(DesignWorld world)
    {
        if (multiShapeSelection != null)
        {
            multiShapeSelection.checkShapes(world);
        }
    }
}
