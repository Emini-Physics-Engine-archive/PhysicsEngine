package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class ShapeDesigner extends MovePanel 
{
    private static final long serialVersionUID = 1940262647334780327L;

    private int currentAction;
    private int selectedAction = Designer.ACTION_POINT;
    private ShapeElement currentVertex = null;
    
    protected DesignShapeStd shape;
    protected DesignWorld world;  //for change update reference
    
    private boolean interactiveMode = true;
    //private int resizeRadius = 5;
    
    private GraphicsWrapper currentGraphics = new GraphicsWrapper(null);
        
    public ShapeDesigner(DesignShapeStd shape, DesignWorld world, boolean interactive)
    {
        super(false, interactive);
        interactiveMode = interactive;
        
        this.world = world;
        this.shape = shape;
        
        initComponents();
    }       
    
    public void setShape(DesignShapeStd s)
    {
        shape = s;
        refresh();
    }
    
    
    public DesignShape getShape()
    {
        return shape;
    }
    
    private void initComponents()
    {
        //contextMenu = new JPopupMenu("Shape Options");        
    }
    
    public Dimension getPreferredSize()
    {
        return new Dimension(200, 100);
    }
    
    public void clearBackground(GraphicsWrapper g)
    {   
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());        
    }
    
    public void paintCanvas(GraphicsWrapper g)
    {
        if (g != null)
        {            
            grid.draw(g);
                            
            if (shape != null)
            {
                shape.draw(g, true);
            }
            currentGraphics = g;
        }
        //g2.setColor(Color.blue);
        //g2.drawArc( - resizeRadius, -resizeRadius, resizeRadius * 2, resizeRadius * 2, 0, 360);
    }
    
    private void updateCurrentAction(Point2D pos, int modifiers)
    {
        currentAction = Designer.ACTION_SCROLL;
        
        if (shape != null)
        {   
            ShapeElement overElement = shape.checkElements(currentGraphics, pos);
            if ( overElement != null)
            {
                if ((modifiers & MouseEvent.CTRL_MASK) != 0
                    && overElement instanceof ShapeVertex)
                {
                    currentAction = Designer.ACTION_RESIZE;
                }
                else if (overElement instanceof ShapeVertex)
                {
                    currentAction = Designer.ACTION_MOVE_POINT;
                }
                else 
                {
                    currentAction = Designer.ACTION_MOVE;
                }
                
                switch(selectedAction)
                {
                case Designer.ACTION_NEW_VERTEX:
                    if (overElement instanceof ShapeEdge)
                    {
                        currentAction = Designer.ACTION_NEW_VERTEX;
                    }
                    break;
                case Designer.ACTION_DELETE:
                    if (overElement instanceof ShapeVertex)
                    {
                        currentAction = Designer.ACTION_DELETE;
                    }
                    break;
                default: 
                    break;
                }
                
            }            
        }
                
        
        switch(currentAction)
        {
        case Designer.ACTION_MOVE:
            canvas.setCursor(Designer.moveCursor);
            break;
        case Designer.ACTION_MOVE_POINT:
            canvas.setCursor(Designer.movePointCursor);
            break;
        case Designer.ACTION_SCROLL:
            canvas.setCursor(Designer.scrollCursor);
            break;
        case Designer.ACTION_NEW_SHAPE:
            canvas.setCursor(Designer.newCursor);
            break;
        case Designer.ACTION_RESIZE:
            canvas.setCursor(Designer.resizeVerticalCursor);
            break;
        case Designer.ACTION_DELETE:
            canvas.setCursor(Designer.deleteCursor);
            break;
        case Designer.ACTION_NEW_VERTEX:
            canvas.setCursor(Designer.vertexCursor);
            break;
        default:
            canvas.setCursor(Cursor.getDefaultCursor());
        }

    }
        
    public void setSelectedAction(int action)
    {
        selectedAction = action;
    }   
    
    public void mouseMoved(MouseEvent e)
    {
        Point2D pos = calcPosition(e.getX(), e.getY());
        updateCurrentAction(pos, e.getModifiers());
        
        super.mouseMoved(e);
    }
    
    public void mouseDragged(MouseEvent e)
    {
        if (currentVertex != null)
        {            
            if ((e.getModifiers() & MouseEvent.CTRL_MASK) != 0 
                    && currentVertex instanceof ShapeVertex)
            {
                //shape.scale((ShapeVertex) currentVertex, calcPosition(e.getX(), e.getY()) );
                shape.scale((ShapeVertex) currentVertex, grid.snapToGrid(calcPosition(e.getX(), e.getY())) );
            }
            else
            {
                //currentVertex.setPos( calcPosition(e.getX(), e.getY()) );
                currentVertex.setPos( grid.snapToGrid(calcPosition(e.getX(), e.getY())) );
            }
            shape.update();
            
            refresh();
            invokeChangeListeners();
                        
            return;
        }
        super.mouseDragged(e);
    }
    
    public void mouseReleased(MouseEvent e) 
    {        
        if (currentVertex != null)
        {
            currentVertex.setMoving( false );            
        }
        if (currentVertex != null)
        {
            if (e.isPopupTrigger())
            {
                showContextMenu(e.getX(), e.getY());                            
            }
        }
        Point2D pos = calcPosition(e.getX(), e.getY());
        updateCurrentAction(pos, e.getModifiers());
        super.mouseReleased(e);
    }
    
    public void mousePressed(MouseEvent e) 
    {
        //check click position for vertices
        if (shape != null)
        {
            Point2D pos = calcPosition(e.getX(), e.getY());
            updateCurrentAction(pos, e.getModifiers());
            
            currentVertex = shape.checkElements(currentGraphics, pos );
            if (currentVertex != null)
            {
                switch(currentAction)
                {
                case Designer.ACTION_POINT:
                    currentVertex.setMoving( true );
                    refresh();
                    
                    if (e.isPopupTrigger())
                    {
                        showContextMenu(e.getX(), e.getY());                            
                    }                     
                    break;
                case Designer.ACTION_NEW_VERTEX:
                    if (currentVertex instanceof ShapeEdge)
                    {
                        shape.insertVertex((ShapeEdge) currentVertex, pos);
                        invokeChangeListeners();
                        refresh();
                    }
                    break;
                case Designer.ACTION_DELETE:
                    if (currentVertex instanceof ShapeVertex)
                    {
                        shape.deleteVertex((ShapeVertex) currentVertex);
                        invokeChangeListeners();
                        refresh();
                    }
                    break;
                default:
                    break;
                }
            return;
            }
        }        
        
        super.mousePressed(e);
    }
    
    private void showContextMenu(int x, int y)
    {
        /*if (currentVertex != null)
        {
            contextMenu.removeAll();
            
            if (shape != null)
            {
                JMenuItem[] items = shape.getMenuItems(currentVertex);
                
                for( int i = 0; i < items.length; i++)
                {
                    contextMenu.add(items[i]);
                }                
                contextMenu.show(this, x, y);
            }
        }*/
    }

    public void infoPanelSelected(ShapeInfoPanel s) {
        refresh();
    }
    
    
    private void invokeChangeListeners()
    {
        world.updateAndRepaint();
    }
}