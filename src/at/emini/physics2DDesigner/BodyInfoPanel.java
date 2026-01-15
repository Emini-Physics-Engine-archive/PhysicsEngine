package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import at.emini.physics2D.Body;
import at.emini.physics2D.UserData;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class BodyInfoPanel extends InfoPanel implements DesignObjectChangeListener
{

    private static final long serialVersionUID = -7923437084022112881L;

    private DesignBody body;

    private JPanel controls;
    private ShapeDrawCanvas shapeCanvas;
    private JToggleButton dynamic;
    private FXSpinner positionX;
    private FXSpinner positionY;
    private FXAngleSpinner rotation;

    private JCheckBox rotatable;
    private JCheckBox gravityAffected;
    private JCheckBox interactive;
    private JTextArea userData;

    private JButton collisionBitflags;
    private JPopupMenu colissionBitflagsMenu;

    public BodyInfoPanel(WorldDesigner designer)
    {
        super(designer);
        initComponents();
    }

    private void initComponents()
    {
        details.setLayout(new BorderLayout() );

        shapeCanvas = new ShapeDrawCanvas(null)
        {
            private static final long serialVersionUID = 3266486742335146647L;

            public Dimension getPreferredSize()
            {
                if (this.shape == null)
                    return new Dimension(0, 0);
                return new Dimension(80, 80);
            }
        };
        //add("North", shapeCanvas);

        Toolkit toolkit = Toolkit.getDefaultToolkit();


        JPanel hold = new JPanel(new BorderLayout(2,2));
        try
        {
            Image dynamicImage = ImageIO.read( getClass().getResourceAsStream("/res/button_dynamic.png") );
            Image staticImage = ImageIO.read( getClass().getResourceAsStream("/res/button_static.png") );

            dynamic = new JToggleButton(new ImageIcon(staticImage, "Static"));
            dynamic.setToolTipText("Toggle Body dynamic/static");
            dynamic.setSelectedIcon(new ImageIcon(dynamicImage, "Dynamic"));
            dynamic.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveBody();
                }
            });
            hold.add("West", dynamic);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        collisionBitflags = new JButton("Collision Layers");
        collisionBitflags.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makeColissionBitflagsMenu();
                colissionBitflagsMenu.show(collisionBitflags, 0, 0);
            }
        });
        collisionBitflags.setToolTipText("Show collision layers for this body");
        hold.add(collisionBitflags, BorderLayout.CENTER);

        positionX = new FXSpinner(0, FXUtil.DECIMAL);
        positionX.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveBody();
            }
        });
        positionY = new FXSpinner(0, FXUtil.DECIMAL);
        positionY.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveBody();
            }
        });
        //rotation = new FXSpinner(0, 0, Math.PI * 2, 0.1, FXUtil.DECIMAL2);
        rotation = new FXAngleSpinner();
        rotation.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveBody();
            }
        });

        rotatable = new JCheckBox("Can rotate");
        rotatable.setSelected(true);
        rotatable.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveBody();
            }
        });

        interactive = new JCheckBox("Interactive");
        interactive.setSelected(true);
        interactive.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveBody();
            }
        });

        gravityAffected = new JCheckBox("Affected by Gravity");
        gravityAffected.setSelected(true);
        gravityAffected.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveBody();
            }
        });

        userData = new JTextArea(3, 10);
        userData.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e){
                saveBody();
            }
            public void insertUpdate(DocumentEvent e){
                saveBody();
            }
            public void changedUpdate(DocumentEvent e){
                saveBody();
            }
        });

        JPanel hold2 = new JPanel(new GridLayout(3,2));

        hold2.add(new JLabel("Position X:"));
        hold2.add(positionX);
        hold2.add(new JLabel("Position Y:"));
        hold2.add(positionY);
        hold2.add(new JLabel("Rotation:"));
        hold2.add(rotation);


        controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        controls.add(hold);
        controls.add(hold2);
        controls.add(rotatable);
        controls.add(interactive);
        controls.add(gravityAffected);
        controls.add(new JScrollPane(userData));

        details.add("Center", controls);
    }


    public Body getBody() {
        return body;
    }

    private void makeColissionBitflagsMenu()
    {
        colissionBitflagsMenu = new JPopupMenu();

        int bitFlags = 0;
        if (body != null && body instanceof DesignBody)
        {
            bitFlags = ((DesignBody) body).getColissionBitFlags();
        }
        else
        {
            return;
        }

        for( int i = 0; i < 32; i++)
        {
            final int index = i;
            int mask = (1 << i);
            //final JCheckBoxMenuItem layer = new JCheckBoxMenuItem("Layer " + (i + 1));
            final JCheckBox layer = new JCheckBox("Layer " + (i + 1));
            layer.setSelected((bitFlags & mask) == mask);
            layer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                   selectColissionLayer(index, layer.isSelected());
                }
            });
            colissionBitflagsMenu.add(layer);
        }
    }

    private void selectColissionLayer(int layer, boolean selected)
    {
        if (body != null && body instanceof DesignBody)
        {
            int mask = (1 << layer);

            int bitFlags = ((DesignBody) body).getColissionBitFlags();

            if (selected)
                bitFlags |= mask;
            else
                bitFlags &= ~mask;

            ((DesignBody) body).setColissionBitFlags(bitFlags);
        }
    }

    private void saveBody()
    {
        if (body != null && ! updateData)
        {
            body.setDynamic(dynamic.isSelected());
            body.setPositionFX( new FXVector(positionX.getValueFX(),positionY.getValueFX()) );
            body.setRotation2FX( rotation.getValueFX() );
            body.setRotatable(rotatable.isSelected());
            body.setInteracting(interactive.isSelected());
            body.setGravityAffected(gravityAffected.isSelected());
            ((StringUserData) body.getUserData()).setData(userData.getText());
            body.getVertices();

            worldChangedUpdate();
        }
    }

    public void setObject(DesignSelectionObject body)
    {
        if (! ( body instanceof DesignBody) )
        {
            return;
        }

        if (this.body != null && this.body instanceof DesignBody)
        {
            ((DesignBody) this.body).removeListener(this);
        }

        super.setObject(body);

        this.body = (DesignBody) body;
        if (body != null)
        {
            if (this.body != null && this.body instanceof DesignBody)
            {
                ((DesignBody) body).addListener(this);
            }

            if (this.body.shape() instanceof DesignShapeStd)
            {
                shapeCanvas.setShape((DesignShapeStd) this.body.shape());
            }
            updateData();

        }
    }

    private boolean updateData = false; //update lock
    protected void updateData()
    {
        updateData = true;
        //fill component values
        dynamic.setSelected(body.isDynamic());
        positionX.setValueFX(body.positionFX().xFX);
        positionY.setValueFX(body.positionFX().yFX);
        rotation.setValueFX(body.rotation2FX());
        rotatable.setSelected(body.canRotate());
        gravityAffected.setSelected(body.isAffectedByGravity());
        interactive.setSelected(body.isInteracting());
        userData.setText(((StringUserData) body.getUserData()).getData());
        updateData = false;
    }

    public void designObjectChanged(DesignSelectionObject object)
    {
        super.designObjectChanged(object);
        updateData();
    }


}
