package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.emini.physics2D.util.FXUtil;

public class BodyAreaEventInfoPanel extends EventInfoPanel
{
    private static final long serialVersionUID = -1781002635360840047L;

    private FXSpinner upperLeftX;
    private FXSpinner upperLeftY;
    private FXSpinner lowerRightX;
    private FXSpinner lowerRightY;

    public BodyAreaEventInfoPanel(WorldDesigner designer)
    {
        super(designer);

        initComponents();
    }

    private void initComponents()
    {
        upperLeftX = new FXSpinner ( 0, FXUtil.DECIMAL );
        upperLeftX.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                valueChanged();
            }
        });

        upperLeftY = new FXSpinner ( 0, FXUtil.DECIMAL );
        upperLeftY.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                valueChanged();
            }
        });
        lowerRightX = new FXSpinner ( 0, FXUtil.DECIMAL );
        lowerRightX.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                valueChanged();
            }
        });
        lowerRightY = new FXSpinner ( 0, FXUtil.DECIMAL );
        lowerRightY.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                valueChanged();
            }
        });

        JPanel bodyAreaDetails = new JPanel(new GridLayout(4,2));

        bodyAreaDetails.add( new JLabel("Upper Left X"));
        bodyAreaDetails.add(upperLeftX);
        bodyAreaDetails.add( new JLabel("Upper Left Y"));
        bodyAreaDetails.add(upperLeftY);
        bodyAreaDetails.add( new JLabel("Lower Right X"));
        bodyAreaDetails.add(lowerRightX);
        bodyAreaDetails.add( new JLabel("Lower Right Y"));
        bodyAreaDetails.add(lowerRightY);

        details.add(bodyAreaDetails, BorderLayout.CENTER);
    }

    private void valueChanged()
    {
        if (! updateLock && event != null)
        {
            event.setTargets(upperLeftX.getValueFX(),
                    upperLeftY.getValueFX(),
                    lowerRightX.getValueFX(),
                    lowerRightY.getValueFX());
            event.setTriggerOnce(triggeredOnce.isSelected());
            event.setVisible(show.isSelected());

            worldChangedUpdate();
        }
    }

    private boolean updateLock = false;
    public void updateData()
    {
        super.updateData();

        updateLock = true;
        upperLeftX.setValueFX( event.getTargetAFX() );
        upperLeftY.setValueFX( event.getTargetBFX() );
        lowerRightX.setValueFX( event.getTargetCFX() );
        lowerRightY.setValueFX( event.getTargetDFX() );

        show.setSelected(event.isVisible());
        triggeredOnce.setSelected(event.getTriggerOnce());

        updateLock = false;
    }
}
