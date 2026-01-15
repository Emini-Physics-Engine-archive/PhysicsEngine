package at.emini.physics2DDesigner;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;


public abstract class SelectionPanel extends JPanel
{

    private static final long serialVersionUID = -8536252621737921090L;

    protected JPanel selectionPanel;
    protected SelectionInfoPanel selectedInfoPanel;

    protected JToolBar toolBar;

    protected DesignWorld world;
    protected WorldDesigner worldDesigner;

    public SelectionPanel(DesignWorld world, WorldDesigner worldDesigner)
    {
        this.world = world;
        this.worldDesigner = worldDesigner;

        initComponents();
    }


    private void initComponents()
    {
        setLayout(new BorderLayout());

        toolBar = new JToolBar();

        add(toolBar, BorderLayout.PAGE_START);

        selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.PAGE_AXIS));
        JScrollPane listScroll = new JScrollPane(selectionPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(listScroll, BorderLayout.CENTER);

    }


    public SelectionInfoPanel getSelection()
    {
        return selectedInfoPanel;
    }

    public WorldDesigner getWorldDesigner()
    {
        return worldDesigner;
    }


    public void addPanel( SelectionInfoPanel newInfoPanel )
    {
        selectionPanel.add(newInfoPanel);
        infoPanelSelected( newInfoPanel );
        validate();

        selectionPanel.repaint();
        repaint();
    }

    public void removePanel( SelectionInfoPanel infoPanel )
    {
        selectionPanel.remove(infoPanel);
        infoPanelSelected(null);
        validate();

        selectionPanel.repaint();
        repaint();
    }

    public void infoPanelSelected(SelectionInfoPanel infoPanel)
    {
        if (infoPanel != null)
        {
            infoPanel.select();
        }
        if (selectedInfoPanel == infoPanel)
        {
            return;
        }
        if (getSelection() != null )
        {
            selectedInfoPanel.unselect();
            selectedInfoPanel.repaint();
        }

        selectedInfoPanel = infoPanel;
        if (infoPanel != null)
        {
            infoPanel.repaint();
        }
    }

    public void setWorld(DesignWorld world)
    {
        this.world = world;
    }

    public DesignWorld getWorld()
    {
        return world;
    }

}
