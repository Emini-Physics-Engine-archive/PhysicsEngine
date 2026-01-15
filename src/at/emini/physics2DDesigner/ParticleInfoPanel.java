package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Savepoint;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class ParticleInfoPanel extends InfoPanel implements WorldChangeListener
{
    private static final long serialVersionUID = -1781002635360840047L;

    private DesignParticleEmitter particleEmitter;
    private double optimalDevFactor = 0.1;
    
    private JCheckBox fixedAxis; 
    private FXSpinner position1X; 
    private FXSpinner position1Y; 
    private FXSpinner position2X; 
    private FXSpinner position2Y; 
    private FXAngleSpinner angle; 
    private FXAngleSpinner angleDeviation; 
    private FXSpinner speed; 
    private FXSpinner speedDeviation; 
    
    private FXSpinner creationRate; 
    private FXSpinner creationRateDeviation; 
    private FXSpinner lifetime; 
    private FXSpinner lifetimeDeviation; 
    private JSpinner maxParticleCount; 
    private JButton optimalParticleCount;

    private FXSpinner elasticity; 
    private FXSpinner gravityEffect; 
    private FXSpinner damping; 
    
    private JLabel body; 
    
    private JTextArea userData;
    
    private int mTimestepFX;
    
    public ParticleInfoPanel(WorldDesigner designer)
    {
        super(designer);
        //this.particleEmitter = particleEmitter;
        mTimestepFX = designer.getWorld().getTimestepFX();
        
        initComponents();
    }
    
    private void initComponents()
    {
        details.setLayout(new BorderLayout() );
        
        JPanel hold = new JPanel(new BorderLayout()); 
        
        JPanel left = new JPanel(new GridLayout(15,1));
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        
        JPanel right_l = new JPanel(new GridLayout(15,1)); 
        JPanel right_c = new JPanel(new GridLayout(15,1)); 
        JPanel right_r = new JPanel(new GridLayout(15,1));
        
        right.add(right_l);
        right.add(right_c);
        right.add(right_r);
        
        hold.add(left, BorderLayout.WEST);
        hold.add(right, BorderLayout.CENTER);
        
        fixedAxis = new JCheckBox("", true);
        fixedAxis.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        left.add(new JLabel("fixed axes"));
        right_l.add(fixedAxis);
        
        body = new JLabel("");
        right_c.add(new JLabel(""));
        right_r.add(body);
                
        position1X = new FXSpinner ( 0, FXUtil.DECIMAL );
        position1X.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        position1X.setPreferredSize(new Dimension(70,20));
        left.add(new JLabel("Start Position X"));
        right_l.add(position1X);
        
        position1Y = new FXSpinner ( 0, FXUtil.DECIMAL );
        position1Y.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        right_c.add(new JLabel("Y", JLabel.CENTER));
        right_r.add(position1Y);
        
        position2X = new FXSpinner ( 0, FXUtil.DECIMAL );
        position2X.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        left.add(new JLabel("End Position X"));
        right_l.add(position2X);
        
        position2Y = new FXSpinner ( 0, FXUtil.DECIMAL );
        position2Y.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        right_c.add(new JLabel("Y", JLabel.CENTER));
        right_r.add(position2Y);
        
        left.add(new JLabel(""));
        right_l.add(new JLabel(""));
        right_c.add(new JLabel(""));
        right_r.add(new JLabel(""));
        
        angle = new FXAngleSpinner ();
        angle.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        left.add(new JLabel("Emit Angle"));
        right_l.add(angle);
        
        angleDeviation = new FXAngleSpinner ();
        angleDeviation.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        right_c.add(new JLabel("+/-", JLabel.CENTER));
        right_r.add(angleDeviation);
        
        speed = new FXSpinner ( 0, 0, 10000, 10, FXUtil.DECIMAL );
        speed.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        left.add(new JLabel("Emit Speed"));
        right_l.add(speed);
        
        speedDeviation = new FXSpinner ( 0, 0, 10000, 10, FXUtil.DECIMAL );
        speedDeviation.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        right_c.add(new JLabel("+/-", JLabel.CENTER));
        right_r.add(speedDeviation);
        
        left.add(new JLabel(""));
        right_l.add(new JLabel(""));
        right_c.add(new JLabel(""));
        right_r.add(new JLabel(""));
        
        creationRate = new FXSpinner ( FXUtil.ONE_FX, 1.0, 1000.0, 10.0, FXUtil.DECIMAL );
        creationRate.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        left.add(new JLabel("Creation Rate"));
        right_l.add(creationRate);
        
        creationRateDeviation = new FXSpinner ( 0, 0.0, 1000.0, 10.0, FXUtil.DECIMAL );
        creationRateDeviation.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        right_c.add(new JLabel("+/-", JLabel.CENTER));
        right_r.add(creationRateDeviation);
        
        lifetime = new FXSpinner( 0, 0, 10000, FXUtil.DECIMAL );
        lifetime.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }            
        });
        left.add(new JLabel("Particle Life"));
        right_l.add(lifetime);
        
        lifetimeDeviation = new FXSpinner ( 0, 0, 1000, 1, FXUtil.DECIMAL );
        lifetimeDeviation.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        right_c.add(new JLabel("+/-", JLabel.CENTER));
        right_r.add(lifetimeDeviation);
        
        maxParticleCount = new JSpinner( new SpinnerNumberModel( 0, 0, 10000, 50 ) )
        {
            public Dimension getPreferredSize(){ return new Dimension(40,20);}
        };
        maxParticleCount.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }            
        });
        optimalParticleCount = new JButton("9999");
        optimalParticleCount.setMaximumSize(new Dimension(70,40));
        optimalParticleCount.setPreferredSize(new Dimension(70,20));
        optimalParticleCount.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                maxParticleCount.setValue(new Integer(calcOptimalParticleCount()));
            }
        });
        optimalParticleCount.setToolTipText("Set the max particle count to optimum.");
        
        left.add(new JLabel("Particle Count"));
        right_l.add(maxParticleCount);
        right_c.add(new JLabel(""));
        right_r.add(optimalParticleCount);

        left.add(new JLabel(""));
        right_l.add(new JLabel(""));
        right_c.add(new JLabel(""));
        right_r.add(new JLabel(""));
        
        elasticity = new FXSpinner( 0, 0.0, 1.0, 0.05, FXUtil.DECIMAL );
        elasticity.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }            
        });
        left.add(new JLabel("Elasticity"));
        right_l.add(elasticity);
        right_c.add(new JLabel(""));
        right_r.add(new JLabel(""));
        
        gravityEffect = new FXSpinner ( 0, -1000.0, 1000.0, 0.1, FXUtil.DECIMAL );
        gravityEffect.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        left.add(new JLabel("Gravity effect"));
        right_l.add(gravityEffect);
        right_c.add(new JLabel(""));
        right_r.add(new JLabel(""));
        
        damping = new FXSpinner ( 0, 0.0, 1.0, 0.05, FXUtil.DECIMAL );
        damping.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveData();
            }
        });
        left.add(new JLabel("Damping"));
        right_l.add(damping);
        right_c.add(new JLabel(""));
        right_r.add(new JLabel(""));
        
        userData = new JTextArea(3, 10);
        userData.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e){
                saveData();
            }        
            public void insertUpdate(DocumentEvent e){
                saveData();
            }        
            public void changedUpdate(DocumentEvent e){
                saveData();
            }
        });
        
        details.add(hold, BorderLayout.CENTER);
        details.add(new JScrollPane(userData), BorderLayout.SOUTH);
        
        resetLabel();
        validate();
    }
    
    public void setObject(DesignSelectionObject particleEmitter)
    {
        if (! ( particleEmitter instanceof DesignParticleEmitter) )
        {
            return;
        }
        
        super.setObject(particleEmitter);
        
        this.particleEmitter = (DesignParticleEmitter) particleEmitter;
        if (particleEmitter != null)
        {
            if (this.particleEmitter != null && particleEmitter instanceof DesignParticleEmitter)
            {
                ((DesignParticleEmitter) particleEmitter).addListener(this);
            }
            
            updateData();            
        }        
    }
    
    public void resetLabel()
    {
        identifier.setText("Particle Emitter");
    }    
            
    public DesignParticleEmitter getEmitter() 
    {
        return particleEmitter;
    }
    
    private int calcOptimalParticleCount()
    {        
        double creationRateVal = (creationRate.getValueFX() + creationRateDeviation.getValueFX() * optimalDevFactor) / FXUtil.ONE_FX;
        double avgLifeVal = (lifetime.getValueFX() +  lifetimeDeviation.getValueFX() * 0.9) / FXUtil.ONE_FX;
        
        return (int) (creationRateVal * avgLifeVal); //#FX2F return (int) (creationRateVal * avgLifeVal);
    }
    
    private boolean updateData = false;
    /**
     * updates values from emitter
     */
    protected void updateData()
    {
        if (particleEmitter.getEmitter() != null)
        {
            body.setText( "Body: " +  particleEmitter.getEmitter().getId());
            fixedAxis.setVisible(true);
        }
        else
        {
            body.setText("no body");
            fixedAxis.setVisible(false);
        }
        updateData = true;
        
        fixedAxis.setSelected( particleEmitter.emitAxesFixed());
        
        position1X.setValueFX( particleEmitter.getRelEmitterPos1().xFX );
        position1Y.setValueFX( particleEmitter.getRelEmitterPos1().yFX );
        position2X.setValueFX( particleEmitter.getRelEmitterPos2().xFX );
        position2Y.setValueFX( particleEmitter.getRelEmitterPos2().yFX );
        
        angle.setValueFX( particleEmitter.getEmitAngle2FX() );    
        angleDeviation.setValueFX( particleEmitter.getEmitAngleDeviation2FX() ); 
        speed.setValueFX( particleEmitter.getEmitSpeedFX() );
        speedDeviation.setValueFX( particleEmitter.getEmitSpeedDeviationFX() );
        
        creationRate.setValueFX( particleEmitter.getCreationRateFX() );
        creationRateDeviation.setValueFX( particleEmitter.getCreationRateDeviationFX() );
        lifetime.setValueFX( particleEmitter.getAvgLifeTimeFX());
        lifetimeDeviation.setValueFX( particleEmitter.getAvgLifeTimeDeviationFX() );
        ((SpinnerNumberModel) maxParticleCount.getModel()).setValue( new Integer( particleEmitter.getMaxParticleCount()) );
        
        elasticity.setValueFX( particleEmitter.getElasticityFX() );
        gravityEffect.setValueFX( particleEmitter.getGravityEffectFX() );
        damping.setValueFX( particleEmitter.getDampingFX() );
        
        userData.setText(((StringUserData) particleEmitter.getUserData()).getData());
        
        updateOptimalParticleButton();
        
        updateData = false;
    }
    
    /**
     * Update emitter
     */
    protected void saveData()
    {
        if (particleEmitter != null && ! updateData)
        {
            particleEmitter.setEmitAxesFixed( fixedAxis.isSelected() );
            particleEmitter.setRelEmitterPos1( new FXVector(position1X.getValueFX(), position1Y.getValueFX()) );
            particleEmitter.setRelEmitterPos2( new FXVector(position2X.getValueFX(), position2Y.getValueFX()) );
            particleEmitter.setEmitAngle2FX( angle.getValueFX(), angleDeviation.getValueFX() );  
            particleEmitter.setEmitSpeedFX( speed.getValueFX(), speedDeviation.getValueFX() );
    
            particleEmitter.setCreationRateFX( creationRate.getValueFX(), creationRateDeviation.getValueFX(), mTimestepFX );
            particleEmitter.setAvgLifeTime( lifetime.getValueFX(), lifetimeDeviation.getValueFX() );
            particleEmitter.setMaxParticleCount( ((SpinnerNumberModel) maxParticleCount.getModel()).getNumber().intValue());
            
            particleEmitter.setElasticityFX( elasticity.getValueFX() );
            particleEmitter.setGravityEffectFX( gravityEffect.getValueFX() );
            particleEmitter.setDampingFX( damping.getValueFX() );
            
            updateOptimalParticleButton(); 
            
            ((StringUserData) particleEmitter.getUserData()).setData(userData.getText());
            
            worldChangedUpdate();
        }        
    }
    
    private void updateOptimalParticleButton()
    {
        optimalParticleCount.setText("" + calcOptimalParticleCount());           
        
    }
    
    
    public void worldChanged(DesignWorld w)
    {
        mTimestepFX = w.getTimestepFX();
    }
    
    public void updateRequired()
    {
        updateData();
    }
}
