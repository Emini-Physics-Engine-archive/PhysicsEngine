package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * SplitButton class that provides a drop down menu when the right side arrow is
 * clicked. Written by Edward Scholl (edscholl@atwistedweb.com)- use as you
 * wish, but a acknowlegement would be appreciated if you use this...
 * 
 * @author Edward Scholl
 */
public class MultiActionButton extends JToolBar implements ActionListener
{
    private static final long serialVersionUID = -728093104564453315L;
    
    private static final String uiClassID = "ToolBarUI";
    
    private JToggleButton mainButton; 
    private JButton dropDownButton;
    private JPopupMenu dropDownMenu;
    
    private int actionId = 0;
    
    /**
     * Default Constructor that creates a blank button with a down facing arrow.
     */
    public MultiActionButton()
    {
        this("");
        setFloatable(false);
        
    }

    /**
     * Creates a button with the specified text and a down facing arrow.
     * 
     * @param text
     *            String
     */
    public MultiActionButton(String text)
    {
        this(new JToggleButton(text), SwingConstants.SOUTH);
        setFloatable(false);
    }

    /**
     * Creates a button with the specified text and a arrow in the specified
     * direction.
     * 
     * @param text
     *            String
     * @param orientation
     *            int
     */
    public MultiActionButton(String text, int orientation)
    {
        this(new JToggleButton(text), orientation);
        setFloatable(false);
    }

    /**
     * Passes in the button to use in the left hand side, with the specified
     * orientation for the arrow on the right hand side.
     * 
     * @param mainButton
     *            JButton
     * @param orientation
     *            int
     */
    public MultiActionButton(JToggleButton mainButton, int orientation)
    {        
        this.mainButton = mainButton;
        setFloatable(false);

        dropDownButton = new BasicArrowButton(orientation);
        dropDownButton.addActionListener(this);

        setBorderPainted(true);
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        dropDownButton.setBorderPainted(true);
        mainButton.setBorderPainted(false);

        Dimension mainButtonPrefSize = mainButton.getPreferredSize();
        Dimension dropDownButtonPrefSize = dropDownButton.getPreferredSize();
        this.setPreferredSize(new Dimension(mainButtonPrefSize.width + dropDownButtonPrefSize.width, mainButtonPrefSize.height));

        Dimension mainButtonMinSize = mainButton.getMinimumSize();
        Dimension dropDownButtonMinSize = dropDownButton.getMinimumSize();
        this.setMaximumSize(new Dimension(mainButtonMinSize.width + dropDownButtonMinSize.width, mainButtonMinSize.height));
        
        Dimension mainButtonMaxSize = mainButton.getMaximumSize();
        Dimension dropDownButtonMaxSize = dropDownButton.getMaximumSize();
        this.setMinimumSize(new Dimension(mainButtonMaxSize.width + dropDownButtonMaxSize.width, mainButtonMaxSize.height));

        this.setLayout(new BorderLayout());
        //this.setMargin(new Insets(-1, -1, -1, -1));

        this.add(mainButton, BorderLayout.CENTER);
        this.add(dropDownButton, BorderLayout.EAST);
        
    }

    public void setAction(int actionId, ImageIcon imageIcon, String toolTip)
    {
        this.actionId = actionId;
        mainButton.setIcon(imageIcon);
        mainButton.setToolTipText(toolTip);
        mainButton.setSelected(true);
    }
    
    public int getActionId()
    {
        return actionId;
    }
    
    /*public String getUIClassID() {
        return uiClassID;
    }*/
    
    /**
     * Sets the popup menu to show when the arrow is clicked.
     * 
     * @param menu
     *            JPopupMenu
     */
    public void setMenu(JPopupMenu menu)
    {
        this.dropDownMenu = menu;
    }

    /**
     * returns the main (left hand side) button.
     * 
     * @return JButton
     */
    public JToggleButton getMainButton()
    {
        return mainButton;
    }

    /**
     * gets the drop down button (with the arrow)
     * 
     * @return JButton
     */
    public JButton getDropDownButton()
    {
        return dropDownButton;
    }

    /**
     * gets the drop down menu
     * 
     * @return JPopupMenu
     */
    public JPopupMenu getMenu()
    {
        return dropDownMenu;
    }

    /**
     * action listener for the arrow button- shows / hides the popup menu.
     * 
     * @param e
     *            ActionEvent
     */
    public void actionPerformed(ActionEvent e)
    {
        if (this.dropDownMenu == null)
        {
            return;
        }
        
        if (!dropDownMenu.isVisible())
        {
            Point p = this.getLocationOnScreen();
            dropDownMenu.show(mainButton, (int) 0, (int) 0
                    + this.getHeight());
        }
        else
        {
            dropDownMenu.setVisible(false);
        }
    }

    /**
     * adds a action listener to this button (actually to the left hand side
     * button, and any left over surrounding space. the arrow button will not be
     * affected.
     * 
     * @param al
     *            ActionListener
     */
    public void addActionListener(ActionListener al)
    {
        this.mainButton.addActionListener(al);
        //super.addActionListener(al);
    }
    
    
    
}