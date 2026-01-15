package at.emini.physics2DDesigner;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import at.emini.physics2D.Body;

public class ScriptSelectionPanel extends SelectionPanel
{
    private static final long serialVersionUID = 6924270775653158453L;

    private JButton addScript;
    private JButton removeScript;
    private JButton applyScript;

    public ScriptSelectionPanel(DesignWorld world, WorldDesigner designer)
    {
        super(world, designer);
        
        initActions();
        initComponents();
    }
    
    private void  initActions()
    {
        try
        {        
            Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_new_script.png") );
            addScript = new JButton(new ImageIcon(image));
            addScript.addActionListener(new ActionListener(){
            
                public void actionPerformed(ActionEvent e) {
                    addScript(new DesignScript(false));                
                }
            });
            addScript.setToolTipText("Add new Script");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_delete_script.png") );
            removeScript = new JButton(new ImageIcon(image));
            removeScript.addActionListener(new ActionListener(){
            
                public void actionPerformed(ActionEvent e) {
                    removeScript();                
                }
            });
            removeScript.setToolTipText("Remove current Script");
            
            image = ImageIO.read( getClass().getResourceAsStream("/res/button_apply_script.png") );
            applyScript = new JButton(new ImageIcon(image));
            applyScript.addActionListener(new ActionListener(){
            
                public void actionPerformed(ActionEvent e) {
                    applyScript();                
                }
            });
            applyScript.setToolTipText("Apply Script to selected Body");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void initComponents()
    {
        toolBar.add(addScript);
        toolBar.add(removeScript);
        toolBar.add(applyScript);        
    }
    
    private void addScript(DesignScript script)
    {
        world.addScript(script);
        
        ScriptInfoPanel infoPanel = new ScriptInfoPanel(script, this);                
        addPanel(infoPanel);
    }
    
    private void removeScript()
    {
        if (selectedInfoPanel != null)
        {
            world.removeScript(((ScriptInfoPanel) selectedInfoPanel).getScript());
            removePanel(selectedInfoPanel);
            resetLabels();            
        }
    }
    
    private void applyScript()
    {
        if ( getWorldDesigner().getSelection() instanceof Body)
        {
            if (selectedInfoPanel != null)
            {
                ((ScriptInfoPanel) selectedInfoPanel).getScript().applyToBody((Body)getWorldDesigner().getSelection(), world);
            }            
        }
    }
  
    public void setWorld(DesignWorld world) 
    {
        super.setWorld(world);
        setScripts(world);
    }
   
    private void setScripts(DesignWorld world) 
    {        
        selectionPanel.removeAll();
        
        for( int i = 0; i < world.getScriptCount(); i++)
        {
            if (world.getScript(i) instanceof DesignScript)
            {
                //addScript( (DesignScript) world.getScript(i) );
                ScriptInfoPanel infoPanel = new ScriptInfoPanel((DesignScript) world.getScript(i), this);                
                addPanel(infoPanel);
            }
        }
    }
    
    private void resetLabels()
    {   
        for( int i = 0; i < world.getScriptCount();i++)
        {
            System.out.println(i);
            ((DesignScript) world.getScript(i)).index = i;
        }
        DesignScript.scriptIndex = world.getScriptCount();
        
        for( int i = 0; i < selectionPanel.getComponentCount(); i++)
        {   
            ((ScriptInfoPanel) selectionPanel.getComponent(i)).resetLabel();
        }
    }

}
