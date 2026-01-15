package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import at.emini.physics2D.MultiShape;

public class MultiShapeSelection extends JPanel
{
    private static final long serialVersionUID = -4129345362885297244L;

    private MultiShape shape;

    private Vector shapes;

    private JPanel selectionPanel;

    public MultiShapeSelection(MultiShape shape, Vector shapes, DesignWorld world)
    {
        this.shape = shape;

        this.shapes = shapes;

        initComponents(shapes, world);
    }

    private void initComponents(Vector shapes, DesignWorld world)
    {
        setLayout(new BorderLayout());

        removeAll();

        selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));

        for( int i = 0; i < shapes.size(); i++)
        {
            DesignShape s = (DesignShape) shapes.get(i);
            if (s instanceof MultiShape)
            {
                continue;
            }

            ShapeSelectViewer viewer = new ShapeSelectViewer(shape, s, world)
            {
                public Dimension getMaximumSize()
                {
                    return new Dimension(24,24);
                }
            };
            viewer.setSelected(((DesignMultiShape) shape).isSelected(s));
            selectionPanel.add(viewer);
        }

        add(selectionPanel, BorderLayout.CENTER);
    }

    public MultiShape getShape()
    {
        return shape;
    }


    public void checkShapes(DesignWorld world)
    {
        Vector shapes = world.getShapes();

        for( int i = shape.getShapeCount() - 1; i >= 0 ; i--)
        {
            if ( ! shapes.contains(shape.getShape(i)))
            {
                ((DesignMultiShape) shape).removeShape( (DesignShape) shape.getShape(i) );
            }
        }

        initComponents(shapes, world);
        validate();
        repaint();
    }
}
