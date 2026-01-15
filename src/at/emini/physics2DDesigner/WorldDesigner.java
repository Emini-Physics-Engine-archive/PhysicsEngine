package at.emini.physics2DDesigner;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import at.emini.physics2D.Landscape;
import at.emini.physics2D.Shape;
import at.emini.physics2D.util.FXUtil;
import at.emini.physics2D.util.FXVector;

public class WorldDesigner extends MovePanel implements WorldChangeListener
{
    private static final long serialVersionUID = -6061848796822674138L;

    private DesignWorld world;

    private int currentAction = 0;
    private int selectedAction = 0;

    private DesignShape selectedShape;
    private DesignElementSelector selector = new DesignElementSelector();
    private DesignElementSelector highLightSelector = new DesignElementSelector();
    private FXVector selectionAreaPoint1 = null;
    
    private DesignJoint currentJoint;
    private DesignSpring currentSpring;

    private Color selectedColor = new Color(255, 200, 120, 120);
    private Color highlightedColor = new Color(255, 200, 120, 80);
    private Color secondaryColor = new Color(205, 250, 120, 80);
    
    private GraphicsWrapper currentGraphics;
    
    private UndoListener undoListener;
    
    private ObjectTreePanel tree;
    
    public WorldDesigner(DesignWorld world) 
    {
        super(true, true);
        setWorld(world);
                   
        initComponents();
    }
    
    public void setTree(ObjectTreePanel tree)
    {
        this.tree = tree;
    }
        
    private void initComponents()
    {
        validate();
    }
    
    public void setUndoListener(UndoListener listener)
    {
        undoListener = listener;
    }
    
    public void setWorld(DesignWorld world) 
    {        
        this.world = world;
        world.registerListener(this);
        selectedShape = null;
        selector = new DesignElementSelector();
        highLightSelector = new DesignElementSelector();
        refresh();
    }
    
    public DesignWorld getWorld()
    {
        return world;
    }
    
