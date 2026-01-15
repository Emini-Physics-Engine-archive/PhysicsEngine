package at.emini.physics2DDesigner;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.emini.physics2D.Event;
import at.emini.physics2D.PhysicsEventListener;
import at.emini.physics2D.util.FXUtil;

public class WorldSimulator extends MovePanel implements PhysicsEventListener
{

    private static final long serialVersionUID = -4854612100379355683L;

    private DesignWorld world;
    private SimulatorThread simulation;

    private Color defaultBackgroundColor = new Color(250,253,255);
    private Color backgroundColor = new Color(250,253,255);
    //private Color backgroundColor = new Color(255,255,255, 20);

    private JToolBar viewOptions;
    private JTextArea messageArea;
    private JScrollPane messageScrollArea;

    JToggleButton showMessages;
    JToggleButton drawContacts;
    JToggleButton drawBodyTrajectory;
    JToggleButton drawVertexTrajectory;
    JToggleButton drawDesignInfo;
    JToggleButton drawParticleTrajectory;
    JToggleButton drawAABB;

    FXSpinner opacity;

    private static boolean antialiasing = false;

    public WorldSimulator(DesignWorld world)
    {
        super(true, true);
        this.world = world;
        simulation = new SimulatorThread(world, canvas, this, 1);
        simulation.start();

        initComponents();
        centerInitialization = true;
    }

    private void initComponents()
    {
        viewOptions = new JToolBar();
        messageArea = new JTextArea();
        messageArea.setRows(8);
        messageArea.setEditable(false);
        messageScrollArea = new JScrollPane(messageArea);
        messageScrollArea.setVisible(false);

        try
        {
            Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_messages.png") );
            showMessages = new JToggleButton( new ImageIcon(image, "Show Messages"));
            showMessages.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showMessages();
                }
            });
            showMessages.setToolTipText("Show Messages");
            viewOptions.add(showMessages);
            viewOptions.addSeparator();

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_draw_contacts.png") );
            drawContacts = new JToggleButton( new ImageIcon(image, "Show Contacts"));
            drawContacts.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateDesignWorld();
                }
            });
            drawContacts.setToolTipText("Show Contacts");
            viewOptions.add(drawContacts);

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_draw_body_trajectory.png") );
            drawBodyTrajectory = new JToggleButton( new ImageIcon(image, "Show Body Trajectories"));
            drawBodyTrajectory.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateDesignWorld();
                }
            });
            drawBodyTrajectory.setSelected(true);
            drawBodyTrajectory.setToolTipText("Show Body Trajectories");
            viewOptions.add(drawBodyTrajectory);

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_draw_aabb.png") );
            drawAABB = new JToggleButton( new ImageIcon(image, "Show AABB"));
            drawAABB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateDesignWorld();
                }
            });
            drawAABB.setToolTipText("Show Axis Aligned Boundary Boxes");
            viewOptions.add(drawAABB);

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_draw_vertex_trajectory.png") );
            drawVertexTrajectory = new JToggleButton( new ImageIcon(image, "Show Vertex Trajectories"));
            drawVertexTrajectory.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateDesignWorld();
                }
            });
            drawVertexTrajectory.setToolTipText("Show Vertex Trajectories");
            viewOptions.add(drawVertexTrajectory);

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_draw_design_info.png") );
            drawDesignInfo = new JToggleButton( new ImageIcon(image, "Show Design Elements"));
            drawDesignInfo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateDesignWorld();
                }
            });
            drawDesignInfo.setToolTipText("Show Design Elements");
            viewOptions.add(drawDesignInfo);

            image = ImageIO.read( getClass().getResourceAsStream("/res/button_draw_particle_lines.png") );
            drawParticleTrajectory = new JToggleButton( new ImageIcon(image, "Show Particle trajectory"));
            drawParticleTrajectory.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateDesignWorld();
                }
            });
            drawParticleTrajectory.setToolTipText("Show Vertex Trajectories");
            viewOptions.add(drawParticleTrajectory);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        opacity = new FXSpinner(FXUtil.ONE_FX, 0.0, 1.0, 0.1, FXUtil.DECIMAL);
        opacity.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateDesignWorld();
            }
        });
        opacity.setMaximumSize(new Dimension(50,30));
        viewOptions.add(new JLabel("Opacity"));
        viewOptions.add(opacity);

        JPanel hold = new JPanel(new BorderLayout());

        hold.add(viewOptions, BorderLayout.NORTH);
        hold.add(messageScrollArea, BorderLayout.CENTER);
        add(hold, BorderLayout.SOUTH);
    }

    public void registerInfoPanel(SimulationBodyInfoPanel infoPanel)
    {
        simulation.registerInfoPanel(infoPanel);
    }

    public void setWorld(DesignWorld world)
    {
        this.world = world;
        simulation.setWorld(world);
        updateDesignWorld();
        refresh();
    }


    private void updateDesignWorld()
    {
        simulation.setDrawParameter(
                drawContacts.isSelected(),
                drawBodyTrajectory.isSelected(),
                drawVertexTrajectory.isSelected(),
                drawDesignInfo.isSelected(),
                drawParticleTrajectory.isSelected(),
                drawAABB.isSelected());

        backgroundColor = new Color(
                defaultBackgroundColor.getRed(),
                defaultBackgroundColor.getGreen(),
                defaultBackgroundColor.getBlue(),
                (opacity.getValueFX() * 255) / FXUtil.ONE_FX); //#FX2F (int) (opacity.getValueFX() * 255));

    }

    private void showMessages()
    {
        messageScrollArea.setVisible(showMessages.isSelected());
        validate();
    }

    public void clearBackground(GraphicsWrapper g)
    {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void paintCanvas(GraphicsWrapper g)
    {
        if(antialiasing)
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke( new BasicStroke(4096) );
        }
        simulation.draw(g);
    }

    public void componentShown(ComponentEvent e)
    {
        updateDesignWorld();
        simulation.restartSimulation();
        //world.recalculateBodyInternals();
        super.componentShown(e);

        refresh();
    }

    public void componentHidden(ComponentEvent e)
    {
        simulation.end();
        super.componentHidden(e);
    }


    public void startSimulation()
    {
        updateDesignWorld();

        if (imageBuffer != null)
        {
            Graphics g = imageBuffer.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        simulation.restart();
    }

    public void restartSimulation()
    {
        simulation.end();
        updateDesignWorld();
        messageArea.setText("");

        if (imageBuffer != null)
        {
            Graphics g = imageBuffer.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        simulation.restartSimulation();

        refresh();
    }

    public void tickSimulation()
    {
        updateDesignWorld();
        simulation.tick();

    }

    public void stopSimulation()
    {
        simulation.end();
    }

    public void eventTriggered(Event e, Object param)
    {
        String text = messageArea.getText();
        text += "Event at " +  simulation.getStepCount() + ": " +  e.getIdentifier() + "\n";
        messageArea.setText(text);
        messageArea.setCaretPosition(text.length());
    }

    public void mousePressed(MouseEvent e)
    {
        simulation.setInteractionBodyAt(calcFXPosition(e.getX(), e.getY()));

        if (! simulation.hasInteraction())
        {
            super.mousePressed(e);
        }
    }

    public void mouseDragged(MouseEvent e)
    {
        if (simulation.hasInteraction())
        {
            simulation.setInteractionPos(calcFXPosition(e.getX(), e.getY()));
        }
        else
        {
            super.mouseDragged(e);
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        super.mouseReleased(e);
        simulation.clearInteractionBody();
    }

    public void mouseExited(MouseEvent e)
    {
        simulation.clearInteractionBody();
    }

}

