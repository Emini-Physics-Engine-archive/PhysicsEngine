package at.emini.physics2DDesigner;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreeCellEditor;

public class ObjectTreeCellEditor implements TreeCellEditor
{
    private static final long serialVersionUID = -5375528044980747256L;

    private Vector listeners = new Vector();
    private BodyInfoPanel bodyInfoPanel;
    private SpringInfoPanel springInfoPanel;
    private MotorInfoPanel motorInfoPanel;
    private LandscapeInfoPanel landscapeInfoPanel;
    private ParticleInfoPanel particleInfoPanel;
    private JointInfoPanel jointInfoPanel;
    private WorldInfoPanel parameterInfoPanel;
    private BodyAreaEventInfoPanel bodyAreaInfoPanel;

    private JLabel defaultPanel;

    public ObjectTreeCellEditor(WorldDesigner worldDesigner)
    {
        defaultPanel = new JLabel();

        bodyInfoPanel = new BodyInfoPanel(worldDesigner);
        landscapeInfoPanel = new LandscapeInfoPanel(worldDesigner);
        springInfoPanel = new SpringInfoPanel(worldDesigner);
        motorInfoPanel = new MotorInfoPanel(worldDesigner);
        particleInfoPanel = new ParticleInfoPanel(worldDesigner);
        jointInfoPanel = new JointInfoPanel(worldDesigner);
        parameterInfoPanel = new WorldInfoPanel(worldDesigner);
        bodyAreaInfoPanel = new BodyAreaEventInfoPanel(worldDesigner);

    }

    public Component getTreeCellEditorComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row)
    {
        ObjectTreeNode node = (ObjectTreeNode) value;
        DesignSelectionObject object = ((ObjectTreeNode) value).getObject();

        InfoPanel infoPanel = null;
        if (object instanceof DesignBody)
        {
            infoPanel = bodyInfoPanel;
        }
        else if (object instanceof DesignLandscape)
        {
            infoPanel = landscapeInfoPanel;
        }
        else if (object instanceof DesignSpring)
        {
            infoPanel = springInfoPanel;
        }
        else if (object instanceof DesignMotor)
        {
            infoPanel = motorInfoPanel;
        }
        else if (object instanceof DesignParticleEmitter)
        {
            infoPanel = particleInfoPanel;
        }
        else if (object instanceof DesignJoint)
        {
            infoPanel = jointInfoPanel;
        }
        else if (object instanceof DesignParameter)
        {
            infoPanel = parameterInfoPanel;
        }
        else if (object instanceof DesignAreaEvent)
        {
            infoPanel = bodyAreaInfoPanel;
        }

        if (infoPanel != null)
        {
            infoPanel.setObject(object);
            infoPanel.setIcon(node.getIcon());
            return infoPanel;
        }

        defaultPanel.setIcon(node.getIcon());
        if (object != null)
        {
            defaultPanel.setText(object.toString());
        }
        else
        {
            defaultPanel.setText(node.toString());
        }

        return defaultPanel;
    }

    public void addCellEditorListener(CellEditorListener l)
    {
        listeners.add(l);
    }

    public void cancelCellEditing()
    {
        for( int i = 0; i < listeners.size(); i++)
        {
            ((CellEditorListener) listeners.get(i)).editingCanceled(new ChangeEvent(this));
        }
    }

    public Object getCellEditorValue()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isCellEditable(EventObject anEvent)
    {
        if ( anEvent instanceof InputEvent)
        {
            ((InputEvent) anEvent).consume();
        }
        return true;
        /*
        if ( anEvent instanceof MouseEvent && anEvent.getSource() instanceof JTree)
        {
            MouseEvent e = (MouseEvent) anEvent;
            TreePath path = ((JTree) anEvent.getSource()).getPathForLocation(e.getX(), e.getY());
            if (path != null)
            {
                return ((ObjectTreeNode) path.getLastPathComponent()).getObject() != null;
            }
        }
        return false;*/
    }

    public void removeCellEditorListener(CellEditorListener l)
    {
        listeners.remove(l);
    }

    public boolean shouldSelectCell(EventObject anEvent)
    {
        return true;
    }

    public boolean stopCellEditing()
    {
        for( int i = 0; i < listeners.size(); i++)
        {
            ((CellEditorListener) listeners.get(i)).editingStopped(new ChangeEvent(this));
        }
        return true;    //every input is ok
    }

}
