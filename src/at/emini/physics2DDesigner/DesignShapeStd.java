package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JMenuItem;

import at.emini.physics2D.Shape;
import at.emini.physics2D.UserData;
import at.emini.physics2D.World;
import at.emini.physics2D.util.FXVector;
import at.emini.physics2D.util.PhysicsFileReader;


public class DesignShapeStd extends Shape implements DesignShape
{
    private Vector designVertices;
    private Vector designEdges;
    private String name = "Unnamed Shape";

    private int centerSize = 2; //FX = 2 << FXUtil.DECIMAL;

    protected FXVector[] cornersConvexCopy;

    protected Color c;

    private static int currColor = 0;
    private static final Color defaultColors[] = {
        new Color(180,   0,   0, 50),
        new Color(180, 180,   0, 50),
        new Color(  0, 180,   0, 50),
        new Color(  0, 180, 180, 50),
        new Color(  0,   0, 180, 50),
        new Color(180,   0, 180, 50) };

    private DesignShapeStd(FXVector[] vertices)
    {
        super(vertices);
        initShapeElements();

        mUserData = new StringUserData();

        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    protected DesignShapeStd(Shape shape)
    {
        super(shape.getCorners());
        setElasticityFX(shape.getElasticityFX());
        setFrictionFX(shape.getFrictionFX());
        setMassFX(shape.getMassFX());

        initShapeElements();

        if( shape.getUserData() != null)
        {
            mUserData = shape.getUserData().copy();
        }
        else
        {
            mUserData = new StringUserData();
        }

        c = defaultColors[currColor];
        currColor = (currColor + 1) % defaultColors.length;
    }

    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#getName()
     */
    public String getName()
    {
        return name;
    }


    public String toString()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#getColor()
     */
    public Color getColor()
    {
        return c;
    }

    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#setColor(java.awt.Color)
     */
    public void setColor(Color c)
    {
        this.c = c;
    }

    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#getOpaqueColor()
     */
    public Color getOpaqueColor()
    {
        return DesignerUtilities.getGrayBlendColor(c);
    }

    private void initShapeElements()
    {
        designVertices = new Vector();
        designEdges= new Vector();

        FXVector[] vertices = getCorners();
        for( int i = 0; i < vertices.length; i++)
        {
            designVertices.addElement(new ShapeVertex(vertices[i]) );
        }

        for( int i = 0; i < vertices.length; i++)
        {
            designEdges.addElement(new ShapeEdge( (ShapeVertex) designVertices.get(i),
                    (ShapeVertex) designVertices.get((i + 1) % designVertices.size())) );
        }

        cornersConvexCopy = new FXVector[mVertices.length];
        for( int i = 0; i < vertices.length; i++)
        {
            cornersConvexCopy[i] = new FXVector(vertices[i]);
        }
    }


    public static DesignShapeStd createDesignPolygon(int radius, int vertices)
    {
        Shape s = Shape.createRegularPolygon(radius, vertices);
        return new DesignShapeStd(s);
    }


    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#draw(at.emini.physics2DDesigner.GraphicsWrapper, boolean)
     */
    public void draw(GraphicsWrapper g, boolean edit)
    {
        if (edit)
        {
            for( int i = 0; i< designVertices.size(); i++)
            {
                ((ShapeVertex) designVertices.get(i)).draw(g);
            }
        }

        for( int i = 0; i < designEdges.size(); i++)
        {
            ((ShapeEdge) designEdges.get(i)).draw(g);
        }

        if (designVertices.size() == 1)
        {
            g.drawArc(-getBoundingRadiusFX(), -getBoundingRadiusFX(), getBoundingRadiusFX() * 2, getBoundingRadiusFX() * 2, 0, 360);
        }

        //centroid
        if (edit)
        {
            double zoomScale = g.getZoomScale();
            g.setColor(Color.green);
            g.drawLine((int)(-centerSize / zoomScale), 0, (int) (centerSize / zoomScale), 0);
            g.drawLine( 0,(int)(-centerSize / zoomScale), 0, (int) (centerSize / zoomScale) );
        }
    }

    public ShapeElement checkElements(GraphicsWrapper g, Point2D point)
    {
        for( int i = 0; i < designVertices.size(); i++)
        {
            if ( ((ShapeVertex) designVertices.get(i)).checkPoint(g, point) )
            {
                return ((ShapeVertex) designVertices.get(i));
            }
        }

        for( int i = 0; i < designEdges.size(); i++)
        {
            if ( ((ShapeEdge) designEdges.get(i)).checkPoint(g, point) )
            {
                return ((ShapeEdge) designEdges.get(i));
            }
        }
        return null;
    }

    public void deleteVertex(ShapeVertex vertex)
    {
        if (mVertices.length <= 3)
        {
            return;
        }
        for( int i = 0; i < designVertices.size(); i++)
        {
            if (designVertices.get(i) == vertex)
            {
                ShapeEdge edge1 = (ShapeEdge) designEdges.get( i );
                ShapeEdge edge2 = (ShapeEdge) designEdges.get( (i - 1 + designEdges.size()) % designEdges.size());

                edge2.setEnd(edge1.getEnd());

                designVertices.remove(i);
                designEdges.remove(i);

                updateCorners();
                return;
            }
        }
    }

    public void insertVertex(ShapeEdge edge, Point2D pos)
    {
        if (mVertices.length == World.M_SHAPE_MAX_VERTICES)
        {
            return;
        }
        for( int i = 0; i < designEdges.size(); i++)
        {
            if (designEdges.get(i) == edge)
            {
                ShapeVertex newVertex;
                if (pos == null)
                {
                    newVertex = new ShapeVertex(edge.getStart(), edge.getEnd());
                }
                else
                {
                    newVertex = new ShapeVertex(pos);
                }
                ShapeEdge newEdge = new ShapeEdge(newVertex, edge.getEnd());
                edge.setEnd(newVertex);

                int index = (i + 1) % designEdges.size();

                designVertices.add( index, newVertex);
                designEdges.add( index, newEdge);

                updateCorners();
                return;
            }
        }
    }

    private void updateCorners()
    {
        mVertices = new FXVector[designVertices.size()];

        for( int i = 0; i < designVertices.size(); i++)
        {
            mVertices[i] = ((ShapeVertex) designVertices.get(i)).getFXVector();
        }

        cornersConvexCopy = new FXVector[mVertices.length];
        for( int i = 0; i < mVertices.length; i++)
        {
            cornersConvexCopy[i] = new FXVector(mVertices[i]);
        }

        update();
    }

    public JMenuItem[] getMenuItems(ShapeElement currentElement)
    {
        JMenuItem[] item = new JMenuItem[1];

        item[0] = new JMenuItem();
        if (currentElement instanceof ShapeVertex)
        {
            final ShapeVertex vertex = (ShapeVertex) currentElement;
            item[0].setText("Delete Vertex");
            item[0].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                   deleteVertex( vertex );
                }
            });
        }
        if (currentElement instanceof ShapeEdge)
        {
            final ShapeEdge edge = (ShapeEdge) currentElement;
            item[0].setText("New Vertex");
            item[0].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                   insertVertex( edge, null );
                }
            });
        }

        return item;
    }

    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#saveToFile(java.io.File)
     */
    public void saveToFile(File file)
    {
        try
        {
            MyFileWriter fileWriter = new MyFileWriter( file );

            saveToFile(fileWriter, null);

            fileWriter.close();
        }
        catch( IOException e)
        {
            System.out.print("Error while writing file!\n");
        }
    }

    /* (non-Javadoc)
     * @see at.emini.physics2DDesigner.DesignShape#saveToFile(at.emini.physics2DDesigner.MyFileWriter)
     */
    public void saveToFile(MyFileWriter fileWriter, Vector worldshapes)
    {
        try
        {
            //fileWriter.write( getShapeId() );
            fileWriter.write( (byte) mVertices.length );
            for( int i = 0; i < mVertices.length; i++)
            {
                fileWriter.writeFX( mVertices[i] );
            }
            fileWriter.writeInt( getElasticityFX());
            fileWriter.writeInt( getFrictionFX());
            fileWriter.writeInt( getMassFX());

            StringUserData.writeToStream(fileWriter, (StringUserData) mUserData);

        }
        catch( IOException e)
        {
            System.out.print("Error while writing file!\n");
        }

    }

    public static DesignShape loadFromFile(File file, UserData userData)
    {
        PhysicsFileReader reader = new PhysicsFileReader(file);

        Shape shape = Shape.loadShape(reader, userData);

        return new DesignShapeStd(shape);
    }


    public void update()
    {
        updateInternals();
        if ( ! checkConvex() )
        {
            for( int i = 0; i < cornersConvexCopy.length; i++)
            {
                ((ShapeVertex) designVertices.elementAt(i)).setPosFromVec(cornersConvexCopy[i]);
            }
            return;
        }
        for( int i = 0; i < mVertices.length; i++)
        {
            ((ShapeVertex) designVertices.elementAt(i)).setPosFromCorner();
            cornersConvexCopy[i].assign(mVertices[i]);
        }
    }

    public boolean checkConvex()
    {
        if (mVertices.length > 1)
        {
            for( int i = 0, j = mVertices.length - 1, k = mVertices.length - 2; i < mVertices.length; k = j, j = i, i++)
            {
                FXVector v1 = new FXVector(mVertices[i]);
                v1.subtract(mVertices[j]);
                FXVector v2 = new FXVector(mVertices[j]);
                v2.subtract(mVertices[k]);

                if (v1.crossFX(v2) < 0)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public static DesignShape loadDesignShape( PhysicsFileReader reader, UserData userData)
    {
        return new DesignShapeStd(Shape.loadShape(reader, userData));
    }

    /**
     * Scales the shape based on the movement of a vertex
     * @param currentVertex
     * @param newPoint
     */
    public void scale(ShapeVertex currentVertex, Point2D newPoint) {

        //calculate scale
        double factor = newPoint.distance(0, 0) / currentVertex.length();

        scale(factor);
    }

    public void scale(double factor)
    {
        for( int i = 0; i < designVertices.size(); i++)
        {
            ((ShapeVertex) designVertices.elementAt(i)).scale(factor);
        }
        update();
    }

    public void correctCentroid()
    {
        super.correctCentroid();
        update();
    }

}
