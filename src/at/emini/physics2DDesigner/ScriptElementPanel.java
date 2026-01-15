package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.emini.physics2D.Script;
import at.emini.physics2D.util.FXUtil;

public class ScriptElementPanel extends JPanel
{

    private static final long serialVersionUID = -6421432425537618716L;

    private Script.ScriptElement element;
    private Color scriptColor;

    private JPanel header;
    private JPanel details;
    private FXSpinner targetA;
    private FXSpinner targetB;
    private JSpinner time;

    private static final double maxValue = 1000.0;

    private static final String[] headerLabel = {
            "",
            "Position",
            "Velocity",
            "Acceleration",
            "Angle",
            "Rot. Velocity",
            "Rot. Acceleration" };

    private static final String[] targetALabel = {
            "",
            "Position X",
            "Velocity X",
            "Acceleration X",
            "Angle",
            "Rot. Velocity",
            "Rot. Acceleration" };

    private static final String[] targetBLabel = {
            "",
            "Position Y",
            "Velocity Y",
            "Acceleration Y",
            "",
            "",
            "" };

    public ScriptElementPanel(Script.ScriptElement element, Color c)
    {
        this.element = element;
        this.scriptColor = c;

        initComponents();
    }

    private void initComponents() {

        setLayout(new BorderLayout());

        ImageIcon icon = null;
        switch (element.mType)
        {
        case Script.POSITION: icon = ScriptInfoPanel.scriptPos; break;
        case Script.VELOCITY: icon = ScriptInfoPanel.scriptVel; break;
        case Script.ACCELERATION: icon = ScriptInfoPanel.scriptAcc; break;
        case Script.ANGLE: icon = ScriptInfoPanel.scriptAngle; break;
        case Script.ROTATIONAL_VELOCITY: icon = ScriptInfoPanel.scriptRotVel; break;
        case Script.ROTATIONAL_ACCELERATION: icon = ScriptInfoPanel.scriptRotAcc; break;
        default: break;
        }

        header = new JPanel(new GridLayout(1,2));
        header.add(new JLabel(headerLabel[element.mType], icon, JLabel.LEFT));
        header.setBackground(scriptColor);
        header.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                selected();
            }
            public void mouseClicked(MouseEvent e) {
                selected();
            }
        });

        boolean useTargetB = targetBLabel[element.mType] != "";

        int decimals = (element.mType == Script.ANGLE ||
                element.mType == Script.ROTATIONAL_ACCELERATION ||
                element.mType == Script.ROTATIONAL_VELOCITY) ? FXUtil.DECIMAL2 : FXUtil.DECIMAL;


        details = new JPanel(new GridLayout(useTargetB ? 3 : 2, 2));

        targetA = new FXSpinner(element.mTargetAFX, -maxValue, maxValue, 1.0, decimals);
        targetA.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                valueChanged();
            }
        });
        details.add(new JLabel(targetALabel[element.mType] ));
        details.add(targetA);

        if (useTargetB)
        {
            targetB = new FXSpinner(element.mTargetBFX, -maxValue, maxValue, 1.0, decimals);
            targetB.addChangeListener( new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    valueChanged();
                }
            });
            details.add(new JLabel(targetBLabel[element.mType] ));
            details.add(targetB);
        }



        time = new JSpinner(new SpinnerNumberModel(element.mTimeSteps, 1, 10000, 20));
        time.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                valueChanged();
            }
        });
        details.add(new JLabel("Timesteps"));
        details.add(time);

        JPanel hold = new JPanel(new BorderLayout());
        hold.add(header, BorderLayout.NORTH);
        hold.add(details, BorderLayout.CENTER);

        add(hold, BorderLayout.NORTH);
    }

    public void select(boolean selected)
    {
        details.setVisible(selected);
    }

    private void valueChanged()
    {
        element.mTargetAFX = targetA.getValueFX();
        if (targetB != null)
        {
            element.mTargetBFX = targetB.getValueFX();
        }
        element.mTimeSteps = (int) (((SpinnerNumberModel)time.getModel()).getNumber().intValue());

    }

    private void selected()
    {
        getParent().requestFocus();
    }
}
