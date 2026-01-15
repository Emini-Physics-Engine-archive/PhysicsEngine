package at.emini.physics2DDesigner;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import at.emini.physics2D.util.FXUtil;

public class SpringInfoPanel extends InfoPanel
{

    private static final long serialVersionUID = -7923437084022112881L;

    private DesignSpring spring;

    private FXSpinner koefficient;

    private JTextArea userData;

    public SpringInfoPanel(WorldDesigner designer)
    {
        super(designer);
        initComponents();
    }

    private void initComponents()
    {
        details.setLayout(new BorderLayout());

        JPanel hold = new JPanel(new BorderLayout());

        koefficient = new FXSpinner(0, 0.0, 100.0, 1.0, FXUtil.DECIMAL);
        koefficient.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                saveSpring();
            }
        });
        hold.add("Center", new JLabel("Spring Koefficient", JLabel.CENTER));
        hold.add("East", koefficient);

        userData = new JTextArea(3, 10);
        userData.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e){
                saveSpring();
            }
            public void insertUpdate(DocumentEvent e){
                saveSpring();
            }
            public void changedUpdate(DocumentEvent e){
                saveSpring();
            }
        });

        hold.add(new JScrollPane(userData), BorderLayout.SOUTH);

        details.add(hold, BorderLayout.SOUTH);

    }

    public DesignSpring getSpring() {
        return spring;
    }

    private void saveSpring()
    {
        if (spring != null && ! updateData)
        {
            spring.setCoefficientFX( koefficient.getValueFX() );

            ((StringUserData) spring.getUserData()).setData(userData.getText());
            worldChangedUpdate();
        }
    }

    public void setObject(DesignSelectionObject spring)
    {
        if (! ( spring instanceof DesignSpring) )
        {
            return;
        }

        super.setObject(spring);

        this.spring = (DesignSpring) spring;
        if (spring != null)
        {
            updateData();
        }
    }

    private boolean updateData = false; //update lock
    protected void updateData()
    {
        updateData = true;
        //fill component values
        koefficient.setValueFX( spring.getCoefficientFX() );
        userData.setText(((StringUserData) spring.getUserData()).getData());
        updateData = false;
    }




}
