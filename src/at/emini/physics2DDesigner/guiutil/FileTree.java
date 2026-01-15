package at.emini.physics2DDesigner.guiutil;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;

public class FileTree extends JPanel 
{
    private static final long serialVersionUID = 2636670125682697966L;
   
    protected JTree  m_tree;
    protected DefaultTreeModel m_model;
    protected JTextField m_display;

    public FileTree()
    {
        setLayout(new BorderLayout());
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(
                new IconData(null, null, "Computer"));
        
        FileSystemView fsv = FileSystemView.getFileSystemView();
        
        File[] roots = fsv.getRoots();
        DefaultMutableTreeNode node;
        for( int i = 0; i < roots.length; i++)
        {
            node = new DefaultMutableTreeNode(new IconData(fsv.getSystemIcon(roots[i]), 
                    null, new FileNode(roots[i]), fsv.getSystemDisplayName(roots[i])));
            node.add( new DefaultMutableTreeNode(new Boolean(true)));
            top.add(node);
        }
        
        m_model = new DefaultTreeModel(top);
        m_tree = new JTree(m_model);

        m_tree.putClientProperty("JTree.lineStyle", "Angled");

        TreeCellRenderer renderer = new 
        IconCellRenderer();
        m_tree.setCellRenderer(renderer);

        m_tree.addTreeExpansionListener(new 
                DirExpansionListener());

        m_tree.addTreeSelectionListener(new 
                DirSelectionListener());

        m_tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION); 
        m_tree.setShowsRootHandles(true); 
        m_tree.setEditable(false);
        m_tree.setRootVisible(false);

        JScrollPane s = new JScrollPane();
        s.getViewport().add(m_tree);
        add(s, BorderLayout.CENTER);

        m_display = new JTextField();
        m_display.setEditable(false);
        add(m_display, BorderLayout.NORTH);

        
        setVisible(true);
    }

    public void addTreeSelectionListener(TreeSelectionListener listener)
    {
        m_tree.addTreeSelectionListener(listener);
    }
    
    DefaultMutableTreeNode getTreeNode(TreePath path)
    {
        return (DefaultMutableTreeNode)(path.getLastPathComponent());
    }
    
    public void expand(File file)
    {   
        if (file == null)
        {
            return;
        }
        
        
        //m_tree.expandPath(path);
    }

    public FileNode getFileNode(DefaultMutableTreeNode node)
    {
        if (node == null)
            return null;
        Object obj = node.getUserObject();
        if (obj instanceof IconData)
            obj = ((IconData)obj).getObject();
        if (obj instanceof FileNode)
            return (FileNode)obj;
        else
            return null;
    }
    
    public Dimension getPreferredSize()
    {
        return new Dimension(300,400);
    }

    // Make sure expansion is threaded and updating the tree model
    // only occurs within the event dispatching thread.
    class DirExpansionListener implements TreeExpansionListener
    {
        public void treeExpanded(TreeExpansionEvent event)
        {
            final DefaultMutableTreeNode node = getTreeNode(
                    event.getPath());
            final FileNode fnode = getFileNode(node);

            Thread runner = new Thread() 
            {
                public void run() 
                {
                    if (fnode != null && fnode.expand(node)) 
                    {
                        Runnable runnable = new Runnable() 
                        {
                            public void run() 
                            {
                                m_model.reload(node);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                }
            };
            runner.start();
        }

        public void treeCollapsed(TreeExpansionEvent event) {}
    }


    class DirSelectionListener 
    implements TreeSelectionListener 
    {
        public void valueChanged(TreeSelectionEvent event)
        {
            DefaultMutableTreeNode node = getTreeNode(
                    event.getPath());
            FileNode fnode = getFileNode(node);
            if (fnode != null)
                m_display.setText(fnode.getFile().
                        getAbsolutePath());
            else
                m_display.setText("");
        }
    }

}

class IconCellRenderer 
extends    JLabel 
implements TreeCellRenderer
{
    protected Color m_textSelectionColor;
    protected Color m_textNonSelectionColor;
    protected Color m_bkSelectionColor;
    protected Color m_bkNonSelectionColor;
    protected Color m_borderSelectionColor;

    protected boolean m_selected;

    public IconCellRenderer()
    {
        super();
        m_textSelectionColor = UIManager.getColor(
        "Tree.selectionForeground");
        m_textNonSelectionColor = UIManager.getColor(
        "Tree.textForeground");
        m_bkSelectionColor = UIManager.getColor(
        "Tree.selectionBackground");
        m_bkNonSelectionColor = UIManager.getColor(
        "Tree.textBackground");
        m_borderSelectionColor = UIManager.getColor(
        "Tree.selectionBorderColor");
        setOpaque(false);
    }

    public Component getTreeCellRendererComponent(JTree tree, 
            Object value, boolean sel, boolean expanded, boolean leaf, 
            int row, boolean hasFocus) 

    {
        DefaultMutableTreeNode node = 
            (DefaultMutableTreeNode)value;
        Object obj = node.getUserObject();
        setText(obj.toString());

        if (obj instanceof Boolean)
            setText("Retrieving data...");

        if (obj instanceof IconData)
        {
            IconData idata = (IconData)obj;
            if (expanded)
                setIcon(idata.getExpandedIcon());
            else
                setIcon(idata.getIcon());
        }
        else
            setIcon(null);

        setFont(tree.getFont());
        setForeground(sel ? m_textSelectionColor : 
            m_textNonSelectionColor);
        setBackground(sel ? m_bkSelectionColor : 
            m_bkNonSelectionColor);
        m_selected = sel;
        return this;
    }

    public void paintComponent(Graphics g) 
    {
        Color bColor = getBackground();
        Icon icon = getIcon();

        g.setColor(bColor);
        int offset = 0;
        if(icon != null && getText() != null) 
            offset = (icon.getIconWidth() + getIconTextGap());
        g.fillRect(offset, 0, getWidth() - 1 - offset,
                getHeight() - 1);

        if (m_selected) 
        {
            g.setColor(m_borderSelectionColor);
            g.drawRect(offset, 0, getWidth()-1-offset, getHeight()-1);
        }
        super.paintComponent(g);
    }
}

class IconData
{
    protected Icon   m_icon;
    protected Icon   m_expandedIcon;
    protected Object m_data;
    protected String m_text;

    public IconData(FileNode node)
    {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        
        m_icon = fsv.getSystemIcon(node.getFile());
        m_expandedIcon = fsv.getSystemIcon(node.getFile());
        m_data = node;
        m_text = fsv.getSystemDisplayName(node.getFile());
    }
    
    public IconData(Icon icon, Object data, String text)
    {
        m_icon = icon;
        m_expandedIcon = null;
        m_data = data;
        m_text = text;
    }

    public IconData(Icon icon, Icon expandedIcon, Object data, String text)
    {
        m_icon = icon;
        m_expandedIcon = expandedIcon;
        m_data = data;
        m_text = text;
    }

    public Icon getIcon() 
    { 
        return m_icon;
    }

    public Icon getExpandedIcon() 
    { 
        return m_expandedIcon!=null ? m_expandedIcon : m_icon;
    }

    public Object getObject() 
    { 
        return m_data;
    }

    public String toString() 
    {         
        return m_text;
    }
}

