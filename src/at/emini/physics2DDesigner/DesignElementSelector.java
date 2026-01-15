package at.emini.physics2DDesigner;

import java.util.Vector;

public class DesignElementSelector 
{
	public static final int STATE_NONE = 0x00;
	public static final int STATE_SELECT_BODY          = 0x01;
	public static final int STATE_SELECT_CONSTRAINT    = 0x02;
    public static final int STATE_SELECT_LANDSCAPE     = 0x04;
    public static final int STATE_SELECT_EVENT         = 0x08;
    public static final int STATE_SELECT_PARTICLE      = 0x10;
    public static final int STATE_SELECT_ALL           = 0x1F;
	
	public static final int STATE_PARTIAL_SELECTION    = 0x100;
	public static final int STATE_SINGLE_SELECTION     = 0x200;
    
	
	/**
	 * State of the selection process
	 */
	private int state = STATE_SELECT_ALL | STATE_SINGLE_SELECTION;

	/**
	 * Main selection element
	 */
	private Vector selectedElements = new Vector();
    private DesignSelectionObject mainSelectedElement;
	
	public DesignElementSelector()
	{
		
	}
	
	public void setState(int action)
	{
	    state = state & ~STATE_SELECT_ALL;
	    
		switch (action)
		{
		case Designer.ACTION_JOINT:
		case Designer.ACTION_SPRING:
		    state = state | STATE_SELECT_BODY; 
        	break;
		case Designer.ACTION_NEW_LANDSCAPE:
			state = state | STATE_SELECT_LANDSCAPE;
			break;
		default: 
			state = state | STATE_SELECT_ALL;
			break;
		}
	}
	
	public void setPartialSelectionModifier(boolean selected)
    {
        setModifier(STATE_PARTIAL_SELECTION, selected); 
    }
    
	public void setSingleSelectionModifier(boolean selected)
    {
        setModifier(STATE_SINGLE_SELECTION, selected); 
    }
	
	public void setSelectionModifier(int modifier)
	{
	    state = state & ~STATE_SELECT_ALL;
	    state = state | modifier;
	}
	
	private void setModifier(int modifier, boolean selected)
	{
	    if (selected)
	    {
	        state |= modifier;
	    }
	    else
	    {
            state &= ~modifier;	        
	    }
	}
	
	public boolean canSelectBody()
	{
		return (state & STATE_SELECT_BODY) != 0;
	}
	
	public boolean canSelectConstraint()
    {
        return (state & STATE_SELECT_CONSTRAINT) != 0;
    }

	public boolean canSelectParticle()
    {
        return (state & STATE_SELECT_PARTICLE) != 0;
    }

	public boolean canSelectEvent()
    {
        return (state & STATE_SELECT_EVENT) != 0;
    }

	public boolean canSelectLandscape()
	{
		return (state & STATE_SELECT_LANDSCAPE) != 0;
	}
	
	public boolean canSelectMultiple()
	{
	    return  (state & STATE_SINGLE_SELECTION) == 0;
	}

	public void setSelection(Vector selectionElements)
	{
	    mainSelectedElement = null;
	    if ( (state & STATE_PARTIAL_SELECTION) != 0)
	    {
	        //check if the all of the selectedElements are contained in the list
	        boolean allContained = true;
	        for( int i = 0; i < selectionElements.size(); i++ )
	        {
	            if (! selectedElements.contains(selectionElements.elementAt(i)) )
	            {
	                allContained = false;
	            } 
	        }
	        
	        if (!allContained || selectionElements.size() == 0)
	        {
	            selectElements(selectionElements);	            
	        }
	    }
	    else
	    {
	        selectElements(selectionElements);
	    }
	}
	
	private void selectElements(Vector elements)
	{
	    if ((state & STATE_SINGLE_SELECTION) != 0)
	    {
	        selectedElements.clear();
	        if ( elements.size() > 0)
	        {
	            selectedElements.add(elements.get(0));
	        }
	    }
	    else
	    {
	        selectedElements = elements;
	    }
	    
	    setMainElement();
	}

	public void setSelection(DesignSelectionObject selection)
	{
	    if ((state & STATE_SINGLE_SELECTION) != 0 || !containsSelection(selection))
        {
    	    selectedElements.clear();
    	    if (selection != null)
    	    {
    	        selectedElements.add(selection);
    	    }
        }	    
	    mainSelectedElement = selection;
	}
	
	public void clearSelection()
	{
        selectedElements.clear();
	    mainSelectedElement = null;
	}
	
	public void addSelection(DesignSelectionObject selection)
	{
	    if ((state & STATE_SINGLE_SELECTION) != 0)
        {
            if ( selectedElements.size() == 0)
            {
                selectedElements.add(selection);
                mainSelectedElement = selection;
            }
        }
        else
        {
            selectedElements.add(selection);
            if ( selectedElements.size() == 0)
            {
                mainSelectedElement = selection;
            }
        }
	}
	
	public Vector getSelectedElements()
	{
	    return selectedElements;
	}
	
	public boolean containsSelection(Object o)
	{
	    return selectedElements.contains(o);
	}
	
	public void setMainElement()
	{
	    if (selectedElements.size() > 0)
	    {
	        mainSelectedElement = (DesignSelectionObject) selectedElements.get(0);
	    }
	}
	
	public void setMainElement(DesignSelectionObject element)
    {
        if (containsSelection(element))
        {
            mainSelectedElement = element;
        }
    }
	
	public DesignSelectionObject getMainSelectedElement()
    {
        return mainSelectedElement;
    }

}
