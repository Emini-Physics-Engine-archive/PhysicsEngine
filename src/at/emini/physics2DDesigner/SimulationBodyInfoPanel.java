package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import at.emini.physics2D.util.FXUtil;

public class SimulationBodyInfoPanel extends JPanel
{
    private static final long serialVersionUID = -5101473463167240561L;
    
    private DesignBody selectedBody;
    private DesignWorld world;
    
    private JLabel bodyId;
    private JLabel bodyPosX;
    private JLabel bodyPosY;
    private JLabel bodyVelocityX;
    private JLabel bodyVelocityY;
    private JLabel bodyRotation;
    private JLabel bodyTotalEnergy;
    
    
    public SimulationBodyInfoPanel(WorldSimulator simulator)
    {
        simulator.registerInfoPanel(this);
        initComponents();
    }
    
    private void initComponents()
    {
        setLayout(new BorderLayout());
        
        JPanel content = new JPanel(new GridLayout(7, 2));
        
        bodyId = new JLabel();
        bodyPosX = new JLabel();
        bodyPosY = new JLabel();
        bodyVelocityX = new JLabel();
        bodyVelocityY = new JLabel();
        bodyRotation = new JLabel();
        bodyTotalEnergy = new JLabel();
        
        content.add(new JLabel("Body"));
        content.add(bodyId);
        content.add(new JLabel("Pos X"));
        content.add(bodyPosX);
        content.add(new JLabel("Pos Y"));
        content.add(bodyPosY);       
        content.add(new JLabel("Vel X"));
        content.add(bodyVelocityX);
        content.add(new JLabel("Vel Y"));
        content.add(bodyVelocityY);       
        content.add(new JLabel("Angle"));
        content.add(bodyRotation);
        content.add(new JLabel("Total Energy"));
        content.add(bodyTotalEnergy);
        
        add(content, BorderLayout.NORTH);
    }
    
    public void selectBody(DesignBody body, DesignWorld world)
    {
        if (body != null)
        {
            selectedBody = body;
            this.world = world;
            update();
        }
    }
    
    private double radToDeg = 180.0 / (Math.PI * (1 << FXUtil.DECIMAL)); 
    public void update()
    {
        if (selectedBody != null)
        {            
            bodyId.setText(""+selectedBody.getId());
            bodyPosX.setText(String.format("%5.2f", selectedBody.positionFX().xAsFloat()));
            bodyPosY.setText(String.format("%5.2f", selectedBody.positionFX().yAsFloat()));
            bodyVelocityX.setText(String.format("%5.2f", selectedBody.velocityFX().xAsFloat()));
            bodyVelocityY.setText(String.format("%5.2f", selectedBody.velocityFX().yAsFloat()));
            bodyRotation.setText( String.format("%3.2f", (selectedBody.rotation2FX() >> FXUtil.DECIMAL) * radToDeg) );
            bodyTotalEnergy.setText(String.format("%8.2f", ((float) world.getBodyTotalEnergyFX(selectedBody)) / FXUtil.ONE_FX ) );
        }
        else
        {
            bodyId.setText("");
            bodyPosX.setText("");
            bodyPosY.setText("");
            bodyRotation.setText("");
            bodyTotalEnergy.setText("");           
        }
    }

}
