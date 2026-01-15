package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import at.emini.physics2D.util.FXVector;

public abstract class EventInfoPanel extends InfoPanel implements WorldChangeListener
{
    private static final long serialVersionUID = -1781002635360840046L;

    protected DesignPhysicsEvent event;
    
    protected JCheckBox triggeredOnce;
    protected JCheckBox show;
    private JTextArea userData;
    
    public EventInfoPanel(WorldDesigner designer)
    {
        super(designer);
       
        initComponents();
    }
    
    private void initComponents()
    {
        show = new JCheckBox();
        show.setOpaque(false);
        show.setToolTipText("Show Event");
        show.addItemListener(new ItemListener()
        {        
            public void itemStateChanged(ItemEvent e)
            {
                showEvent();
            }
        } );
        header.add(show, BorderLayout.EAST);
        
        details.setLayout(new BorderLayout() );
        
        JPanel hold = new JPanel();
        hold.setLayout(new BoxLayout(hold, BoxLayout.Y_AXIS));
        
        triggeredOnce = new JCheckBox("Trigger once");
        triggeredOnce.addItemListener(new ItemListener()
        {        
            public void itemStateChanged(ItemEvent e)
            {
                setTriggeredOnce();
            }
        } );
        hold.add(triggeredOnce);
        
        userData = new JTextArea(3, 10);
        userData.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e){
                saveEvent();
            }        
            public void insertUpdate(DocumentEvent e){
                saveEvent();
            }        
            public void changedUpdate(DocumentEvent e){
                saveEvent();
            }
        });
        hold.add(new JScrollPane(userData));
        
        details.add(hold, BorderLayout.SOUTH);
        
        resetLabel();
        validate();
    }
    
    private boolean updateData = false; //update lock
    public void updateData()
    {
        updateData = true;
        userData.setText(((StringUserData) event.getUserData()).getData());
        updateData = false;
    }
    
    public void saveEvent()
    {
        if (event != null && ! updateData)
        {
            ((StringUserData) event.getUserData()).setData(userData.getText());
            worldChangedUpdate();
        }
    }
    
    
    
    public void resetLabel()
    {
        if (event != null)
        {
            identifier.setText("Event: " + event.getIdentifier());
        }
    }    
        
    private void setTriggeredOnce()
    {
        event.setTriggerOnce(triggeredOnce.isSelected());        
    }
    
    private void showEvent()
    {
        event.setVisible(show.isSelected());
        worldChangedUpdate();
    }

    public DesignPhysicsEvent getEvent() 
    {
        return event;
    }

    
    public void setObject(DesignSelectionObject event)
    {
        if (! ( event instanceof DesignPhysicsEvent) )
        {
            return;
        }
        
        if (this.event != null && this.event instanceof DesignPhysicsEvent)
        {
            ((DesignPhysicsEvent) this.event).removeListener(this);
        }
        
        
        super.setObject(event);
            
        this.event = (DesignPhysicsEvent) event;
        if (this.event != null)
        {            
            this.event.addListener(this);            
            
            updateData();
        }
    }
        
    
    public void worldChanged(DesignWorld w)
    {        
    }
    
    public void updateRequired()
    {
        updateData();
    }
}
