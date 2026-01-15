package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class InfoPanel extends JPanel implements DesignObjectChangeListener
{
    private static final long serialVersionUID = -1781002635360840046L;

    protected JPanel header;
    protected JPanel details;
    protected JLabel identifier;
    protected JButton colorChooser;

    private boolean isSelected;

    private Color mainColor;
    private DesignSelectionObject object = null;
    private WorldDesigner worldDesigner;

    public InfoPanel(WorldDesigner designer)
    {
        worldDesigner = designer;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.black));

        mainColor = Color.lightGray;

        initComponents();
    }

    private void initComponents()
    {
        final InfoPanel thisPanel = this;
        header = new JPanel(new BorderLayout());
        identifier = new JLabel("");
        header.add(identifier, BorderLayout.CENTER);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try
        {

            Image image = ImageIO.read( getClass().getResourceAsStream("/res/button_colorpick.png") );

            colorChooser = new JButton(new ImageIcon(image, "Select Color"));
            colorChooser.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    selectColor();
                }
            });
            colorChooser.setMargin(new Insets(0,0,0,0));
            colorChooser.setToolTipText("Select the color of this item.");

            header.add(colorChooser, BorderLayout.EAST);
        }
        catch (IOException e)
        {

        }

        details = new JPanel();
        details.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        add(header, BorderLayout.NORTH);
        add(details, BorderLayout.CENTER);

        setMainColor(mainColor);
    }

    protected void disableColorChooser()
    {
        colorChooser.setVisible(false);
    }

    public void setIcon(ImageIcon icon)
    {
        identifier.setIcon(icon);
    }

    public void resetLabel(DesignSelectionObject object)
    {
        if (object != null)
        {
            identifier.setText( object.toString() );
        }
    }

    public Dimension getMaximumSize()
    {
        int height = details.getPreferredSize().height + header.getPreferredSize().height;
        return new Dimension(400,height);
    }

    private void selectColor()
    {
        Color newColor = JColorChooser.showDialog(this, "Select color", mainColor);
        if (newColor != null)
        {
            object.setColor(newColor);
            setMainColor(newColor);
            worldChangedUpdate();
        }
    }

    private void setMainColor(Color c)
    {
        mainColor = c;
        Color backgroundColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 150);
        header.setBackground(backgroundColor);
    }

    protected abstract void updateData();

    public void designObjectChanged(DesignSelectionObject object)
    {
        resetLabel(object);
        updateData();
    }

    public void setObject(DesignSelectionObject object)
    {
        if (this.object != null)
        {
            this.object.removeListener(this);
        }

        if (object != null)
        {
            resetLabel(object);
            setMainColor(object.getColor());
            object.addListener(this);

            this.object = object;
        }
    }

    protected void worldChangedUpdate()
    {
        worldDesigner.getWorld().worldChanged();
    }

}
