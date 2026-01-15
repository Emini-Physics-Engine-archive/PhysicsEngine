package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class ObjectTreePanel extends JPanel implements TreeSelectionListener
{
    private static final long serialVersionUID = 4005370997456450671L;

    private JTree tree;
    private WorldDesigner worldDesigner = null;
    
    public ObjectTreePanel(DesignWorld world, WorldDesigner designer)
    {        
        this.worldDesigner = designer;
        initComponents(world.getObjectTreeModel());
        setWorld(world);
    }
        
    private void initComponents(TreeModel model)
    {
        setLayout(new BorderLayout());
        
        tree = new JTree(model)
        {
            public boolean isPathEditable(TreePath path) 
            { 
                if (isEditable()) 
                { 
                    return ((ObjectTreeNode) path.getLastPathComponent()).getObject() != null;
                }
                return false;
              }
        };
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.setCellRenderer(new ObjectTreeCellRenderer());
        tree.setCellEditor(new ObjectTreeCellEditor(worldDesigner));
        tree.addTreeSelectionListener(this);
        
        JScrollPane scrollpane = new JScrollPane(tree);
        add(scrollpane, BorderLayout.CENTER);
    }

    public void setWorld(DesignWorld world)
    {
        tree.setModel(world.getObjectTreeModel());        
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(300,400);
    }
    
    public void select(DesignSelectionObject object)
    {
        
        ObjectTreeNode node = ((ObjectTreeNode) tree.getModel().getRoot()).findNode(object);
        if (node != null)
        {
            tree.startEditingAtPath( new TreePath(node.getPath()) );
        }
    }

    public void valueChanged(TreeSelectionEvent e)
    {
        if ( e.getNewLeadSelectionPath() != null && e.getNewLeadSelectionPath().getLastPathComponent() instanceof ObjectTreeNode )
        {
            DesignSelectionObject object = 
                ((ObjectTreeNode) e.getNewLeadSelectionPath().getLastPathComponent()).getObject(); 
            if ( object != null && worldDesigner != null)
            {
                worldDesigner.setSelection(object);
            }
        }
    }
   
}
