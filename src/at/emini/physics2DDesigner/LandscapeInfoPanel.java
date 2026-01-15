package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.emini.physics2D.Landscape;
import at.emini.physics2D.util.FXUtil;

public class LandscapeInfoPanel extends InfoPanel
{

    private static final long serialVersionUID = -7923437084022112882L;

    private Landscape landscape;

    private FXSpinner elasticity;
    private FXSpinner friction;

    private JPanel controls;

    public LandscapeInfoPanel(WorldDesigner designer)
    {
        super(designer);
        initComponents();
        disableColorChooser();
    }

    private void initComponents()
    {
        details.setLayout(new BorderLayout());

        controls = new JPanel(new GridLayout(2,2));

        controls.add(new JLabel("Elasticity:"));
        elasticity = new FXSpinner(0, 0.0, 1.0, 0.05, FXUtil.DECIMAL);
        elasticity.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveLandscape();
            }
        });
        controls.add(elasticity);

        controls.add(new JLabel("Friction:"));
        friction = new FXSpinner(0, 0.0, 1.0, 0.05, FXUtil.DECIMAL);
        friction.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveLandscape();
            }
        });
        controls.add(friction);
        details.add("South", controls);
    }

    public Landscape getLandscape() {
        return landscape;
    }


    private void saveLandscape()
    {
        if (landscape != null && ! updateData)
        {
            landscape.getShape().setElasticityFX(elasticity.getValueFX());
            landscape.getShape().setFrictionFX(friction.getValueFX());
            worldChangedUpdate();
        }
    }

    public void setObject(DesignSelectionObject landscape)
    {
        if (! ( landscape instanceof DesignLandscape) )
        {
            return;
        }

        super.setObject(landscape);

        this.landscape = (DesignLandscape)landscape;
        updateData();

    }

    private boolean updateData = false; //update lock
    protected void updateData()
    {
        if (landscape != null)
        {
            updateData = true;
            //fill component values
            elasticity.setValueFX(landscape.getShape().getElasticityFX());
            friction.setValueFX(landscape.getShape().getFrictionFX());
            updateData = false;
        }
    }



}
