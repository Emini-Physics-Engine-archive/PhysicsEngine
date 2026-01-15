package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class SelectionInfoPanel extends JPanel implements FocusListener, MouseListener
{
    private static final long serialVersionUID = -1781002635360840046L;

    protected JPanel header;
    protected JPanel details;
    protected JLabel identifier;

    private boolean isSelected;

    private Color mainColor;
    private Color selectedColor;

    protected SelectionPanel selectionPanel = null;

    public SelectionInfoPanel(SelectionPanel selectionPanel, Color c1, Color c2 )
    {
        this.selectionPanel = selectionPanel;
        setLayout(new BorderLayout());

        mainColor = c1;
        selectedColor = c2;

        initComponents();

        addFocusListener(this);
        identifier.addMouseListener(this);
    }

    private void initComponents()
    {
        final SelectionInfoPanel thisPanel = this;
        header = new JPanel(new BorderLayout());
        identifier = new JLabel("");
        identifier.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                select();
                selectionPanel.infoPanelSelected(thisPanel);
            }
        });


        header.add(identifier, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        details = new JPanel();

        add("Center", details);
        details.setVisible(false);

        setHeaderColor(mainColor);
    }

    public abstract void resetLabel();

    public Dimension getMaximumSize()
    {
        int height = (isSelected ? details.getPreferredSize().height : 0)
            + header.getPreferredSize().height;
        return new Dimension(400,height);
    }


    public void unselect()
    {
        isSelected = false;
        updateVisibility();
    }

    public void select()
    {
        isSelected = true;
        updateVisibility();
    }

    private void updateVisibility()
    {
        details.setVisible(isSelected);
    }

    public void focusGained(FocusEvent e)
    {
        select();
        if (selectionPanel != null)
        {
            selectionPanel.infoPanelSelected(this);
        }
    }

    public void focusLost(FocusEvent e) {}

    public void mouseClicked(MouseEvent e){}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e)
    {
        setHeaderColor(selectedColor);
    }

    public void mouseExited(MouseEvent e)
    {
        setHeaderColor(mainColor);
    }

    private void setHeaderColor(Color c)
    {
        header.setBackground(c);
    }

}
