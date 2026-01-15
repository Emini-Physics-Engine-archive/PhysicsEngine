package at.emini.physics2DDesigner;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

public class ObjectTreeNode extends DefaultMutableTreeNode
{

    private static final long serialVersionUID = 8637155384927519494L;
    
    private ImageIcon icon;
    private DesignSelectionObject object = null;
    
    public ObjectTreeNode(String text, ImageIcon icon)
    {
        super(text);
        this.icon = icon;
    }
    
    public ObjectTreeNode(String text, ImageIcon icon, DesignSelectionObject object)
    {
        super(text);
        this.icon = icon;
        this.object = object;
    }
    
    public ImageIcon getIcon()
    {
        return icon;
    }
    
    public void setObject(DesignSelectionObject object)
    {
        this.object = object;
    }
    
    public DesignSelectionObject getObject()
    {
        return object;
    }
    
    public ObjectTreeNode findNode(DesignSelectionObject object)
    {
        if (this.object != null && this.object.equals(object))
        {
            return this;
        }
        
        for( int i = 0; i < getChildCount(); i++)
        {
            ObjectTreeNode found = ((ObjectTreeNode) getChildAt(i)).findNode(object);
            if (found != null)
            {
                return found;
            }
        }
        
        return null;
    }
}
