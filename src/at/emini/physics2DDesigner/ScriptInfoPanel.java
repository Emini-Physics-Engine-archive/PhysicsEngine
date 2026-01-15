package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import at.emini.physics2D.Script;

public class ScriptInfoPanel extends SelectionInfoPanel
{
    private static final long serialVersionUID = -6031156785897916540L;

    private DesignScript script;

    private JToolBar toolBar;
    private JScrollPane listScroll;
    private JPanel elementsPanel;

    private JCheckBox repeat;
    private JCheckBox show;

    protected static ImageIcon scriptPos;
    protected static ImageIcon scriptVel;
    protected static ImageIcon scriptAcc;
    protected static ImageIcon scriptAngle;
    protected static ImageIcon scriptRotVel;
    protected static ImageIcon scriptRotAcc;


    public ScriptInfoPanel(DesignScript script, SelectionPanel selectionPanel )
    {
        super(selectionPanel, script.getOpaqueColor(), script.getOpaqueColor().brighter());
        this.script = script;

        initImages();
        initComponents();

        setScript(script);
    }

    private void initImages()
    {
        if (scriptPos == null)
        {
            try
            {
                Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_script_pos_sm.png") );
                scriptPos = new ImageIcon(image, "Position Element");

                image = ImageIO.read( getClass().getResourceAsStream("/res/button_script_velocity_sm.png") );
                scriptVel = new ImageIcon(image, "Velocity Element");

                image = ImageIO.read( getClass().getResourceAsStream("/res/button_script_acceleration_sm.png") );
                scriptAcc = new ImageIcon(image, "Acceleration Element");

                image = ImageIO.read( getClass().getResourceAsStream("/res/button_script_angle_sm.png") );
                scriptAngle = new ImageIcon(image, "Angle Element");

                image = ImageIO.read( getClass().getResourceAsStream("/res/button_script_rotvel_sm.png") );
                scriptRotVel = new ImageIcon(image, "Rotational Velocity Element");

                image = ImageIO.read( getClass().getResourceAsStream("/res/button_script_rotacc_sm.png") );
                scriptRotAcc = new ImageIcon(image, "Rotational Acceleration Element");
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    private void initComponents()
    {
        toolBar = new JToolBar("Actions");
        toolBar.setOrientation(JToolBar.VERTICAL);


        JButton pos = new JButton( scriptPos );
        pos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addScriptElement(Script.POSITION);
            }
        });
        pos.setToolTipText("Position Element");
        toolBar.add(pos);

        JButton velocity = new JButton( scriptVel );
        velocity.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addScriptElement(Script.VELOCITY);
            }
        });
        velocity.setToolTipText("Velocity Element");
        toolBar.add(velocity);

        JButton acceleration = new JButton( scriptAcc );
        acceleration.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addScriptElement(Script.ACCELERATION);
            }
        });
        acceleration.setToolTipText("Acceleration Element");
        toolBar.add(acceleration);

        JButton angle = new JButton( scriptAngle );
        angle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addScriptElement(Script.ANGLE);
            }
        });
        angle.setToolTipText("Angle Element");
        toolBar.add(angle);

        JButton rotvel = new JButton( scriptRotVel );
        rotvel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addScriptElement(Script.ROTATIONAL_VELOCITY);
            }
        });
        rotvel.setToolTipText("Rotational Velocity Element");
        toolBar.add(rotvel);

        JButton rotacc = new JButton( scriptRotAcc );
        rotacc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addScriptElement(Script.ROTATIONAL_ACCELERATION);
            }
        });
        rotacc.setToolTipText("Rotational Acceleration Element");
        toolBar.add(rotacc);

        //add(toolBar, BorderLayout.EAST);

        elementsPanel = new JPanel();
        elementsPanel.setLayout(new BoxLayout(elementsPanel, BoxLayout.Y_AXIS));
        listScroll = new JScrollPane(elementsPanel);

        repeat = new JCheckBox("Repeat Script");
        repeat.setToolTipText("Repeat Script");
        repeat.setSelected(script.isRestart());
        repeat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartSelected();
            }
        });

        show = new JCheckBox();
        show.setOpaque(false);
        show.setSelected(script.isVisible());
        show.setToolTipText("Show Event");
        show.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                showScript();
            }
        } );

        header.add(show, BorderLayout.EAST);


        details.setLayout(new BorderLayout());
        details.add(toolBar, BorderLayout.EAST);
        details.add(listScroll, BorderLayout.CENTER);
        details.add(repeat, BorderLayout.SOUTH);

        add(details, BorderLayout.CENTER);

        resetLabel();
        validate();
    }

    public void resetLabel()
    {
        identifier.setText(script.getName());
    }


    public DesignScript getScript()
    {
        return script;
    }

    private void setScript(DesignScript script)
    {
        this.script = script;

        for( int i = 0; i < script.getElementCount(); i++)
        {
            addScriptElement(script.getElement(i));
        }
    }

    private void restartSelected()
    {
        script.setRestart(repeat.isSelected());
    }

    private void addScriptElement( int type )
    {
        script.addElement(type, 0, 0, 20);
        addScriptElement( script.getElement( script.getElementCount() - 1) );
    }

    private void addScriptElement( Script.ScriptElement element )
    {
        ScriptElementPanel elementPanel = new ScriptElementPanel( element, script.getOpaqueColor() );
        elementsPanel.add(elementPanel);

        selectionPanel.validate();
    }

    private void showScript()
    {
        script.setVisible(show.isSelected());
        selectionPanel.getWorld().updateAndRepaint();
    }

}
