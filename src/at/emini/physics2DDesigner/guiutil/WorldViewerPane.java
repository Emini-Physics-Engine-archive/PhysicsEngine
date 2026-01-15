package at.emini.physics2DDesigner.guiutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import at.emini.physics2D.util.PhysicsFileReader;
import at.emini.physics2DDesigner.DesignWorld;
import at.emini.physics2DDesigner.WorldViewer;

public abstract class WorldViewerPane extends JPanel
{

    private WorldViewer viewer;

    public WorldViewerPane(final File file)
    {
        setLayout(new BorderLayout());

        PhysicsFileReader reader = new PhysicsFileReader(file);
        DesignWorld world = DesignWorld.loadFromFile(reader);

        viewer = new WorldViewer(world)
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    createTab(file);

                    e.consume();
                }
                else
                {
                    super.mouseClicked(e);
                }
            }
        };

        viewer.setBorder(new LineBorder(Color.BLACK, 1));
        viewer.scaleTo(128, 128);

        add(viewer, BorderLayout.CENTER);

        JPanel hold = new JPanel(new BorderLayout());

        JLabel label = new JLabel(file.getName());
        hold.add(label, BorderLayout.CENTER);

        JButton buttonOpen = new JButton("Open");
        buttonOpen.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e)
            {
                createTab(file);
            }
        });
        hold.add(buttonOpen, BorderLayout.EAST);

        add(hold, BorderLayout.SOUTH);
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(128, 128 + 20);
    }

    @Override
    public Dimension getMaximumSize()
    {
        return new Dimension(128, 128 + 20);
    }

    public abstract void createTab(File file);
}
