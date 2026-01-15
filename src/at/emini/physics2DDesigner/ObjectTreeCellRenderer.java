package at.emini.physics2DDesigner;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ObjectTreeCellRenderer extends DefaultTreeCellRenderer
{

    private static final long serialVersionUID = -5375528044980747255L;

    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);

        ObjectTreeNode node = (ObjectTreeNode) value;
        setIcon(node.getIcon());

        DesignSelectionObject object = node.getObject();
        if (object != null)
        {
            setText(object.toString());
        }

        return this;
    }

}
