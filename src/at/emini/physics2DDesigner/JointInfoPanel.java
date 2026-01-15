package at.emini.physics2DDesigner;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JointInfoPanel extends InfoPanel 
{

    private static final long serialVersionUID = -7923437084022112881L;
    
    private DesignJoint joint;
    
    private JTextArea userData;
    
    public JointInfoPanel(WorldDesigner designer)
    {
        super(designer);
        initComponents();
    }

    private void initComponents()
    {
        details.setLayout(new BorderLayout());
       
        userData = new JTextArea(3, 10);
        userData.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e){
                saveJoint();
            }        
            public void insertUpdate(DocumentEvent e){
                saveJoint();
            }        
            public void changedUpdate(DocumentEvent e){
                saveJoint();
            }
        });
        
        details.add(new JScrollPane(userData), BorderLayout.CENTER);
    }

    public DesignJoint getJoint() 
    {
        return joint;
    }

    private void saveJoint()
    {
        if (joint != null && ! updateData)
        {
            ((StringUserData) joint.getUserData()).setData(userData.getText());
            
            worldChangedUpdate();
        }
    } 
    
    public void setObject(DesignSelectionObject joint)
    {
        if (! ( joint instanceof DesignJoint) )
        {
            return;
        }
        
        super.setObject(joint);
            
        this.joint = (DesignJoint) joint;
        if (joint != null)
        {
            updateData();
        }
                
    }

    private boolean updateData = false; //update lock
    protected void updateData()
    {
        updateData = true;
        //fill component values
        userData.setText(((StringUserData) joint.getUserData()).getData());
        updateData = false;
    }
    
     
    

}
