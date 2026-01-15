package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import at.emini.physics2D.util.PhysicsFileReader;
import at.emini.physics2DDesigner.guiutil.ButtonTabComponent;
import at.emini.physics2DDesigner.guiutil.FileNode;
import at.emini.physics2DDesigner.guiutil.FileTree;
import at.emini.physics2DDesigner.guiutil.WorldViewerPane;

public class DesignerManager extends JFrame implements ChangeListener
{
    //public static File stdDir = new File(".");
    public static File stdDir = new File("C:/eigenes/Projekte/physengine/aaphys/misc/designerfiles");
    
    /**
     * Serial id
     */
    private static final long serialVersionUID = 9165118103326342150L;
    
    private FileTree fileTree;
    private JPanel worldDisplay;
    
    private JTabbedPane mainPane;
    private Designer currentDesigner = null;
    
    public DesignerManager()
    {
        super("Emini Physics 2D Designer");
        
        setLayout(new BorderLayout());
        
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        setLocation( 100, 100);
        addWindowListener (new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) {
                closeWindow();
            }
        });
        
        initComponents();
        pack();
    }
    
    private void initComponents()
    {
        mainPane = new JTabbedPane();
                
        
        JPanel selection = new JPanel();
        selection.setLayout( new BorderLayout( 10, 10) );
        
        fileTree = new FileTree();
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) 
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
                .getPath().getLastPathComponent();
                nodeSelected(node);
            }
        });

        selection.add(fileTree, BorderLayout.WEST);
        
        worldDisplay = new JPanel();
        
        selection.add(new JScrollPane(worldDisplay), BorderLayout.CENTER);
        
        mainPane.addTab("Select", selection);
        mainPane.addTab("+", null);
        
        mainPane.addChangeListener(this);
                
        add(mainPane, BorderLayout.CENTER);
    }
    
    private void closeWindow()
    {
        //check all panes
        
        for( int i = 1; i < mainPane.getTabCount() - 1; i++)
        {
            if (! (mainPane.getComponent(i) instanceof Designer))
            {
                continue;
            }
            
            Designer designer = (Designer) mainPane.getComponent(i);
            if (designer == null)
            {
                continue;
            }
            
            if ( ! designer.checkSaved() )
            {
                return;
            } 
        }
        
        dispose();
        System.exit(0);
    }
    
    private void nodeSelected(DefaultMutableTreeNode node)
    {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        worldDisplay.removeAll();
     
        FileNode fileNode = fileTree.getFileNode(node);
        File dir = fileNode.getFile();
        
        String curPath = dir.getPath();
        Vector ol = new Vector();
        String[] tmp = dir.list();
        for (int i = 0; i < tmp.length; i++)
            ol.addElement(tmp[i]);
        Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
        
        File f;
        Vector files = new Vector();
        for (int i = 0; i < ol.size(); i++) {
            String thisObject = (String) ol.elementAt(i);
            String newPath;
            if (curPath.equals("."))
                newPath = thisObject;
            else
                newPath = curPath + File.separator + thisObject;
            if (! (f = new File(newPath)).isDirectory())
            {
                int index = newPath.lastIndexOf(".");
                if ( index > 0 && newPath.substring(index).equals(".phy"))
                {
                    files.addElement(f);
                }
            }
        }
        
        int cols = 4;
        
        worldDisplay.setLayout(new GridLayout(((files.size() - 1)/ 4) + 1, 4, 10, 10));
        for( int i = 0; i < files.size(); i++)
        {
            final File file = (File) files.get(i); 
            
            WorldViewerPane viewer = new WorldViewerPane(file)
            {
                public void createTab(File file)
                {
                    createNewTab(file);
                }
            };            
                    
            worldDisplay.add(viewer);
            worldDisplay.updateUI();
            worldDisplay.repaint();
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        worldDisplay.updateUI();
        worldDisplay.repaint();
    }
    
    private void createNewTab(File file)
    {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        mainPane.removeChangeListener(this);
        
        final Designer newDesigner = file == null ? new Designer() : new Designer(file);        
        String name = file != null ? file.getName() : "untitled"; 
            
        int index = mainPane.getTabCount() - 1;
        mainPane.insertTab(name, null, newDesigner, name, index);
        ButtonTabComponent tab = new ButtonTabComponent(mainPane, name)
        {
            public void close()
            {
                if ( newDesigner.checkSaved())
                {
                    removePane();
                }
            }
        }; 
        mainPane.setTabComponentAt(index, tab);
        newDesigner.setSaveChangeListener(tab);
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        mainPane.addChangeListener(this);
        mainPane.setSelectedIndex(index);
    }
    
    public static void main(String[] args)
    {
        DesignerManager designer = new DesignerManager();
        designer.setSize( 800, 600 );
        designer.setVisible( true );
    }

    public void stateChanged(ChangeEvent e)
    {
        if (currentDesigner != null)
        {
            currentDesigner.pauseSimulation();
        }
        
        if (mainPane.getSelectedComponent() instanceof Designer)
        {
            currentDesigner = (Designer) mainPane.getSelectedComponent();
        }
        else if (mainPane.getSelectedIndex() == mainPane.getTabCount() - 1)
        {
            currentDesigner = null;
            
            createNewTab(null);                       
            
        }
        
    }

}
