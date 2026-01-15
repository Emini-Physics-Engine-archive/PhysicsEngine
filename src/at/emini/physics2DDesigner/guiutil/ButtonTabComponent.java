package at.emini.physics2DDesigner.guiutil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class ButtonTabComponent extends JPanel implements SaveChangeListener
{
    private final JTabbedPane pane;

    //private boolean saved = true;
    //private String displayName;
    private JLabel label;

    public ButtonTabComponent(final JTabbedPane pane, String initialName)
    {
        //unset default FlowLayout' gaps
        super(new BorderLayout());

        if (pane == null)
        {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);

        //make JLabel read titles from JTabbedPane
        label = new JLabel(initialName);

        add(label, BorderLayout.CENTER);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        JButton button = new TabButton();
        add(button, BorderLayout.EAST);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    public void close()
    {
        removePane();
    }

    public void removePane()
    {
        int i = pane.indexOfTabComponent(this);
        if (i > 0) {
            if (pane.getSelectedIndex() == i)
            {
                pane.setSelectedIndex(i - 1);
            }

            pane.remove(i);
        }
    }

    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            //int size = 17;
            //setPreferredSize(new Dimension(size, size));
            setToolTipText("Close");

            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);

            try
            {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_delete_sm.png") );
                setIcon(new ImageIcon(image));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }

        public void actionPerformed(ActionEvent e)
        {
            close();
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    @Override
    public void saveStateChanged(boolean saved, File file)
    {
        String text = (saved ? "" : "*") + ((file == null) ? "untitled" : file.getName());

        label.setText(text);
        validate();
        updateUI();
    }
}


