package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.GridLayout;

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

import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class WorldInfoPanel extends InfoPanel 
{

    private static final long serialVersionUID = 1L;

    private DesignWorld world;
    
    private FXSpinner gravityX; 
    private FXSpinner gravityY;    
    private FXSpinner dampingLateral;
    private FXSpinner dampingRotational;
    
    private JSpinner mConstraintIt;
    private JSpinner mPosConstraintIt;
    
    private JTextArea userData;
    
    
    public WorldInfoPanel(WorldDesigner worldDesigner)
    {
        super(worldDesigner);
        if (worldDesigner != null)
        {
            world = worldDesigner.getWorld();
        }
        
        initComponents();
        disableColorChooser();
    }

    private void initComponents()
    {
        details.setLayout(new BorderLayout());
                
        JPanel hold = new JPanel(new GridLayout(6,2));
        
        double maxGravity = Shape.MAX_MASS_FX >> FXUtil.DECIMAL;
        gravityX = new FXSpinner( 0, -maxGravity, maxGravity, 1.0, FXUtil.DECIMAL);
        gravityX.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveParameters();
            }
        });
        hold.add(new JLabel("Gravity(x) [px/s*s]"));
        hold.add(gravityX);
        
        gravityY = new FXSpinner(0, -maxGravity, maxGravity, 1.0, FXUtil.DECIMAL);
        gravityY.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveParameters();
            }
        });
        hold.add(new JLabel("Gravity(y) [px/s*s]"));
        hold.add(gravityY);
        
        dampingLateral = new FXSpinner(0, 0.0, 1.0, 0.01, FXUtil.DECIMAL);
        dampingLateral.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveParameters();
            }
        });
        hold.add(new JLabel("Lateral Damping"));
        hold.add(dampingLateral);        

        dampingRotational = new FXSpinner(0, 0.0, 1.0, 0.01, FXUtil.DECIMAL);
        dampingRotational.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveParameters();
            }
        });
        hold.add(new JLabel("Rotational Damping"));
        hold.add(dampingRotational);        
        
        mConstraintIt = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        mConstraintIt.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveParameters();
            }
        });
        hold.add(new JLabel("Constraint iterations"));
        hold.add(mConstraintIt);
        
        mPosConstraintIt = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        mPosConstraintIt.addChangeListener( new ChangeListener() {        
            public void stateChanged(ChangeEvent e) {
                saveParameters();
            }
        });
        hold.add(new JLabel("Pos. iterations"));
        hold.add(mPosConstraintIt);
        
        userData = new JTextArea(3, 10);
        userData.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e){
                saveParameters();
            }        
            public void insertUpdate(DocumentEvent e){
                saveParameters();
            }        
            public void changedUpdate(DocumentEvent e){
                saveParameters();
            }
        });
        
        
        details.add("North", hold);
        details.add("South", new JScrollPane(userData));
    }

    private void saveParameters() 
    {
        if (world != null && ! updateData)
        {
            world.setDampingLateralFX( dampingLateral.getValueFX() );
            world.setDampingRotationalFX( dampingRotational.getValueFX() );
            
            world.setConstraintIterations(((SpinnerNumberModel) mConstraintIt.getModel()).getNumber().intValue() ); 
            world.setPositionConstraintIterations(((SpinnerNumberModel) mPosConstraintIt.getModel()).getNumber().intValue() ); 
            
            FXVector gravityFX = new FXVector(gravityX.getValueFX(), gravityY.getValueFX() );
            world.setGravity( gravityFX );
            
            ((StringUserData) world.getUserData()).setData(userData.getText());
            
            worldChangedUpdate();
        }        
    }
    
    private boolean updateData = false;
    protected void updateData()
    {
        if (world != null)
        {
            updateData = true;
            //fill component values
            FXVector gravityFX = world.getGravity(); 
            gravityX.setValueFX( gravityFX.xFX );
            gravityY.setValueFX( gravityFX.yFX );
            dampingLateral.setValueFX( world.getDampingLateralFX() );
            dampingRotational.setValueFX( world.getDampingRotationalFX() );
            
            mConstraintIt.setValue(world.getConstraintIterations());
            mPosConstraintIt.setValue(world.getPositionConstraintIterations());
            
            userData.setText(((StringUserData) world.getUserData()).getData());
            updateData = false;
        }    
    }
    
    public void setObject(DesignSelectionObject parameters) 
    {
        if (! ( parameters instanceof DesignParameter) )
        {
            return;
        }
        super.setObject(parameters);
        
        this.world = ((DesignParameter) parameters).getWorld();
        updateData();
    }

}

