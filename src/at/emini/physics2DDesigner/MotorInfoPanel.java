package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import at.emini.physics2D.util.FXUtil;

public class MotorInfoPanel extends InfoPanel
{

    private static final long serialVersionUID = -7923437084022112882L;

    private DesignMotor motor;

    private JPanel hold;
    private JPanel rotation;
    private JPanel linear;
    private FXSpinner targetRotFX;
    private FXSpinner targetAFX;
    private FXSpinner targetBFX;
    private FXSpinner maxForceFX;
    private JCheckBox isRotate;
    private JCheckBox isRelative;
    private JCheckBox fixOrthogonal;

    private JTextArea userData;


    public MotorInfoPanel(WorldDesigner designer)
    {
        super(designer);
        initComponents();
        disableColorChooser();
    }

    private void initComponents()
    {
        details.setLayout(new BorderLayout());
        details.setMinimumSize(new Dimension(200,100));

        hold = new JPanel();
        hold.setLayout(new BoxLayout(hold, BoxLayout.Y_AXIS));

        linear = new JPanel(new GridLayout(4,2));
        rotation = new JPanel(new GridLayout(4,2));
        JPanel common = new JPanel(new GridLayout(2,2));

        targetRotFX = new FXSpinner(0, FXUtil.DECIMAL2);
        targetRotFX.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveMotor();
            }
        });
        rotation.add(new JLabel("Angular Velocity", JLabel.LEFT));
        rotation.add(targetRotFX);
        rotation.add(new JLabel(""));
        rotation.add(new JLabel(""));

        targetAFX = new FXSpinner(0, FXUtil.DECIMAL);
        targetAFX.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveMotor();
            }
        });
        linear.add(new JLabel("Velocity X", JLabel.LEFT));
        linear.add(targetAFX);

        targetBFX = new FXSpinner(0, FXUtil.DECIMAL);
        targetBFX.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveMotor();
            }
        });
        linear.add(new JLabel("Velocity Y", JLabel.LEFT));
        linear.add(targetBFX);

        isRelative = new JCheckBox();
        isRelative.setSelected(false);
        isRelative.setToolTipText("Apply force relative to the body position.");
        isRelative.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveMotor();
            }
        });
        linear.add(new JLabel("Relative", JLabel.CENTER));
        linear.add(isRelative);


        fixOrthogonal = new JCheckBox();
        fixOrthogonal.setSelected(false);
        fixOrthogonal.setToolTipText("Fix orthogonal movement to zero.");
        fixOrthogonal.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveMotor();
            }
        });

        linear.add(new JLabel("Fix Orthogonal", JLabel.CENTER));
        linear.add(fixOrthogonal);

        maxForceFX = new FXSpinner(0, FXUtil.DECIMAL);
        maxForceFX.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveMotor();
            }
        });

        common.add(new JLabel("Power", JLabel.LEFT));
        common.add(maxForceFX);

        isRotate = new JCheckBox();
        isRotate.setSelected(true);
        isRotate.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //convertValues();
                setVisibility();
                saveMotor();
            }
        });
        common.add(new JLabel("Rotate", JLabel.CENTER));
        common.add(isRotate);

        userData = new JTextArea(3, 10);
        userData.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e){
                saveMotor();
            }
            public void insertUpdate(DocumentEvent e){
                saveMotor();
            }
            public void changedUpdate(DocumentEvent e){
                saveMotor();
            }
        });

        setVisibility();

        hold.add(linear);
        hold.add(rotation);
        hold.add(common);
        hold.add(new JScrollPane(userData));

        hold.setVisible(false);
        details.add(hold, BorderLayout.SOUTH);

    }

    private void setVisibility()
    {
        if (isRotate.isSelected())
        {
            rotation.setVisible(true);
            linear.setVisible(false);
        }
        else
        {
            rotation.setVisible(false);
            linear.setVisible(true);
        }
        validate();
    }

    public DesignMotor getMotor()
    {
        return motor;
    }

    private void saveMotor()
    {
        if (motor != null && ! updateData)
        {
            int newTargetAFX = isRotate.isSelected() ? targetRotFX.getValueFX() : targetAFX.getValueFX();
            int newTargetBFX = targetBFX.getValueFX();

            motor.setDesignMotorParameter(
                    newTargetAFX,
                    newTargetBFX,
                    maxForceFX.getValueFX(),
                    isRotate.isSelected(),
                    isRelative.isSelected(),
                    fixOrthogonal.isSelected());

            ((StringUserData) motor.getUserData()).setData(userData.getText());

            worldChangedUpdate();
        }
    }

    private boolean updateData = false; //update lock
    public void setObject(DesignSelectionObject motor) {

        if (! ( motor instanceof DesignMotor) )
        {
            return;
        }

        super.setObject(motor);

        this.motor = (DesignMotor) motor;
        if (motor != null)
        {
            hold.setVisible(true);

            updateData();
        }
        else
        {
            hold.setVisible(false);
        }

        repaint();
    }

    protected void updateData()
    {
        updateData = true;
        //fill component values
        isRotate.setSelected(this.motor.isRotation());
        isRelative.setSelected(this.motor.isRelative());
        fixOrthogonal.setSelected(this.motor.isFixOrthogonal());

        if ( motor.isRotation() )
        {
            targetRotFX.setValueFX( this.motor.getDesignTargetAFX() );
        }
        else
        {
            targetAFX.setValueFX( this.motor.getDesignTargetAFX() );
            targetBFX.setValueFX( this.motor.getDesignTargetBFX() );
        }

        maxForceFX.setValueFX( this.motor.getMaxForceFX());
        userData.setText(((StringUserData) motor.getUserData()).getData());
        updateData = false;
    }


}