    public void loadImage()
    {
        JFileChooser chooser = new JFileChooser("Load Background image");
        chooser.setCurrentDirectory( Designer.stdDir );
        int returnVal = chooser.showOpenDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION ) 
        {          
            File imageFile = chooser.getSelectedFile();
        
            try 
            {            
                setBackgroundImage( ImageIO.read(imageFile), 0, 0 );
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void clearBackground(GraphicsWrapper g)
    {   
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    public void paintCanvas(GraphicsWrapper g)
    {
        grid.draw(g);
        
        world.draw(g, false);
        for (int i = 0; i < selector.getSelectedElements().size(); i++)
        {
            DesignSelectionObject element = (DesignSelectionObject) selector.getSelectedElements().get(i);
            //element.drawInteractives(g2, selectedColor);
            element.drawObject(g, selectedColor, secondaryColor, true);
        }
        
        if (selector.getMainSelectedElement()!= null)
        {
            selector.getMainSelectedElement().drawInteractives(g, selectedColor, secondaryColor);
        }
        
        if (highLightSelector.getMainSelectedElement()!= null)
        {
            highLightSelector.getMainSelectedElement().drawInteractives(g, highlightedColor, secondaryColor);
        }
        
        //highlight script objects
        for( int i = 0; i < world.getDesignScriptBodyCount(); i++)
        {
            DesignScript s = (DesignScript) world.getScriptBodyScript(i); 
            if( s.isVisible())
            {
                DesignBody b = (DesignBody) world.getScriptBody(i);
                b.drawObject(g, s.getColor(), null, true);
            }
        }
        
        currentGraphics = g;    
    }
    
    public DesignSelectionObject getSelection()
    {
        return selector.getMainSelectedElement();
    }
    
    public void setSelection(DesignSelectionObject object)
    {
        selector.setSelection(object);
        repaint();
    }
    
                
    public void setAction(int action)
    {
        selectedAction = action;
        if (action != Designer.ACTION_NEW_SHAPE)
        {
            selectedShape = null;
        }
        if (action != Designer.ACTION_JOINT && action != Designer.ACTION_FIX_JOINT)
        {
            currentJoint = null;
        }  
        if (action != Designer.ACTION_SPRING)
        {
            currentSpring = null;
        }  
        repaint();
    }

    public void setShape(DesignShape shape)
    {
        selectedShape = shape;
    }
    
    private void viewScript(int i, boolean selected)
    {
        
    }    
        
    private void drawMouseRelated(FXVector mousePos)
    {
        if (currentJoint != null)
        {
            refresh();
            
            Graphics2D g2 = (Graphics2D) canvas.getGraphics();
            transform(g2);
            currentJoint.drawSelecting(new GraphicsWrapper(g2), mousePos);            
        }
        
        if (currentSpring != null)
        {
            refresh();
            
            Graphics2D g2 = (Graphics2D) canvas.getGraphics();
            transform(g2);
            currentSpring.drawSelecting(new GraphicsWrapper(g2), mousePos);            
        }
        if (selectionAreaPoint1 != null)
        {
            refresh();
            
            Graphics2D g2 = (Graphics2D) canvas.getGraphics();
            transform(g2);
            GraphicsWrapper gw = new GraphicsWrapper(g2);            
            gw.drawRect(Math.min(selectionAreaPoint1.xFX, mousePos.xFX), 
                        Math.min(selectionAreaPoint1.yFX, mousePos.yFX), 
                        Math.abs(mousePos.xFX - selectionAreaPoint1.xFX), 
                        Math.abs(mousePos.yFX - selectionAreaPoint1.yFX) );
        }
    }
    
    private void updateCurrentAction(FXVector mousePos, int modifier)
    {
        DesignSelectionObject highLightedElement = highLightSelector.getMainSelectedElement();
        currentAction = Designer.ACTION_SCROLL;
        
        if (selectedAction == Designer.ACTION_POINT
                && (modifier & Event.CTRL_MASK) != 0)
        {
            currentAction = Designer.ACTION_POINT;
        }
        
        if ( highLightedElement == null)
        {
            if ( selectedAction == Designer.ACTION_NEW_SHAPE )
            {
                currentAction = selectedAction;
            }            
        }
        else
        {
            int objectAction = highLightedElement.getAction(currentGraphics, mousePos);
            if (objectAction < 0)
            {
                currentAction = Designer.ACTION_POINT;
            }            
            else
            {
                currentAction = objectAction;
            }
        }
        
        switch(selectedAction)
        {
        case Designer.ACTION_DELETE:
            if (highLightedElement != null) currentAction = Designer.ACTION_DELETE;
            break;
        case Designer.ACTION_JOINT:        
        	if (highLightedElement != null || currentJoint != null) currentAction = Designer.ACTION_JOINT;
        	break;
        case Designer.ACTION_FIX_JOINT:
        	if (highLightedElement != null || currentJoint != null) currentAction = Designer.ACTION_FIX_JOINT;
        	break;
        case Designer.ACTION_SPRING: 
        	if( highLightedElement != null || currentSpring != null) currentAction = Designer.ACTION_SPRING;
        	break;
        case Designer.ACTION_MOTOR:
        	if (highLightedElement != null && highLightedElement instanceof DesignBody) currentAction = Designer.ACTION_MOTOR;
        	break;
        case Designer.ACTION_NEW_LANDSCAPE:
            if ( currentAction == Designer.ACTION_NONE || 
                    currentAction == Designer.ACTION_SCROLL ||
                 (highLightedElement instanceof DesignLandscape && currentAction == Designer.ACTION_MOVE))
            {
                currentAction = Designer.ACTION_NEW_LANDSCAPE;
            }
            break;
        case Designer.ACTION_PARTICLE:
            currentAction = Designer.ACTION_PARTICLE;
            break;
        case Designer.ACTION_EVENT:
            currentAction = Designer.ACTION_EVENT;
            break;
        default: 
            break;
        }
        
        selector.setState(currentAction);
        highLightSelector.setState(currentAction);
        
        switch(currentAction)
        {
        case Designer.ACTION_MOVE:
            canvas.setCursor(Designer.moveCursor);
            break;
        case Designer.ACTION_ROTATE:
            canvas.setCursor(Designer.rotateCursor);
            break;
        case Designer.ACTION_SCROLL:
            canvas.setCursor(Designer.scrollCursor);
            break;
        case Designer.ACTION_NEW_SHAPE:
        case Designer.ACTION_PARTICLE:
        case Designer.ACTION_EVENT:
        case Designer.ACTION_NEW_LANDSCAPE:
        case Designer.ACTION_MOTOR:
            canvas.setCursor(Designer.newCursor);
            break;
        case Designer.ACTION_JOINT:
        case Designer.ACTION_SPRING:
            canvas.setCursor(Designer.jointCursor);
            break;
        case Designer.ACTION_FIX_JOINT:
            canvas.setCursor(Designer.fixJointCursor);
            break;
        case Designer.ACTION_DELETE:
            canvas.setCursor(Designer.deleteCursor);
            break;
        case Designer.ACTION_RESIZE_HORIZONTAL:
            canvas.setCursor(Designer.resizeHorizontalCursor);
            break;
        case Designer.ACTION_RESIZE_VERTICAL:
            canvas.setCursor(Designer.resizeVerticalCursor);
            break;
        case Designer.ACTION_POINT: 
        case Designer.ACTION_FACE_SWITCH:
        default:
            canvas.setCursor(Cursor.getDefaultCursor());
        }        
    }
    
    public void componentShown(ComponentEvent e) 
    {
        world.recalcBodyVertices();
        super.componentShown(e);
        
        repaint();
    }
    
    public void mouseMoved(MouseEvent e)
    {   
        if (currentAction == Designer.ACTION_NEW_LANDSCAPE)
        {
            mouseDragged(e);
        }
            
        FXVector mousePos = calcFXPosition(e.getX(), e.getY());
        
        DesignSelectionObject highlightedBefore = highLightSelector.getMainSelectedElement();
        world.checkObject( currentGraphics, mousePos, mousePos, highLightSelector, null );
        
        if (highlightedBefore != highLightSelector.getMainSelectedElement())
        {
            refresh();
        }
        
        /*if (currentAction == Designer.ACTION_NEW_LANDSCAPE && selectedElement != null)
        {
            selectedElement.makeMove( grid.snapToGrid(mousePos) );
            refresh();
        }
        else*/
        {
            updateCurrentAction(mousePos, e.getModifiers());
        }
        
        drawMouseRelated(mousePos);
    }
    
    public void mouseDragged(MouseEvent e)
    {        
        if (selector.getSelectedElements().size() > 0 && selectionAreaPoint1 == null)
        {   
            boolean movedone = false;
            FXVector mousePos = calcFXPosition(e.getX(), e.getY());
            
            for( int i = 0; i < selector.getSelectedElements().size(); i++)
            {
                DesignSelectionObject element = (DesignSelectionObject) selector.getSelectedElements().get(i);
                if (element.hasAction())
                {                    
                    element.makeMove( grid.snapToGrid(mousePos) );
                    movedone = true;
                }
            }
            
            if (movedone)
            {
                refresh();
                return;
            }
        }
        
        if (selectionAreaPoint1 != null)
        {
            selector.setSingleSelectionModifier(false);
            selector.setSelectionModifier(DesignElementSelector.STATE_SELECT_BODY);
            FXVector mousePos = calcFXPosition(e.getX(), e.getY());
            world.checkObject( currentGraphics, mousePos, selectionAreaPoint1, selector, null );
            refresh();
        }
                
        super.mouseDragged(e);
        
        drawMouseRelated(calcFXPosition(e.getX(), e.getY()));
    }
    
    
    public void mouseReleased(MouseEvent e) 
    {        
        if (selector.getSelectedElements().size() > 0 )
        {
            boolean actionOver = false;
            for( int i = 0; i < selector.getSelectedElements().size(); i++)
            {
                DesignSelectionObject element = (DesignSelectionObject) selector.getSelectedElements().get(i);
                actionOver |= element.unsetAction();
            }
            if (actionOver)
            {
                addUndoPoint();
            }
            
            FXVector mousePos = calcFXPosition(e.getX(), e.getY());
            updateCurrentAction(mousePos, e.getModifiers());            
        }
        
        
        selectionAreaPoint1 = null;
        if (selector.getSelectedElements().size() < 2)
        {
            selector.setSingleSelectionModifier(true);
        }
        selector.setSelectionModifier(DesignElementSelector.STATE_SELECT_ALL);
        refresh();
        
        super.mouseReleased(e);
    }
    
    public void mousePressed(MouseEvent e) 
    {
        FXVector mousePos = calcFXPosition(e.getX(), e.getY());
        if (currentAction == Designer.ACTION_NEW_LANDSCAPE)
        {
            newLandscapeElement(e);
            mouseMoved(e);          //check highlighted Element ->should be the landscape now
            super.mousePressed(e);
            return;
        }
        
        boolean newSelection = true;
    	//check click position for vertices
        if (selector.getSelectedElements().size() > 1)
        {   
            DesignBody b = world.checkBody( currentGraphics, mousePos, mousePos, null, null );
            if ( selector.containsSelection(b))
            {
                newSelection = false;
                selector.setMainElement(b);
            }
        } 
        
        if (newSelection)
        {
            selector.setSingleSelectionModifier(true);
            world.checkObject( currentGraphics, mousePos, selectionAreaPoint1, selector, null );
        }        
        
        if (selector.getMainSelectedElement() != null)
        {            
            int action = selector.getMainSelectedElement().setAction(currentGraphics, mousePos, grid.snapToGrid(mousePos));
            
            if (action == Designer.ACTION_MOVE)
            {
                for (int i = 0; i < selector.getSelectedElements().size(); i++)
                {
                    DesignSelectionObject element = (DesignSelectionObject) selector.getSelectedElements().get(i);
                    element.setCoAction(action, currentGraphics, mousePos, grid.snapToGrid(mousePos));
                }
            }
        }
        
        setSelectionInfo();
        

        if (currentAction == Designer.ACTION_POINT) 
        {
            selectionAreaPoint1 = new FXVector(mousePos);            
            return;
        }
        
        updateCurrentAction(mousePos, e.getModifiers());
        refresh();
        
        super.mousePressed(e);
    }
    
    public void mouseClicked(MouseEvent e) 
    {   
        DesignSelectionObject highLightedElement = highLightSelector.getMainSelectedElement();
        //check click position for vertices
        if (currentAction == Designer.ACTION_NEW_SHAPE && selectedShape != null)
        {
            newShape(e);            
        }
        else if (currentAction == Designer.ACTION_PARTICLE )
        {
            newParticle(e);            
        }
        else if (currentAction == Designer.ACTION_EVENT )
        {
            newEvent(e);            
        }
        else if (currentAction == Designer.ACTION_NEW_LANDSCAPE)
        {
            //putting new landscape here does not work, because 
            //the click event comes after the release, 
            //so we will always add the next element when we release the first
            //check if we currently move a landscape object
            /*if (! ((DesignLandscape) world.getLandscape()).hasAction() )
            {
                newLandscapeElement(e);
                mouseMoved(e);          //check highlighted Element ->should be the landscape now
            }*/
        }
        else if ((currentAction == Designer.ACTION_JOINT || currentAction == Designer.ACTION_FIX_JOINT) && (highLightedElement instanceof DesignBody || currentJoint != null))
        {
            createJoint(e);            
        }
        else if (currentAction == Designer.ACTION_SPRING && highLightedElement instanceof DesignBody )
        {
            createSpring(e);
        }
        else if (currentAction == Designer.ACTION_DELETE && highLightedElement != null)
        {
            deleteElement(e);            
        }
        else if (currentAction == Designer.ACTION_MOTOR)
        {
            newMotor(e);            
        }
        else if (currentAction == Designer.ACTION_FACE_SWITCH)
        {
            DesignLandscape landscape = (DesignLandscape) world.getLandscape();
            landscape.switchFace( calcFXPosition(e.getX(), e.getY()) );
            addUndoPoint();
            refresh();            
        }
        super.mouseClicked(e);
    }
    
    private void newMotor(MouseEvent e) {
        
        if ( highLightSelector.getMainSelectedElement() != null && 
                highLightSelector.getMainSelectedElement() instanceof DesignBody)
        {
            DesignBody body = (DesignBody) highLightSelector.getMainSelectedElement();
            DesignMotor motor = new DesignMotor(body, 1 << FXUtil.DECIMAL2, 100 * (1 << FXUtil.DECIMAL) );
            
            world.addConstraint(motor);
            addUndoPoint();
            
            selector.setSelection(motor);
            setSelectionInfo();
            
            refresh();
        }
    }

    private void deleteElement(MouseEvent e) 
    {
    	if (highLightSelector.getMainSelectedElement() instanceof DesignLandscape)
        {
        	((DesignLandscape) highLightSelector.getMainSelectedElement()).removeAt(currentGraphics, calcFXPosition(e.getX(), e.getY()));
        }
    	else
    	{
	        world.removeObject(highLightSelector.getMainSelectedElement());
	        addUndoPoint();
	        
	        FXVector mousePos = calcFXPosition(e.getX(), e.getY());
	        world.checkObject( currentGraphics, mousePos, selectionAreaPoint1, selector, null );
	        setSelectionInfo();
	        
    	}
    	highLightSelector.setSelection((DesignSelectionObject) null);
                
        refresh();
        
        updateCurrentAction(calcFXPosition(e.getX(), e.getY()), e.getModifiers());
    }

    private void createSpring(MouseEvent e) 
    {
        FXVector mousePos = calcFXPosition(e.getX(), e.getY());
        if (currentSpring == null)
        {
            currentSpring = new DesignSpring();
            currentSpring.setBody1((DesignBody) highLightSelector.getMainSelectedElement(), grid.snapToGrid(mousePos));                
        }
        else
        {
            if (! currentSpring.bothBodyFixed())
            {
                DesignBody secondBody = world.checkBody( currentGraphics, mousePos, mousePos, (DesignBody) currentSpring.getBody1(), null );
                if (secondBody != null)
                {
                    currentSpring.setBody2(secondBody, grid.snapToGrid(mousePos));
                    currentSpring.calcDistance();
                    
                    world.addConstraint(currentSpring);
                    selector.setSelection(currentSpring);
                    setSelectionInfo();
                    addUndoPoint();
                    
                    refresh();
                    
                    currentSpring = null;
                    updateCurrentAction(mousePos, e.getModifiers());
                }
            }
        }
    }

    private void createJoint(MouseEvent e) {
        boolean fixed = currentAction == Designer.ACTION_FIX_JOINT;
        if (currentJoint == null)
        {
            FXVector mousePos = calcFXPosition(e.getX(), e.getY());
            DesignBody secondBody = world.checkBody( currentGraphics, mousePos, mousePos, highLightSelector.getMainSelectedElement(), null );
            
            //check if two bodies present to set Joint directly
            if (secondBody != null)
            {                    
                DesignJoint joint = new DesignJoint(fixed);
                
                joint.setBody1((DesignBody) highLightSelector.getMainSelectedElement());
                joint.setBody2(secondBody);
                joint.setFixPoint(grid.snapToGrid(mousePos));
                
                joint.setCollisionLayer(world.getNextLayer());
                
                world.addConstraint(joint);
                addUndoPoint();
                refresh();
            }
            else
            {
                currentJoint = new DesignJoint(fixed);
                currentJoint.setBody1((DesignBody) highLightSelector.getMainSelectedElement());
            }
        }
        else
        {
            FXVector mousePos = calcFXPosition(e.getX(), e.getY());
            if (! currentJoint.bothBodyFixed())
            {
                DesignBody secondBody = world.checkBody( currentGraphics, mousePos, mousePos, (DesignBody) currentJoint.getBody1(), null );
                if (secondBody != null)
                {
                    currentJoint.setBody2(secondBody);
                }
            }
            else
            {
                currentJoint.setFixPoint(grid.snapToGrid(mousePos));                    
                //currentJoint.setColissionLayer(world.getNextLayer());
                
                world.addConstraint(currentJoint);
                addUndoPoint();
                repaint();
                
                currentJoint = null;
                updateCurrentAction(mousePos, e.getModifiers());
            }                    
        }
        
    }

    private void newLandscapeElement(MouseEvent e) 
    {
        FXVector mousePos = calcFXPosition(e.getX(), e.getY());
        FXVector gridPos = grid.snapToGrid(mousePos);
        FXVector endPoint = new FXVector(gridPos);
        world.getLandscape().addSegment(new FXVector(gridPos), endPoint, Landscape.FACE_LEFT);
        
        world.checkObject( currentGraphics, mousePos, selectionAreaPoint1, selector, null );
        ((DesignLandscape) world.getLandscape()).selectLastElement(mousePos, gridPos, endPoint);

        setSelectionInfo();
        
        updateCurrentAction(mousePos, e.getModifiers());
        refresh();        
    }

    private void newShape(MouseEvent e) 
    {
        FXVector mousePos = calcFXPosition(e.getX(), e.getY());
        FXVector gridPos = grid.snapToGrid(mousePos);
        
        DesignBody body = new DesignBody( gridPos.xFX, gridPos.yFX, (Shape) selectedShape, true );
        
        world.addBody(body);
        
        world.checkObject( currentGraphics, mousePos, selectionAreaPoint1, selector, null );
        setSelectionInfo();
        
        addUndoPoint();
        
        refresh();
    }
    
    private void newParticle(MouseEvent e) 
    {
        FXVector mousePos = calcFXPosition(e.getX(), e.getY());
        FXVector gridPos = grid.snapToGrid(mousePos);
        
        DesignSelectionObject highLightedElement = highLightSelector.getMainSelectedElement();
        DesignBody emitter = null;
        int posxFX = gridPos.xFX;
        int posyFX = gridPos.yFX; 
        if (highLightedElement instanceof DesignBody)
        {
            emitter = (DesignBody) highLightedElement;
            FXVector relativeAnchor = emitter.getRelativePoint(new FXVector(posxFX, posyFX));
            
            posxFX = relativeAnchor.xFX;
            posyFX = relativeAnchor.yFX;
        }
        DesignParticleEmitter particleEmitter = new DesignParticleEmitter(500, posxFX, posyFX, emitter, world.getTimestepFX());
                
        world.addParticleEmitter(particleEmitter);
        
        world.checkObject( currentGraphics, mousePos, selectionAreaPoint1, selector, null );
        setSelectionInfo();
        
        addUndoPoint();
        
        refresh();
    }
    
    private void newEvent(MouseEvent e) 
    {
        FXVector mousePos = calcFXPosition(e.getX(), e.getY());
        FXVector gridPos = grid.snapToGrid(mousePos);
        
        DesignPhysicsEvent event = new DesignAreaEvent(null, null, 
                FXUtil.fromFX(mousePos.xFX - 10 * FXUtil.ONE_FX), 
                FXUtil.fromFX(mousePos.yFX - 10 * FXUtil.ONE_FX), 
                FXUtil.fromFX(mousePos.xFX + 10 * FXUtil.ONE_FX), 
                FXUtil.fromFX(mousePos.yFX + 10 * FXUtil.ONE_FX));
        world.addEvent(event);
                
        world.checkObject( currentGraphics, mousePos, selectionAreaPoint1, selector, null );
        setSelectionInfo();
        
        addUndoPoint();
        
        refresh();
    }

    private void setSelectionInfo()
    {
        DesignSelectionObject selectedElement = selector.getMainSelectedElement();
        //set selection on tree
        if (tree != null)
        {
            tree.select(selectedElement);
        }
    }

    public void worldChanged(DesignWorld world) 
    {
        if (getGraphics() != null)
        {
            refresh();            
        }
    }
    
    public void updateRequired() 
    {
        if (getGraphics() != null)
        {
            refresh();            
        }
    }
    
    private void addUndoPoint()
    {
        if ( undoListener != null)
        {
            undoListener.addUndoPoint();
        }
    } 
    
}
