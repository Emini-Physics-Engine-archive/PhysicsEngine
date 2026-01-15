package at.emini.physics2DDesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;


public class MovePanel extends JPanel implements ComponentListener, MouseMotionListener, MouseListener, MouseWheelListener {

    /**
     *
     */
    private static final long serialVersionUID = 1002125619228651245L;

    private AffineTransform transform;
    private static final int decimal = FXUtil.DECIMAL;
    private static final int koeff = 1 << decimal;      //trick to avoid preprocessing
    public static final AffineTransform FXTransform =
        AffineTransform.getScaleInstance(1.0 / koeff, 1.0 / koeff);
    private static final AffineTransform baseTransform = new AffineTransform();
    protected boolean centerInitialization = false;

    private boolean move = false;
    private int xOffset = 0;
    private int yOffset = 0;

    protected static Grid grid = new Grid();

    protected JPanel canvas;
    protected MovePanelRuler topRuler;
    protected MovePanelRuler leftRuler;

    protected BufferedImage imageBuffer; //double buffering

    private BufferedImage backgroundImage = null;
    private int anchorx = 0;
    private int anchory = 0;

    public MovePanel(boolean useruler, boolean interactive)
    {
        //transform = new AffineTransform();
        transform = new AffineTransform(baseTransform);
        setLayout(new BorderLayout());

        addComponentListener(this);

        topRuler = new MovePanelRuler(MovePanelRuler.HORIZONTAL);
        topRuler.setTransform(transform);
        topRuler.setVisible(useruler);

        leftRuler = new MovePanelRuler(MovePanelRuler.VERTICAL);
        leftRuler.setTransform(transform);
        leftRuler.setVisible(useruler);

        add(topRuler, BorderLayout.NORTH);
        add(leftRuler, BorderLayout.WEST);

        canvas = new JPanel()
        {
            private static final long serialVersionUID = 1L;

            public void paint(Graphics g)
            {
                long timeStart = 0, timeEnd = 0;
                timeStart = System.nanoTime();
                if (imageBuffer == null)
                {
                    return;
                }
                Graphics doubleBufferGraphics = imageBuffer.getGraphics();
                if (doubleBufferGraphics != null )
                {

                    GraphicsWrapper wrapper = new GraphicsWrapper((Graphics2D) doubleBufferGraphics);
                    clearBackground(wrapper);
                    wrapper.transform(transform);
                    paintBackground(wrapper);
                    wrapper.transform(FXTransform);
                    paintCanvas(wrapper);

                }

                if (g != null)
                {
                    g.drawImage(imageBuffer, 0, 0, this);
                }
                timeEnd = System.nanoTime();
                //System.out.println("Milli: " + ((double) (timeEnd - timeStart) / 1000000.0));

            }

            public void update(Graphics g)
            {
                if (g != null && imageBuffer != null)
                {
                    g.drawImage(imageBuffer, 0, 0, this);
                }
                //paint(g);
            }

        };

        add(canvas, BorderLayout.CENTER);

        if (interactive)
        {
            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);
            canvas.addMouseWheelListener(this);
        }
    }

    public void setRulerVisibility(boolean useruler)
    {
        topRuler.setVisible(useruler);
        leftRuler.setVisible(useruler);
    }

    public void setBackgroundImage(BufferedImage image, int x, int y)
    {
        anchorx = x;
        anchory = y;

        backgroundImage = image;
        backgroundImage.setAccelerationPriority(0.8f);
        //System.out.println( "BackgroundImage: " + backgroundImage.getCapabilities(getGraphicsConfiguration()).isAccelerated() );
    }

    public void paintCanvas(GraphicsWrapper g)
    {
    }

    public void clearBackground(GraphicsWrapper g)
    {
    }

    public void paintBackground(GraphicsWrapper g)
    {
        if (backgroundImage != null)
        {
            g.drawImage(backgroundImage, anchorx, anchory, this);
        }
    }

    public static void setGridParam(float spacing, boolean active)
    {
        grid.setParameter((int)(spacing * FXUtil.ONE_FX), active);
    }

    public AffineTransform getTransform()
    {
        return transform;
    }

    public void componentHidden(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentResized(ComponentEvent e)
    {
        if (!centerInitialization )
        {
            transform.preConcatenate(AffineTransform.getTranslateInstance(getWidth()/2,getHeight()/2));
            centerInitialization = true;
        }

        if (getWidth() <= 0 || getHeight() <= 0)
        {
            return;
        }

        //imageBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        GraphicsConfiguration gc = getGraphicsConfiguration();
        imageBuffer = gc.createCompatibleImage(getWidth(), getHeight());
        imageBuffer.setAccelerationPriority(0.9f);
        //System.out.println( "Acc: " + gc.getImageCapabilities().isAccelerated());
        //System.out.println( "imageBuffer: " + imageBuffer.getCapabilities(getGraphicsConfiguration()).isAccelerated() );

        Graphics g = imageBuffer.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        refresh();
    }
    public void componentShown(ComponentEvent e) {}

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    public void mouseDragged(MouseEvent e)
    {
        if (move)
        {
            transform.preConcatenate( AffineTransform.getTranslateInstance(e.getX() - xOffset,e.getY() - yOffset) );
            xOffset = e.getX();
            yOffset = e.getY();
            refresh();
        }
    }

    public void mousePressed(MouseEvent e)
    {
        move = true;
        xOffset = e.getX();
        yOffset = e.getY();
    }

    public void mouseReleased(MouseEvent e)
    {
        move = false;
    }

    public void setScale(double scale)
    {
        transform.setToScale(scale, scale);
        transform.preConcatenate( AffineTransform.getTranslateInstance(getWidth()/2,getHeight()/2) );
        refresh();
    }

    public void scale(double scale)
    {
        transform.preConcatenate( AffineTransform.getTranslateInstance(-getWidth()/2,-getHeight()/2) );
        transform.preConcatenate( AffineTransform.getScaleInstance(scale, scale));
        transform.preConcatenate( AffineTransform.getTranslateInstance(getWidth()/2,getHeight()/2) );
        refresh();
    }

    public void translate(double x, double y)
    {
        transform.preConcatenate( AffineTransform.getTranslateInstance(x, y) );
        refresh();
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        double scale = Math.pow(1.1, -e.getWheelRotation());
        transform.preConcatenate( AffineTransform.getTranslateInstance(-getWidth()/2,-getHeight()/2) );
        transform.preConcatenate( AffineTransform.getScaleInstance(scale, scale) );
        transform.preConcatenate( AffineTransform.getTranslateInstance(getWidth()/2,getHeight()/2) );
        refresh();
    }

    protected void transform(Graphics2D g)
    {
        g.transform(transform);
        g.transform(FXTransform);
    }

    protected FXVector calcFXPosition(int x, int y)
    {
        Point2D src = new Point2D.Double(x, y);
        Point2D dest = new Point2D.Double();
        try {
            transform.inverseTransform(src, dest);

            //System.out.println("compute: (" + x + "," + y + ") to "+ dest.getX() + ", "+ dest.getY() + ")");
            return new FXVector( (int) ((float) dest.getX() * FXUtil.ONE_FX), (int) ((float)dest.getY() * FXUtil.ONE_FX));

        }
        catch (NoninvertibleTransformException e1)
        {
            e1.printStackTrace();
        }
        return new FXVector();
    }

    protected Point2D calcPosition(int x, int y)
    {
        Point2D src = new Point2D.Double(x, y);
        Point2D interm = new Point2D.Double();
        Point2D dest = new Point2D.Double();
        try
        {
            transform.inverseTransform(src, interm);
            FXTransform.inverseTransform(interm, dest);

            return dest;

        }
        catch (NoninvertibleTransformException e1)
        {
            e1.printStackTrace();
        }
        return src;
    }

    public void adjustTransform(MovePanel other)
    {
        transform = other.transform;
        topRuler.setTransform(transform);
        leftRuler.setTransform(transform);
        refresh();
    }

    public void refresh()
    {
        canvas.paint(canvas.getGraphics());
        if (topRuler.isVisible())
        {
            topRuler.paint(topRuler.getGraphics());
            leftRuler.paint(leftRuler.getGraphics());
        }
    }
}
